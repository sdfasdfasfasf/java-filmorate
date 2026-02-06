package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.BaseDbStorageTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(UserDbStorage.class)
class UserDbStorageTest extends BaseDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void testCreateAndGetUserById() {
        User user = createTestUser("test@mail.com", "testLogin");

        User createdUser = userStorage.create(user);
        Optional<User> foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@mail.com");
        assertThat(foundUser.get().getLogin()).isEqualTo("testLogin");
        assertThat(foundUser.get().getName()).isEqualTo("Test Name");
    }

    @Test
    void testGetAllUsers() {
        User user1 = createTestUser("user1@mail.com", "login1");
        User user2 = createTestUser("user2@mail.com", "login2");

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.getAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@mail.com", "user2@mail.com");
    }

    @Test
    void testUpdateUser() {
        User user = createTestUser("old@mail.com", "oldLogin");
        User createdUser = userStorage.create(user);

        createdUser.setEmail("new@mail.com");
        createdUser.setLogin("newLogin");
        createdUser.setName("New Name");
        User updatedUser = userStorage.update(createdUser);

        Optional<User> foundUser = userStorage.getById(createdUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("new@mail.com");
        assertThat(foundUser.get().getLogin()).isEqualTo("newLogin");
        assertThat(foundUser.get().getName()).isEqualTo("New Name");
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = createTestUser("user1@mail.com", "login1");
        User user2 = createTestUser("user2@mail.com", "login2");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        List<User> friends = userStorage.getFriends(createdUser1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(createdUser2.getId());

        userStorage.removeFriend(createdUser1.getId(), createdUser2.getId());
        friends = userStorage.getFriends(createdUser1.getId());

        assertThat(friends).isEmpty();
    }

    @Test
    void testGetCommonFriends() {
        User user1 = createTestUser("user1@mail.com", "login1");
        User user2 = createTestUser("user2@mail.com", "login2");
        User user3 = createTestUser("user3@mail.com", "login3");

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);

        userStorage.addFriend(createdUser1.getId(), createdUser3.getId());
        userStorage.addFriend(createdUser2.getId(), createdUser3.getId());

        List<User> commonFriends = userStorage.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(createdUser3.getId());
    }

    @Test
    void testGetUserByIdWhenNotExists() {
        Optional<User> user = userStorage.getById(999L);

        assertThat(user).isEmpty();
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}
