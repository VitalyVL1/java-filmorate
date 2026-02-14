package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.Collection;

/**
 * Сервисный слой для управления рейтингами MPA.
 * <p>
 * Реализует бизнес-логику работы со справочником возрастных рейтингов.
 * В приложении рейтинги MPA являются предопределенным справочником (1-5),
 * однако сервис предоставляет полный CRUD функционал для возможного расширения
 * и администрирования рейтингов в будущем.
 * </p>
 *
 * <p>Основные бизнес-правила:
 * <ul>
 *   <li>Название рейтинга должно быть уникальным</li>
 *   <li>Нельзя создать рейтинг с существующим названием</li>
 *   <li>При обновлении проверяется уникальность названия, если оно изменяется</li>
 * </ul>
 * </p>
 *
 * @see ru.yandex.practicum.filmorate.model.Mpa
 * @see ru.yandex.practicum.filmorate.storage.mpa.MpaStorage
 */
@Service
public class MpaService {
    private final MpaStorage mpaStorage;

    /**
     * Конструктор сервиса рейтингов MPA.
     *
     * @param mpaStorage хранилище рейтингов MPA (алиас из {@link ru.yandex.practicum.filmorate.config.AppConfig})
     */
    public MpaService(@Qualifier("mpaStorageAlias") MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    /**
     * Создает новый рейтинг MPA.
     * <p>
     * Перед созданием проверяет уникальность названия рейтинга.
     * </p>
     *
     * @param mpa объект рейтинга для создания
     * @return созданный рейтинг с автоматически сгенерированным идентификатором
     * @throws DuplicatedDataException если рейтинг с таким названием уже существует
     */
    public Mpa create(Mpa mpa) {
        if (containsName(mpa)) {
            throw new DuplicatedDataException("Такой рейтинг уже существует");
        }
        return mpaStorage.create(mpa);
    }

    /**
     * Находит рейтинг MPA по его идентификатору.
     *
     * @param id идентификатор рейтинга (от 1 до 5 для предопределенных значений)
     * @return найденный рейтинг
     * @throws ConditionsNotMetException если id равен null
     * @throws NotFoundException если рейтинг с указанным id не найден
     */
    public Mpa findById(Integer id) {
        if (id == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        return mpaStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден"));
    }

    /**
     * Возвращает список всех рейтингов MPA.
     *
     * @return коллекция всех рейтингов в системе
     */
    public Collection<Mpa> findAll() {
        return mpaStorage.findAll();
    }

    /**
     * Обновляет существующий рейтинг MPA.
     * <p>
     * Проверяет существование рейтинга и уникальность названия при его изменении.
     * </p>
     *
     * @param newMpa объект рейтинга с обновленными данными
     * @return обновленный рейтинг
     * @throws ConditionsNotMetException если id не указан
     * @throws NotFoundException если рейтинг с указанным id не найден
     * @throws DuplicatedDataException если новое название уже существует у другого рейтинга
     */
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

    /**
     * Проверяет, существует ли рейтинг с таким названием.
     * <p>
     * Вспомогательный метод для проверки уникальности названия при создании и обновлении.
     * </p>
     *
     * @param mpa рейтинг с проверяемым названием
     * @return {@code true} если рейтинг с таким названием уже существует, иначе {@code false}
     */
    private boolean containsName(Mpa mpa) {
        return mpaStorage.findAll().stream()
                .map(Mpa::getName)
                .anyMatch(mpa.getName()::equals);
    }
}