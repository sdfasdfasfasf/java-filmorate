package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<Mpa>> getAllMpa() {
        log.info("Запрос на получение всех рейтингов MPA");
        List<Mpa> mpas = mpaService.findAll();
        return ResponseEntity.ok(mpas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getGenreById(@PathVariable Integer id) {
        log.info("Запрос на получение Mpa с id: {}", id);
        Mpa mpa = mpaService.findById(id);
        return ResponseEntity.ok(mpa);
    }
}