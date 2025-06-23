package ru.yandex.practicum.filmorate.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CORRECT_NAME = "Name";
    private static final String CORRECT_DESCRIPTION = "Description";
    private static final LocalDate CORRECT_RELEASE_DATE = LocalDate.of(1995, 1, 1);
    private static final int CORRECT_DURATION = 100;
    private static final int CORRECT_MPA_ID = 1;
    private static final int CORRECT_GENRE_ID1 = 1;
    private static final int CORRECT_GENRE_ID2 = 2;

    private static final String INCORRECT_DESCRIPTION = "Description".repeat(200);
    private static final LocalDate INCORRECT_RELEASE_DATE = LocalDate.of(1895, 1, 1);
    private static final int INCORRECT_DURATION = -90;
    private static final int INCORRECT_MPA_ID = 100;
    private static final int INCORRECT_GENRE_ID = 100;

    private static final String UPDATE_NAME = "UPDATE_Name";
    private static final String UPDATE_DESCRIPTION = "UPDATE_Description";
    private static final LocalDate UPDATE_RELEASE_DATE = LocalDate.of(2000, 12, 31);
    private static final int UPDATE_DURATION = 200;
    private static final int UPDATE_MPA_ID = 2;
    private static final int UPDATE_GENRE_ID = 3;

    @Test
    @Order(1)
    void addFilm_OK() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(CORRECT_NAME)))
                .andExpect(jsonPath("$.description", is(CORRECT_DESCRIPTION)))
                .andExpect(jsonPath("$.releaseDate", is(CORRECT_RELEASE_DATE.toString())))
                .andExpect(jsonPath("$.duration", is(CORRECT_DURATION)))
                .andExpect(jsonPath("$.mpa.id", is(CORRECT_MPA_ID)))
                .andExpect(jsonPath("$.genres[0].id", is(CORRECT_GENRE_ID1)));
    }

    @Test
    @Order(2)
    void updateFilm_allFieldsOk() throws Exception {
        String filmJson = jsonUpdateString(
                1,
                UPDATE_NAME,
                UPDATE_DESCRIPTION,
                UPDATE_RELEASE_DATE,
                UPDATE_DURATION,
                UPDATE_MPA_ID,
                UPDATE_GENRE_ID
        );

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(UPDATE_NAME)))
                .andExpect(jsonPath("$.description", is(UPDATE_DESCRIPTION)))
                .andExpect(jsonPath("$.releaseDate", is(UPDATE_RELEASE_DATE.toString())))
                .andExpect(jsonPath("$.duration", is(UPDATE_DURATION)))
                .andExpect(jsonPath("$.mpa.id", is(UPDATE_MPA_ID)))
                .andExpect(jsonPath("$.genres[0].id", is(UPDATE_GENRE_ID)));
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
    void deleteLike_ok() throws Exception {
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
    void getFilmById_ok() throws Exception {
        mockMvc.perform(get("/films/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void findPopular_ok() throws Exception {
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

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].id", is(15)));

        mockMvc.perform(get("/films/popular?count=5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].id", is(15)));
    }

    @Test
    void addFilm_multiple_genre_OK() throws Exception {
        String filmJson = jsonTwoGenersString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1,
                CORRECT_GENRE_ID2
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is(CORRECT_NAME)))
                .andExpect(jsonPath("$.description", is(CORRECT_DESCRIPTION)))
                .andExpect(jsonPath("$.releaseDate", is(CORRECT_RELEASE_DATE.toString())))
                .andExpect(jsonPath("$.duration", is(CORRECT_DURATION)))
                .andExpect(jsonPath("$.mpa.id", is(CORRECT_MPA_ID)))
                .andExpect(jsonPath("$.genres[0].id", is(CORRECT_GENRE_ID1)))
                .andExpect(jsonPath("$.genres[1].id", is(CORRECT_GENRE_ID2)));
    }

    @Test
    void addFilm_blankName_badRequest() throws Exception {
        String filmJson = jsonString(
                "",
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_failDescription_badRequest() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                INCORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_failReleaseDate_badRequest() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                INCORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_failDuration_badRequest() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                INCORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addFilm_failMpa_notFound() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                INCORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void addFilm_failGenre_notFound() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                INCORRECT_GENRE_ID
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFilm_noSuchFilm_notFound() throws Exception {
        String filmJson = jsonUpdateString(
                1000,
                UPDATE_NAME,
                UPDATE_DESCRIPTION,
                UPDATE_RELEASE_DATE,
                UPDATE_DURATION,
                UPDATE_MPA_ID,
                UPDATE_GENRE_ID
        );

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateFilm_noId_badRequest() throws Exception {
        String filmJson = jsonString(
                CORRECT_NAME,
                CORRECT_DESCRIPTION,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getFilmById_notFound() throws Exception {
        mockMvc.perform(get("/films/1001")
                        .accept(MediaType.APPLICATION_JSON))
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
        String filmJson = jsonString(
                "Name" + number,
                "Description" + number,
                CORRECT_RELEASE_DATE,
                CORRECT_DURATION,
                CORRECT_MPA_ID,
                CORRECT_GENRE_ID1
        );

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filmJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private void addLike(int film, int user) throws Exception {
        String uri = String.format("/films/%d/like/%d", film, user);
        mockMvc.perform(put(uri))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private String jsonNoMpaNoGenreString(String name, String description, LocalDate releaseDate, int duration) {
        return String.format("{\"name\":\"%s\"," +
                        "\"description\":\"%s\"," +
                        "\"releaseDate\":\"%s\"," +
                        "\"duration\":%d}",
                name, description, releaseDate, duration);
    }

    private String jsonString(String name, String description, LocalDate releaseDate, int duration, int mpaId, int genreId) {
        return String.format("{\"name\":\"%s\"," +
                        "\"description\":\"%s\"," +
                        "\"releaseDate\":\"%s\"," +
                        "\"duration\":%d," +
                        "\"mpa\":{\"id\":%d}," +
                        "\"genres\":[{\"id\":%d}]}",
                name, description, releaseDate, duration, mpaId, genreId);
    }

    private String jsonUpdateString(int id, String name, String description, LocalDate releaseDate, int duration, int mpaId, int genreId) {
        return String.format("{\"id\":\"%d\"," +
                        "\"name\":\"%s\"," +
                        "\"description\":\"%s\"," +
                        "\"releaseDate\":\"%s\"," +
                        "\"duration\":%d," +
                        "\"mpa\":{\"id\":%d}," +
                        "\"genres\":[{\"id\":%d}]}",
                id, name, description, releaseDate, duration, mpaId, genreId);
    }

    private String jsonTwoGenersString(String name, String description, LocalDate releaseDate, int duration, int mpaId, int genreId1, int genreId2) {
        return String.format("{\"name\":\"%s\"," +
                        "\"description\":\"%s\"," +
                        "\"releaseDate\":\"%s\"," +
                        "\"duration\":%d," +
                        "\"mpa\":{\"id\":%d}," +
                        "\"genres\":[{\"id\":%d},{\"id\": %d}]}",
                name, description, releaseDate, duration, mpaId, genreId1, genreId2);
    }
}
