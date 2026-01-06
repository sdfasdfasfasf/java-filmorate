package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films - создание фильма: {}", film.getName());
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("PUT /films - обновление фильма с ID: {}", film.getId());
        return filmService.update(film);
    }

    @GetMapping
    public List<Film> getAll() {
        log.info("GET /films - получение всех фильмов");
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getById(@PathVariable Long id) {
        log.info("GET /films/{} - получение фильма по ID", id);
        return filmService.getById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT /films/{}/like/{} - добавление лайка", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("DELETE /films/{}/like/{} - удаление лайка", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("GET /films/popular?count={} - получение популярных фильмов", count);
        return filmService.getPopular(count);
    }
}