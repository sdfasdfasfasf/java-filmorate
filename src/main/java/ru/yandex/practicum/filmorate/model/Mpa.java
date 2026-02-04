package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
public class Mpa {
    @NotNull(message = "ID рейтинга MPA не может быть null")
    private Integer id;

    @Size(max = 10, message = "Название рейтинга MPA не может превышать 10 символов")
    private String name;

    @Size(max = 255, message = "Описание рейтинга MPA не может превышать 255 символов")
    private String description;

    public Mpa() {
    }

    public Mpa(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Метод для получения американской системы рейтингов
    public static String getAmericanName(Integer id) {
        switch (id) {
            case 1: return "G";
            case 2: return "PG";
            case 3: return "PG-13";
            case 4: return "R";
            case 5: return "NC-17";
            default: throw new IllegalArgumentException("Неизвестный ID рейтинга MPA: " + id);
        }
    }

    // Метод для получения российских названий (если нужно)
    public static String getRussianName(Integer id) {
        switch (id) {
            case 1: return "0+";
            case 2: return "6+";
            case 3: return "12+";
            case 4: return "16+";
            case 5: return "18+";
            default: throw new IllegalArgumentException("Неизвестный ID рейтинга MPA: " + id);
        }
    }
}