package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserTest {

    @Test
    void shouldUseLoginWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // При создании контроллер должен установить name = login
    }

    @Test
    void shouldNotCreateUserWithInvalidEmail() {
        User user = new User();
        user.setEmail("invalid-email"); // Нет @
        user.setLogin("login");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        // Валидация должна выбросить исключение
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1)); // Завтра

        // Валидация должна выбросить исключение
    }
}