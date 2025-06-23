package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.util.MpaUtil;

import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SpringBootTest
public class MpaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<Integer, Mpa> MPA_MAP = MpaUtil.fillMpa();

    @Test
    public void testFindAll() throws Exception {
        MvcResult result = mockMvc.perform(get("/mpa")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        Mpa[] mpaDb = MAPPER.readValue(result.getResponse().getContentAsString(), Mpa[].class);

        for (int i = 0; i < mpaDb.length; i++) {
            assertEquals(mpaDb[i].getName(), MPA_MAP.get(i + 1).getName());
            assertEquals(mpaDb[i].getDescription(), MPA_MAP.get(i + 1).getDescription());
        }
    }

    @Test
    public void testFindById_ok() throws Exception {
        mockMvc.perform(get("/mpa/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(MPA_MAP.get(1).getName())))
                .andExpect(jsonPath("$.description", is(MPA_MAP.get(1).getDescription())));
    }

    @Test
    public void testFindById_notFound() throws Exception {
        mockMvc.perform(get("/mpa/1000")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
