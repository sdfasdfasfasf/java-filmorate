package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User create(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.create(user);
    }

    public User update(User user) {
        validateUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.update(user);
    }

    public User getById(Long id) {
        return userStorage.getById(id)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден", id);
                    return new NotFoundException("Пользователь с таким id не найден");
                });
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        // Проверяем, существует ли пользователь
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при добавлении друга", userId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        // Проверяем, существует ли друг
        userStorage.getById(friendId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при добавлении друга", friendId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        // Проверяем, существует ли пользователь
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при удалении друга", userId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        // Проверяем, существует ли друг
        userStorage.getById(friendId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при удалении друга", friendId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        // Проверяем, существует ли пользователь
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при получении списка друзей", userId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        // Проверяем, существует ли первый пользователь
        userStorage.getById(userId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при поиске общих друзей", userId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        // Проверяем, существует ли второй пользователь
        userStorage.getById(otherId)
                .orElseThrow(() -> {
                    log.error("Пользователь с ID {} не найден при поиске общих друзей", otherId);
                    return new NotFoundException("Пользователь с таким id не найден");
                });

        return userStorage.getCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}