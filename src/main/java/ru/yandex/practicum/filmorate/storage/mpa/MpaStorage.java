package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {
    Mpa create(Mpa mpa);

    Optional<Mpa> findById(Long id);

    Collection<Mpa> findAll();

    Optional<Mpa> update(Mpa newMpa);

    Optional<Mpa> removeById(Long id);

}
