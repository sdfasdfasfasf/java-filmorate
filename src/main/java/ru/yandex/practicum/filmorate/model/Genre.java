package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class Genre {
    @NotNull(message = "ID жанра не может быть null")
    private Integer id;

    @NotBlank(message = "Название жанра не может быть пустым")
    @Size(max = 50, message = "Название жанра не может превышать 50 символов")
    private String name;

    public Genre() {
    }

    public Genre(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}