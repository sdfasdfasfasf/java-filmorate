package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
public class Friendship {
    @NotNull(message = "ID пользователя не может быть null")
    private Long userId;

    @NotNull(message = "ID друга не может быть null")
    private Long friendId;

    @NotNull(message = "Статус дружбы не может быть null")
    private Integer statusId;

    public Friendship() {
    }

    public Friendship(Long userId, Long friendId, Integer statusId) {
        this.userId = userId;
        this.friendId = friendId;
        this.statusId = statusId;
    }

    // Статусы дружбы
    public static class Status {
        public static final Integer PENDING = 1;    // Неподтверждённая
        public static final Integer CONFIRMED = 2;  // Подтверждённая
    }
}