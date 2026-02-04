-- Очистка таблиц перед вставкой
DELETE FROM film_likes;
DELETE FROM friendships;
DELETE FROM film_genres;
DELETE FROM films;
DELETE FROM users;
DELETE FROM genres;
DELETE FROM mpa_ratings;
DELETE FROM friendship_statuses;

-- Сброс счетчиков ID (для H2)
ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE mpa_ratings ALTER COLUMN mpa_id RESTART WITH 1;
ALTER TABLE genres ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE friendship_statuses ALTER COLUMN status_id RESTART WITH 1;

-- Заполнение таблицы рейтингов MPA (американская система)
MERGE INTO mpa_ratings (mpa_id, name, description) VALUES
    (1, 'G', 'Нет возрастных ограничений'),
    (2, 'PG', 'Детям рекомендуется смотреть с родителями'),
    (3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
    (4, 'R', 'Лицам до 17 лет только в присутствии взрослого'),
    (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');

-- Заполнение таблицы жанров
MERGE INTO genres (genre_id, name) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

-- Заполнение таблицы статусов дружбы
MERGE INTO friendship_statuses (status_id, name) VALUES
    (1, 'неподтверждённая'),
    (2, 'подтверждённая');