package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    @Override
    public List<User> getAll() {
        String sql = "SELECT u.* FROM users u ORDER BY u.user_id";
        log.debug("Выполняется запрос на получение всех пользователей");
        List<User> users = jdbcTemplate.query(sql, userRowMapper);

        // Загружаем друзей для каждого пользователя
        users.forEach(this::loadUserFriends);

        return users;
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        Long userId = keyHolder.getKey().longValue();
        user.setId(userId);

        log.info("Создан новый пользователь: {} с id: {}", user.getLogin(), userId);
        return user;
    }

    @Override
    public User update(User user) {
        if (!existsById(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?";

        int rowsUpdated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Пользователь с id {} не найден для обновления", user.getId());
            throw new RuntimeException("Пользователь не найден");
        }

        log.info("Обновлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(Long id) {
        String sql = "SELECT u.* FROM users u WHERE u.user_id = ?";
        try {
            log.debug("Выполняется запрос на получение пользователя с id: {}", id);
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);

            if (user != null) {
                loadUserFriends(user);
            }

            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        if (!existsById(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        String sql = "INSERT INTO friendships (user_id, friend_id, status_id) VALUES (?, ?, 1)";// +

        log.debug("Пользователь {} добавляет пользователя {} в друзья", userId, friendId);
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        if (!existsById(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        log.debug("Пользователь {} удаляет пользователя {} из друзей", userId, friendId);
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        // Получаем всех друзей пользователя (односторонняя дружба)
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? " +
                "ORDER BY u.user_id";

        log.debug("Выполняется запрос на получение друзей пользователя {}", userId);
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        if (!existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        if (!existsById(otherId)) {
            throw new NotFoundException("Пользователь с id=" + otherId + " не найден");
        }

        // Получаем общих друзей (тех, кого оба пользователя добавили в друзья)
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ? " +
                "ORDER BY u.user_id";

        log.debug("Выполняется запрос на получение общих друзей пользователей {} и {}", userId, otherId);
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }

    private void loadUserFriends(User user) {
        // Загружаем друзей пользователя (односторонняя дружба)
        String friendsSql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Long> friendIds = jdbcTemplate.queryForList(friendsSql, Long.class, user.getId());
        user.setFriends(new HashSet<>(friendIds));

        // Загружаем статусы дружбы
        loadFriendshipStatuses(user);
    }

    private void loadFriendshipStatuses(User user) {
        // Для каждого друга загружаем статус дружбы
        Map<Long, Integer> friendStatuses = new HashMap<>();

        String statusSql = "SELECT friend_id, status_id FROM friendships WHERE user_id = ?";
        jdbcTemplate.query(statusSql, rs -> {
            Long friendId = rs.getLong("friend_id");
            Integer statusId = rs.getInt("status_id");
            friendStatuses.put(friendId, statusId);
        }, user.getId());

        // Можно сохранить статусы в отдельные коллекции или в объект User
        // В данном примере просто логируем
        friendStatuses.forEach((friendId, statusId) -> {
            String status = statusId == 1 ? "неподтверждённая" : "подтверждённая";
            log.debug("Статус дружбы между {} и {}: {}", user.getId(), friendId, status);
        });
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    // Дополнительный метод для подтверждения дружбы
    public void confirmFriendship(Long userId, Long friendId) {
        String sql = "UPDATE friendships SET status_id = 2 WHERE user_id = ? AND friend_id = ?";
        log.debug("Подтверждение дружбы между {} и {}", friendId, userId);
        jdbcTemplate.update(sql, friendId, userId);
    }
}