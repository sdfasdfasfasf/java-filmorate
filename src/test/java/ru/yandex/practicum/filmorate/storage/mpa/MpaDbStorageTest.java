package ru.yandex.practicum.filmorate.storage.mpa;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseDbStorageTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(MpaDbStorage.class)
class MpaDbStorageTest extends BaseDbStorageTest {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Test
    void testFindAllMpa() {
        List<Mpa> mpaList = mpaStorage.findAll();

        assertThat(mpaList).hasSize(5); // Из data.sql: 5 рейтингов MPA
        assertThat(mpaList).extracting(Mpa::getName)
                .contains("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testFindMpaById() {
        Optional<Mpa> mpa = mpaStorage.findById(1);

        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
        assertThat(mpa.get().getDescription()).isEqualTo("Нет возрастных ограничений");
    }

    @Test
    void testFindMpaByIdWhenNotExists() {
        Optional<Mpa> mpa = mpaStorage.findById(999);

        assertThat(mpa).isEmpty();
    }
}