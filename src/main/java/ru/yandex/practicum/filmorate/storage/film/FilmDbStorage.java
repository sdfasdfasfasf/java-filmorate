package ru.yandex.practicum.filmorate.storage.film;

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
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private RowMapper<Film> getFilmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            Mpa mpa = mpaStorage.findById(rs.getInt("mpa_id")).orElse(null);
            film.setMpa(mpa);

            return film;
        };
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.* FROM films f ORDER BY f.film_id";
        log.debug("Выполняется запрос на получение всех фильмов");
        List<Film> films = jdbcTemplate.query(sql, getFilmRowMapper());

        // Загружаем жанры и лайки для каждого фильма
        films.forEach(this::loadFilmAdditionalData);

        return films;
    }

    @Override
    public Film create(Film film) {
        List<Genre> genres = film.getGenres();

        if(genres != null) {
            if(genres.size() > 0) {
                List<Genre> uniqueGenres = genres.stream()
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(
                                        Genre::getId, // Ключ - поле для уникальности
                                        genre -> genre,
                                        (existing, replacement) -> existing // При конфликте берём существующий
                                ),
                                map -> new ArrayList<>(map.values())
                        ));

                film.setGenres(uniqueGenres);

                for (int i = 0; i < uniqueGenres.size(); i++) {
                    Integer genreId = uniqueGenres.get(i).getId();

                    if(genreId != null) {
                        Optional<Genre> genre = genreStorage.findById(genreId);
                        if (genre.isEmpty()) {
                            throw new NotFoundException("Жанр с id=" + genreId + " не найден");
                        }
                    }
                    else {
                        throw new NotFoundException("Жанр с id=" + genreId + " не найден");
                    }
                }
            }
        }

        if(film.getReleaseDate().isBefore(LocalDate.of(1895, 1, 1))) {
            throw new ValidationException("Фильма с датой выпуска =" + film.getReleaseDate() + " нет");
        }

        Optional<Mpa> mpa = mpaStorage.findById(film.getMpaId());
        if (mpa.isEmpty()) {
            throw new NotFoundException("Рейтинг с id=" + film.getMpaId() + " не найден");
        }

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpaId());
            return stmt;
        }, keyHolder);

        Long filmId = keyHolder.getKey().longValue();
        film.setId(filmId);

        // Сохраняем жанры
        saveFilmGenres(film);

        // Загружаем полные данные
        loadFilmAdditionalData(film);

        log.info("Создан новый фильм: {} с id: {}", film.getName(), filmId);
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!existsById(film.getId())) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_id = ? WHERE film_id = ?";

        int rowsUpdated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaId(),
                film.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Фильм с id {} не найден для обновления", film.getId());
            throw new RuntimeException("Фильм не найден");
        }

        // Обновляем жанры
        updateFilmGenres(film);

        // Загружаем обновленные данные
        loadFilmAdditionalData(film);

        log.info("Обновлен фильм с id: {}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> getById(Long id) {
        String sql = "SELECT f.* FROM films f WHERE f.film_id = ?";
        try {
            log.debug("Выполняется запрос на получение фильма с id: {}", id);
            Film film = jdbcTemplate.queryForObject(sql, getFilmRowMapper(), id);

            if (film != null) {
                loadFilmAdditionalData(film);
            }

            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с id {} не найден", id);
            return Optional.empty();
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        log.debug("Добавление лайка фильму {} от пользователя {}", filmId, userId);
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        log.debug("Удаление лайка у фильма {} от пользователя {}", filmId, userId);
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        log.debug("Выполняется запрос на получение {} популярных фильмов", count);
        List<Film> films = jdbcTemplate.query(sql, getFilmRowMapper(), count);

        // Загружаем дополнительные данные для каждого фильма
        films.forEach(this::loadFilmAdditionalData);

        return films;
    }

    private void loadFilmAdditionalData(Film film) {
        // Загружаем жанры
        List<Genre> genres = genreStorage.findByFilmId(film.getId());
        film.setGenres(genres);

        // Загружаем лайки
        String likesSql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            film.getGenres().forEach(genre -> {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            });
        }
    }

    private void updateFilmGenres(Film film) {
        // Удаляем старые жанры
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());

        // Добавляем новые жанры
        saveFilmGenres(film);
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }
}