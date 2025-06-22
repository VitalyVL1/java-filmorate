package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.util.MpaUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryMpaStorage implements MpaStorage {
    Map<Integer, Mpa> mpaMap = MpaUtil.fillMpa();

    @Override
    public Mpa create(Mpa mpa) {
        mpa.setId(getNextId());
        mpaMap.put(mpa.getId(), mpa);
        return mpa;
    }

    @Override
    public Optional<Mpa> findById(Integer id) {
        return Optional.ofNullable(mpaMap.get(id));
    }

    @Override
    public Collection<Mpa> findAll() {
        return mpaMap.values();
    }

    @Override
    public Optional<Mpa> update(Mpa newMpa) {
        if (!mpaMap.containsKey(newMpa.getId())) {
            return Optional.empty();
        }

        Mpa oldMpa = mpaMap.get(newMpa.getId());

        if (newMpa.getDescription() != null && !newMpa.getDescription().isBlank()) {
            oldMpa.setDescription(newMpa.getDescription());
        }

        if (newMpa.getName() != null && !newMpa.getName().isBlank()) {
            oldMpa.setName(newMpa.getName());
        }

        mpaMap.put(oldMpa.getId(), oldMpa);

        return Optional.of(oldMpa);
    }

    @Override
    public Optional<Mpa> removeById(Integer id) {
        return Optional.ofNullable(mpaMap.remove(id));
    }

    @Override
    public boolean contains(Mpa mpa) {
        return mpaMap.containsKey(mpa.getId());
    }

    private Integer getNextId() {
        int currentMaxId = mpaMap.keySet()
                .stream()
                .reduce(Integer::max)
                .orElse(0);
        return ++currentMaxId;
    }
}
