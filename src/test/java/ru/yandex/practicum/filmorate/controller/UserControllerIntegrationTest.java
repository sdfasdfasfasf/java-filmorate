package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;
    private User testUser3;

    @BeforeEach
    void setUp() {
        testUser1 = createTestUser("user1@mail.com", "user1", "User One");
        testUser2 = createTestUser("user2@mail.com", "user2", "User Two");
        testUser3 = createTestUser("user3@mail.com", "user3", "User Three");
    }

    @Test
    void shouldCreateUser() throws Exception {
        // When & Then
        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.email", is("user1@mail.com")))
                .andExpect(jsonPath("$.login", is("user1")))
                .andExpect(jsonPath("$.name", is("User One")))
                .andExpect(jsonPath("$.birthday", is("1990-01-01")));
    }

    @Test
    void shouldUpdateUser() throws Exception {
        String createResponse = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(createResponse, User.class);

        createdUser.setName("Updated Name");
        createdUser.setEmail("updated@mail.com");
        createdUser.setLogin("updatedLogin");

        mockMvc.perform(put("/users")
                        .content(objectMapper.writeValueAsString(createdUser))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdUser.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated@mail.com")))
                .andExpect(jsonPath("$.login", is("updatedLogin")));
    }

    @Test
    void shouldGetUserById() throws Exception {
        String createResponse = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(createResponse, User.class);

        mockMvc.perform(get("/users/{id}", createdUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdUser.getId().intValue())))
                .andExpect(jsonPath("$.email", is("user1@mail.com")))
                .andExpect(jsonPath("$.login", is("user1")));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(testUser1))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/users")
                .content(objectMapper.writeValueAsString(testUser2))
                .contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].email", is("user1@mail.com")))
                .andExpect(jsonPath("$[1].email", is("user2@mail.com")));
    }

    @Test
    void shouldAddAndRemoveFriend() throws Exception {
        String user1Response = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String user2Response = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User user1 = objectMapper.readValue(user1Response, User.class);
        User user2 = objectMapper.readValue(user2Response, User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user2.getId().intValue())));

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}/friends", user1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldGetCommonFriends() throws Exception {
        String user1Response = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String user2Response = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser2))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String user3Response = mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(testUser3))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User user1 = objectMapper.readValue(user1Response, User.class);
        User user2 = objectMapper.readValue(user2Response, User.class);
        User user3 = objectMapper.readValue(user3Response, User.class);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", user1.getId(), user3.getId()));
        mockMvc.perform(put("/users/{id}/friends/{friendId}", user2.getId(), user3.getId()));

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", user1.getId(), user2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(user3.getId().intValue())));
    }

    @Test
    void shouldReturnValidationErrorForInvalidEmail() throws Exception {
        User user = createTestUser("invalid-email", "login", "Name");

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("email")));
    }

    @Test
    void shouldReturnValidationErrorForEmptyLogin() throws Exception {
        User user = createTestUser("test@mail.com", "", "Name");

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("login")));
    }

    @Test
    void shouldReturnValidationErrorForLoginWithSpaces() throws Exception {
        User user = createTestUser("test@mail.com", "login with spaces", "Name");

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("пробелы")));
    }

    @Test
    void shouldReturnValidationErrorForFutureBirthday() throws Exception {
        User user = createTestUser("test@mail.com", "login", "Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("будущем")));
    }

    @Test
    void shouldHandleFriendOperationsWithNonExistentUsers() throws Exception {
        mockMvc.perform(put("/users/999/friends/888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("не найден")));

        mockMvc.perform(get("/users/999/friends"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("не найден")));

        mockMvc.perform(get("/users/999/friends/common/888"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("не найден")));
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}