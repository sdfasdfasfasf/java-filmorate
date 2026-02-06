package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class FilmGenre {
    @NotNull(message = "ID фильма не может быть null")
    private Long filmId;

    @NotNull(message = "ID жанра не может быть null")
    private Integer genreId;

    public FilmGenre() {
    }

    public FilmGenre(Long filmId, Integer genreId) {
        this.filmId = filmId;
        this.genreId = genreId;
    }
}
