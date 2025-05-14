package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import java.time.LocalDate;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    private static final String CORRECT_EMAIL = "ivan@gmail.com";
    private static final String CORRECT_LOGIN = "Vanya";
    private static final String CORRECT_NAME = "Ivan";
    private static final LocalDate CORRECT_BIRTHDAY = LocalDate.of(1900, 1, 1);

    private static final String UPDATE_EMAIL = "updateivan@gmail.com";
    private static final String UPDATE_LOGIN = "updateVanya";
    private static final String UPDATE_NAME = "updateIvan";
    private static final LocalDate UPDATE_BIRTHDAY = LocalDate.of(2000, 1, 1);

    private static final String INCORRECT_EMAIL = "1234abc";
    private static final String INCORRECT_LOGIN = "Van ya";
    private static final LocalDate INCORRECT_BIRTHDAY = LocalDate.of(3000, 1, 1);

    private static final ObjectMapper MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void addUser_allFieldsOk() throws Exception {
        User user = new User();
        user.setName(CORRECT_NAME);
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
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
    void updateUser_allFieldsOk() throws Exception {
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
    void addUser_noName() throws Exception {
        User user = new User();
        user.setEmail(CORRECT_EMAIL);
        user.setBirthday(CORRECT_BIRTHDAY);
        user.setLogin(CORRECT_LOGIN);

        String userJson = MAPPER.writeValueAsString(user);

        MvcResult result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
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
    void getUsers() throws Exception {
        MvcResult result = mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        User[] users = MAPPER.readValue(result.getResponse().getContentAsString(), User[].class);

        assertEquals(2, users.length);
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
}
