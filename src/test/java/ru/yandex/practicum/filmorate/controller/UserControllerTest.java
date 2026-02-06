package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateValidUser() throws Exception {
        User user = new User();
        user.setEmail("testt@mail.com");
        user.setLogin("testLogint");
        user.setName("Test Namet");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(jsonPath("$.login").value("testLogint"));
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() throws Exception {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() throws Exception {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithEmptyLogin() throws Exception {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateUserWithLoginContainingSpaces() throws Exception {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("log in");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/users").content(objectMapper.writeValueAsString(user)).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
    }
}