package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final String CORRECT_LOGIN = "Login";
    private static final String CORRECT_NAME = "Name";
    private static final String CORRECT_EMAIL = "email@email.com";
    private static final LocalDate CORRECT_BIRTHDAY = LocalDate.of(1986, 10, 2);

    private static final String UPDATE_LOGIN = "update_login";
    private static final String UPDATE_NAME = "update_name";
    private static final String UPDATE_EMAIL = "update_email@email.com";
    private static final LocalDate UPDATE_BIRTHDAY = LocalDate.of(1989, 6, 24);

    private static final String INCORRECT_EMAIL = "1234abc";
    private static final String INCORRECT_LOGIN = "Us er";
    private static final LocalDate INCORRECT_BIRTHDAY = LocalDate.of(3000, 1, 1);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    @Order(1)
    void addUser_allFields_ok() throws Exception {
        String userJson = jsonString(
                CORRECT_LOGIN,
                CORRECT_NAME,
                CORRECT_EMAIL,
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login", is(CORRECT_LOGIN)))
                .andExpect(jsonPath("$.name", is(CORRECT_NAME)))
                .andExpect(jsonPath("$.email", is(CORRECT_EMAIL)))
                .andExpect(jsonPath("$.birthday", is(CORRECT_BIRTHDAY.toString())));
    }

    @Test
    @Order(2)
    void updateUser_allFields_ok() throws Exception {
        String userJson = jsonUpdateString(
                1,
                UPDATE_LOGIN,
                UPDATE_NAME,
                UPDATE_EMAIL,
                UPDATE_BIRTHDAY
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login", is(UPDATE_LOGIN)))
                .andExpect(jsonPath("$.name", is(UPDATE_NAME)))
                .andExpect(jsonPath("$.email", is(UPDATE_EMAIL)))
                .andExpect(jsonPath("$.birthday", is(UPDATE_BIRTHDAY.toString())));
    }

    @Test
    @Order(3)
    void addUser_noName_ok() throws Exception {
        String userJson = jsonString(
                CORRECT_LOGIN,
                "",
                CORRECT_EMAIL,
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.login", is(CORRECT_LOGIN)))
                .andExpect(jsonPath("$.name", is(CORRECT_LOGIN)))
                .andExpect(jsonPath("$.email", is(CORRECT_EMAIL)))
                .andExpect(jsonPath("$.birthday", is(CORRECT_BIRTHDAY.toString())));
    }

    @Test
    @Order(4)
    void getUsers_ok() throws Exception {
        MvcResult result = mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        User[] users = MAPPER.readValue(result.getResponse().getContentAsString(), User[].class);

        assertEquals(2, users.length);
    }

    @Test
    @Order(5)
    void addFriend_status_confirmed_ok() throws Exception {
        mockMvc.perform(put("/users/1/friends/2?status=CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.friends", hasKey("2")));

        MvcResult result = mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        User[] users = MAPPER.readValue(result.getResponse().getContentAsString(), User[].class);

        assertTrue(users[0].getFriends().containsKey(2L));
        assertTrue(users[1].getFriends().containsKey(1L));
    }

    @Test
    @Order(6)
    void findFriends_ok() throws Exception {
        MvcResult result = mockMvc.perform(get("/users/1/friends")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        User[] friends = MAPPER.readValue(result.getResponse().getContentAsString(), User[].class);

        assertEquals(2, friends[0].getId());
    }

    @Test
    @Order(7)
    void deleteFriend_ok() throws Exception {
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.friends", anEmptyMap()));

        MvcResult result = mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        User[] users = MAPPER.readValue(result.getResponse().getContentAsString(), User[].class);

        assertFalse(users[0].getFriends().containsKey(users[1].getId()));
    }

    @Test
    void getUserById_ok() throws Exception {
        mockMvc.perform(get("/users/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    void findCommonFriends_ok() throws Exception {
        String userJson = jsonString(
                "Login1",
                "Name1",
                "email1@email.com",
                LocalDate.of(1984, 5, 6)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(delete("/users/2/friends/1"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/1/friends/2?status=CONFIRMED"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/3/friends/2?status=CONFIRMED"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/3/friends/common/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].friends", hasKey("1")))
                .andExpect(jsonPath("$[0].friends", hasKey("3")));
    }

    @Test
    void addUser_failEmail_badRequest() throws Exception {
        String userJson = jsonString(
                CORRECT_LOGIN,
                CORRECT_NAME,
                INCORRECT_EMAIL,
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_failLogin_badRequest() throws Exception {
        String userJson = jsonString(
                INCORRECT_LOGIN,
                CORRECT_NAME,
                CORRECT_EMAIL,
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_failBirthday_badRequest() throws Exception {
        String userJson = jsonString(
                CORRECT_LOGIN,
                CORRECT_NAME,
                CORRECT_EMAIL,
                INCORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_blankEmail_badRequest() throws Exception {
        String userJson = jsonString(
                CORRECT_LOGIN,
                CORRECT_NAME,
                "",
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_blankLogin_badRequest() throws Exception {
        String userJson = jsonString(
                "",
                CORRECT_NAME,
                CORRECT_EMAIL,
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_notFound() throws Exception {
        String userJson = jsonUpdateString(
                1000,
                UPDATE_LOGIN,
                UPDATE_NAME,
                UPDATE_EMAIL,
                UPDATE_BIRTHDAY
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_noId_badRequest() throws Exception {
        String userJson = jsonString(
                CORRECT_LOGIN,
                CORRECT_NAME,
                "email2@email.com",
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUnknownFriend_badRequest() throws Exception {
        mockMvc.perform(put("/users/1/friends/1001"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findUnknownFriends_notFound() throws Exception {
        mockMvc.perform(get("/users/1000/friends")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserById_notFound() throws Exception {
        mockMvc.perform(get("/users/1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void addUser_duplicatedEmail_badRequest() throws Exception {
        String userJson = jsonString(
                "Login1",
                CORRECT_NAME,
                CORRECT_EMAIL,
                CORRECT_BIRTHDAY
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_duplicatedEmail_badRequest() throws Exception {
        String userJson = jsonUpdateString(
                1,
                UPDATE_LOGIN,
                UPDATE_NAME,
                CORRECT_EMAIL,
                UPDATE_BIRTHDAY
        );

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    private String jsonUpdateString(int id, String login, String name, String email, LocalDate birthday) {
        return String.format("{\"id\":%d," +
                        "\"login\":\"%s\"," +
                        "\"name\":\"%s\"," +
                        "\"email\":\"%s\"," +
                        "\"birthday\":\"%s\"}",
                id, login, name, email, birthday);
    }

    private String jsonString(String login, String name, String email, LocalDate birthday) {
        return String.format("{\"login\":\"%s\"," +
                        "\"name\":\"%s\"," +
                        "\"email\":\"%s\"," +
                        "\"birthday\":\"%s\"}",
                login, name, email, birthday);
    }
}
