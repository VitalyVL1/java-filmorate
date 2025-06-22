package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    public MpaService(@Qualifier("mpaStorageAlias") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    public Mpa create(Mpa mpa) {
        if (containsName(mpa)) {
            throw new DuplicatedDataException("Такой рейтинг уже существует");
        }
        return mpaStorage.create(mpa);
    }

    public Mpa findById(Integer id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }

    public Collection<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    public Mpa update(Mpa newMpa) {
        Mpa oldMpa = findById(newMpa.getId());

        if (newMpa.getName() != null &&
                !newMpa.getName().equals(oldMpa.getName()) &&
                containsName(newMpa)) {
            throw new DuplicatedDataException("Такой рейтинг уже существует");
        }

        return mpaStorage.update(newMpa)
                .orElseThrow(
                        () -> new NotFoundException("Рейтинг с id = " + newMpa.getId() + " не найден")
                );
    }

    private boolean containsName(Mpa mpa) {
        return mpaStorage.findAll().stream()
                .map(Mpa::getName)
                .anyMatch(mpa.getName()::equals);
    }
}
