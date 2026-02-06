package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class FilmLike {
    @NotNull(message = "ID фильма не может быть null")
    private Long filmId;

    @NotNull(message = "ID пользователя не может быть null")
    private Long userId;

    public FilmLike() {
    }

    public FilmLike(Long filmId, Long userId) {
        this.filmId = filmId;
        this.userId = userId;
    }
}