package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long userId = 1L;

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        user.setId(userId++);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.error("Пользователь с id={} не найден", user.getId());
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь с id={}", user.getId());
        return user;
    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен запрос на получение всех пользователей. Количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    private void validateUser(User user) {
        if (user.getBirthday() == null) {
            log.error("Дата рождения не указана");
            throw new ValidationException("Дата рождения обязательна");
        }

        if (user.getBirthday().isAfter(java.time.LocalDate.now())) {
            log.error("Дата рождения {} находится в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}