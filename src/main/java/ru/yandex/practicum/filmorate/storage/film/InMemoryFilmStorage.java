package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long filmId = 1L;

    @Override
    public List<Film> getAll() {
        log.info("Получен запрос на получение всех фильмов. Количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Film create(Film film) {
        film.setId(filmId++);
        films.put(film.getId(), film);
        log.info("Добавлен фильм: {} с ID: {}", film.getName(), film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с ID {} не найден", film.getId());
            throw new NotFoundException("Фильм с таким id не найден");
        }
        films.put(film.getId(), film);
        log.info("Обновлен фильм с ID: {}", film.getId());
        return film;
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilm(filmId);
        film.addLike(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = getFilm(filmId);
        film.removeLike(userId);
        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    @Override
    public List<Film> getPopular(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikesCount(), f1.getLikesCount()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film getFilm(Long id) {
        return getById(id).orElseThrow(() -> {
            log.error("Фильм с ID {} не найден", id);
            return new NotFoundException("Фильм с таким id не найден");
        });
    }
}