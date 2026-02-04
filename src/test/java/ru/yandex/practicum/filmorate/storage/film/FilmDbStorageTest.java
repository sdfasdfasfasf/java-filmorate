package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.BaseDbStorageTest;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
class FilmDbStorageTest extends BaseDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private GenreDbStorage genreStorage;

    private Mpa testMpa;
    private Genre testGenre;

    @BeforeEach
    void setUp() {
        testMpa = mpaStorage.findById(1).orElseThrow();
        testGenre = genreStorage.findById(1).orElseThrow();
    }

    @Test
    void testCreateAndGetFilmById() {
        Film film = createTestFilm("Test Film");

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Test Film");
        assertThat(foundFilm.get().getDescription()).isEqualTo("Test Description");
        assertThat(foundFilm.get().getMpa().getId()).isEqualTo(testMpa.getId());
    }

    @Test
    void testGetAllFilms() {
        Film film1 = createTestFilm("Film 1");
        Film film2 = createTestFilm("Film 2");

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.getAll();

        assertThat(films).hasSize(2);
        assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder("Film 1", "Film 2");
    }

    @Test
    void testUpdateFilm() {
        Film film = createTestFilm("Old Title");
        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("New Title");
        createdFilm.setDescription("New Description");
        Film updatedFilm = filmStorage.update(createdFilm);

        Optional<Film> foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("New Title");
        assertThat(foundFilm.get().getDescription()).isEqualTo("New Description");
    }

    @Test
    void testFilmWithGenres() {
        Film film = createTestFilm("Film with Genres");
        film.setGenres(List.of(testGenre));

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = filmStorage.getById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getGenres()).hasSize(1);
        assertThat(foundFilm.get().getGenres().get(0).getId()).isEqualTo(testGenre.getId());
    }

    @Test
    void testGetFilmByIdWhenNotExists() {
        Optional<Film> film = filmStorage.getById(999L);

        assertThat(film).isEmpty();
    }

    private Film createTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(testMpa);
        return film;
    }
}
