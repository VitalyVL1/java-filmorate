package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.User;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.LocalDate;

@AutoConfigureTestDatabase
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WebMvcTest(UserController.class)
@ComponentScan(basePackages = "ru.yandex.practicum.filmorate")
public class UserControllerTest {

    private static final String CORRECT_EMAIL = "user1@email.com";
    private static final String CORRECT_LOGIN = "User_login1";
    private static final String CORRECT_NAME = "User_name1";
    private static final LocalDate CORRECT_BIRTHDAY = LocalDate.of(1900, 1, 1);

    private static final String UPDATE_EMAIL = "update_user1@email.com";
    private static final String UPDATE_LOGIN = "update_User_login1";
    private static final String UPDATE_NAME = "update_User_name1";
    private static final LocalDate UPDATE_BIRTHDAY = LocalDate.of(2000, 1, 1);

    private static final String INCORRECT_EMAIL = "1234abc";
    private static final String INCORRECT_LOGIN = "Us er";
    private static final LocalDate INCORRECT_BIRTHDAY = LocalDate.of(3000, 1, 1);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void addUser_allFields_ok() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        User responseUser = MAPPER.readValue(result.getResponse().getContentAsString(), User.class);

        assertEquals(CORRECT_NAME, responseUser.getName());
        assertEquals(CORRECT_EMAIL, responseUser.getEmail());
        assertEquals(CORRECT_BIRTHDAY, responseUser.getBirthday());
        assertEquals(CORRECT_LOGIN, responseUser.getLogin());
        assertEquals(1, responseUser.getId());
    }

    @Test
    @Order(2)
    void updateUser_allFields_ok() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName(UPDATE_NAME);
        user.setEmail(UPDATE_EMAIL);
        user.setBirthday(UPDATE_BIRTHDAY);
        user.setLogin(UPDATE_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        MvcResult result = mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andReturn();

        User responseUser = MAPPER.readValue(result.getResponse().getContentAsString(), User.class);

        assertEquals(UPDATE_NAME, responseUser.getName());
        assertEquals(UPDATE_EMAIL, responseUser.getEmail());
        assertEquals(UPDATE_BIRTHDAY, responseUser.getBirthday());
        assertEquals(UPDATE_LOGIN, responseUser.getLogin());
        assertEquals(1, responseUser.getId());
    }

    @Test
    @Order(3)
    void addUser_noName_ok() throws Exception {
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andReturn();

        User responseUser = MAPPER.readValue(result.getResponse().getContentAsString(), User.class);

        assertEquals(CORRECT_LOGIN, responseUser.getName());
        assertEquals(CORRECT_EMAIL, responseUser.getEmail());
        assertEquals(CORRECT_BIRTHDAY, responseUser.getBirthday());
        assertEquals(CORRECT_LOGIN, responseUser.getLogin());
        assertEquals(2, responseUser.getId());
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
        User user = new User();
        user.setName("user_name");
        user.setEmail("user@email.com");
        user.setBirthday(LocalDate.of(1900, 1, 1));
        user.setLogin("user_login");

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

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
    void addUser_incorrectEmail_badRequest() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setEmail(INCORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_incorrectLogin_badRequest() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(INCORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_incorrectBirthday_badRequest() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(INCORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_blankEmail_badRequest() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);
        user.setEmail("");

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_blankLogin_badRequest() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin("");
        user.setEmail(CORRECT_EMAIL);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_notFound() throws Exception {
        User user = new User();
        user.setId(1000L);
        user.setName(UPDATE_NAME);
        user.setEmail("ivan1000@gmail.com");
        user.setBirthday(UPDATE_BIRTHDAY);
        user.setLogin(UPDATE_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_noId_badRequest() throws Exception {
        User user = new User();
        user.setName(UPDATE_NAME);
        user.setEmail("ivan1000@gmail.com");
        user.setBirthday(UPDATE_BIRTHDAY);
        user.setLogin(UPDATE_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

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
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_duplicatedEmail_badRequest() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isBadRequest());
    }
}
