package ru.yandex.practicum.filmorate.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.LocalDate;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WebMvcTest(FilmController.class)
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
public class FilmControllerTest {
    private static final String CORRECT_NAME = "Toy Story";
    private static final String CORRECT_DESCRIPTION = "Good movie";
    private static final LocalDate CORRECT_RELEASE_DATE = LocalDate.of(1995, 1, 1);
    private static final Integer CORRECT_DURATION = 90;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String INCORRECT_DESCRIPTION = "Good movie".repeat(200);
    private static final LocalDate INCORRECT_RELEASE_DATE = LocalDate.of(1895, 1, 1);
    private static final Integer INCORRECT_DURATION = -90;

    private static final String UPDATE_NAME = "UPDATE_Toy Story";
    private static final String UPDATE_DESCRIPTION = "UPDATE_Good movie";
    private static final LocalDate UPDATE_RELEASE_DATE = LocalDate.of(1995, 11, 19);
    private static final Integer UPDATE_DURATION = 81;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void addFilm_allFieldsOk() throws Exception {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        MvcResult result = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Film responseFilm = MAPPER.readValue(result.getResponse().getContentAsString(), Film.class);

        assertEquals(CORRECT_NAME, responseFilm.getName());
        assertEquals(CORRECT_DESCRIPTION, responseFilm.getDescription());
        assertEquals(CORRECT_RELEASE_DATE, responseFilm.getReleaseDate());
        assertEquals(CORRECT_DURATION, responseFilm.getDuration());
        assertEquals(1, responseFilm.getId());
    }

    @Test
    @Order(2)
    void updateFilm_allFieldsOk() throws Exception {
        Film film = new Film();
        film.setId(1L);
        film.setName(UPDATE_NAME);
        film.setDescription(UPDATE_DESCRIPTION);
        film.setReleaseDate(UPDATE_RELEASE_DATE);
        film.setDuration(UPDATE_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        MvcResult result = mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Film responseFilm = MAPPER.readValue(result.getResponse().getContentAsString(), Film.class);

        assertEquals(UPDATE_NAME, responseFilm.getName());
        assertEquals(UPDATE_DESCRIPTION, responseFilm.getDescription());
        assertEquals(UPDATE_RELEASE_DATE, responseFilm.getReleaseDate());
        assertEquals(UPDATE_DURATION, responseFilm.getDuration());
        assertEquals(1, responseFilm.getId());
    }

    @Test
    @Order(3)
    void getFilms() throws Exception {
        MvcResult result = mockMvc.perform(get("/films")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Film[] films = MAPPER.readValue(result.getResponse().getContentAsString(), Film[].class);

        assertEquals(1, films.length);
    }

    @Test
    @Order(4)
    void addLike_Ok() throws Exception {
        addUser(0);

        mockMvc.perform(put("/films/1/like/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.likes", contains(1)));
    }

    @Test
    @Order(5)
    void deleteLike_Ok() throws Exception {
        mockMvc.perform(delete("/films/1/like/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        MvcResult result = mockMvc.perform(get("/films")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Film[] films = MAPPER.readValue(result.getResponse().getContentAsString(), Film[].class);

        assertFalse(films[0].getLikes().contains(1L));
    }

    @Test
    void findPopular_Ok() throws Exception {
        for (int i = 1; i <= 15; i++) {
            addFilm(i);
        }
        for (int i = 1; i <= 15; i++) {
            addUser(i);
            if (i == 14) {
                addLike(15, i);
            } else {
                addLike(i, i);
            }
        }


        mockMvc.perform(get("/films/popular")).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$", hasSize(10))).
                andExpect(jsonPath("$[0].id", is(15)));

        mockMvc.perform(get("/films/popular?count=5")).
                andExpect(status().isOk()).
                andExpect(content().contentType(MediaType.APPLICATION_JSON)).
                andExpect(jsonPath("$", hasSize(5))).
                andExpect(jsonPath("$[0].id", is(15)));
    }

    @Test
    void addFilm_blankName_badRequest() throws Exception {
        Film film = new Film();
        film.setName("");
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_incorrectDescription_badRequest() throws Exception {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(INCORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_incorrectReleaseDate_badRequest() throws Exception {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(INCORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_incorrectDuration_badRequest() throws Exception {
        Film film = new Film();
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(INCORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateFilm_noSuchFilm_notFound() throws Exception {
        Film film = new Film();
        film.setId(1000L);
        film.setName(CORRECT_NAME);
        film.setDescription(CORRECT_DESCRIPTION);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isNotFound());
    }

    private void addUser(int number) throws Exception {
        User user = new User();
        user.setName("user_name" + number);
        user.setEmail(String.format("user%d@email.com", number));
        user.setBirthday(LocalDate.of(1900, 1, 1));
        user.setLogin("user_login" + number);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private void addFilm(int number) throws Exception {
        Film film = new Film();
        film.setName("Film_name" + number);
        film.setDescription("Film_description" + number);
        film.setReleaseDate(CORRECT_RELEASE_DATE);
        film.setDuration(CORRECT_DURATION);

        String userJson = MAPPER.writeValueAsString(film);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private void addLike(int film, int user) throws Exception {
        String URI = String.format("/films/%d/like/%d", film, user);
        mockMvc.perform(put(URI))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
