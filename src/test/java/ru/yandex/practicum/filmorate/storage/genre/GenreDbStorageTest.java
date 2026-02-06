package ru.yandex.practicum.filmorate.storage.genre;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.BaseDbStorageTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(GenreDbStorage.class)
class GenreDbStorageTest extends BaseDbStorageTest {

    @Autowired
    private GenreDbStorage genreStorage;

    @Test
    void testFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).hasSize(6); // Из data.sql: 6 жанров
        assertThat(genres).extracting(Genre::getName)
                .contains("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void testFindGenreById() {
        Optional<Genre> genre = genreStorage.findById(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }

    @Test
    void testFindGenreByIdWhenNotExists() {
        Optional<Genre> genre = genreStorage.findById(999);

        assertThat(genre).isEmpty();
    }

    @Test
    void testFindGenresByFilmId() {
        List<Genre> genres = genreStorage.findByFilmId(999L); // Несуществующий фильм

        assertThat(genres).isEmpty();
    }
}