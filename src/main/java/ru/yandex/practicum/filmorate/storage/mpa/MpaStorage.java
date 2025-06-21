package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

public interface MpaStorage {
    Mpa create(Mpa mpa);

    Optional<Mpa> findById(Integer id);

    Collection<Mpa> findAll();

    Optional<Mpa> update(Mpa newMpa);

    Optional<Mpa> removeById(Integer id);

}
