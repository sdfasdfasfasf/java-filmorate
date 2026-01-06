package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long userId = 1L;

    @Override
    public List<User> getAll() {
        log.info("Получен запрос на получение всех пользователей. Количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public User create(User user) {
        user.setId(userId++);
        users.put(user.getId(), user);
        log.info("Добавлен пользователь: {} с ID: {}", user.getLogin(), user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            log.error("Пользователь с ID {} не найден", user.getId());
            throw new NotFoundException("Пользователь с таким id не найден");
        }
        users.put(user.getId(), user);
        log.info("Обновлен пользователь с ID: {}", user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        user.addFriend(friendId);
        friend.addFriend(userId);

        log.info("Пользователь {} и пользователь {} теперь друзья", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        log.info("Пользователь {} и пользователь {} больше не друзья", userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = getUser(userId);
        return user.getFriends().stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user1 = getUser(userId);
        User user2 = getUser(otherId);

        Set<Long> commonFriendIds = new HashSet<>(user1.getFriends());
        commonFriendIds.retainAll(user2.getFriends());

        return commonFriendIds.stream()
                .map(this::getUser)
                .collect(Collectors.toList());
    }

    private User getUser(Long id) {
        return getById(id).orElseThrow(() -> {
            log.error("Пользователь с ID {} не найден", id);
            return new NotFoundException("Пользователь с таким id не найден");
        });
    }
}