package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.util.MpaUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Реализация хранилища рейтингов MPA на основе оперативной памяти (in-memory).
 * <p>
 * Обеспечивает временное хранение данных о возрастных рейтингах MPA, используя {@link Map}
 * для хранения объектов. При инициализации заполняется предопределенными
 * значениями рейтингов (1-5) через утилитный класс {@link MpaUtil}.
 * Предназначен для разработки и тестирования.
 * </p>
 *
 * <p>Стандартные рейтинги MPA, предустановленные в хранилище:
 * <ul>
 *   <li>1 - G (General Audiences) — нет возрастных ограничений</li>
 *   <li>2 - PG (Parental Guidance Suggested) — рекомендуется присутствие родителей</li>
 *   <li>3 - PG-13 (Parents Strongly Cautioned) — детям до 13 лет не рекомендуется</li>
 *   <li>4 - R (Restricted) — лицам до 17 лет обязательно присутствие родителей</li>
 *   <li>5 - NC-17 (Adults Only) — только для взрослых (18+)</li>
 * </ul>
 * </p>
 *
 * <p><b>Важно:</b> Данная реализация не подходит для production-среды,
 * так как все данные теряются при остановке приложения. Используется
 * на этапе разработки для быстрого прототипирования или как источник
 * данных по умолчанию.</p>
 *
 * @see ru.yandex.practicum.filmorate.storage.mpa.MpaStorage
 * @see ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage
 * @see ru.yandex.practicum.filmorate.util.MpaUtil
 */
@Slf4j
@Component
public class InMemoryMpaStorage implements MpaStorage {

    /**
     * Хранилище рейтингов MPA в памяти.
     * Ключ - идентификатор рейтинга, значение - объект рейтинга.
     * Инициализируется предопределенными значениями через {@link MpaUtil#fillMpa()}.
     */
    private final Map<Integer, Mpa> mpaMap = MpaUtil.fillMpa();

    /**
     * Создает новый рейтинг MPA в in-memory хранилище.
     * <p>
     * Генерирует новый идентификатор для рейтинга (начиная с 6, так как
     * предопределенные рейтинги занимают ID 1-5) и добавляет его в хранилище.
     * </p>
     *
     * @param mpa объект рейтинга для сохранения
     * @return сохраненный рейтинг с заполненным идентификатором
     */
    @Override
    public Mpa create(Mpa mpa) {
        mpa.setId(getNextId());
        mpaMap.put(mpa.getId(), mpa);
        return mpa;
    }

    /**
     * Находит рейтинг MPA по его идентификатору.
     *
     * @param id идентификатор рейтинга (для стандартных значений от 1 до 5)
     * @return {@link Optional} с найденным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не существует
     */
    @Override
    public Optional<Mpa> findById(Integer id) {
        return Optional.ofNullable(mpaMap.get(id));
    }

    /**
     * Возвращает список всех рейтингов MPA.
     * <p>
     * Возвращает коллекцию, содержащую как предопределенные рейтинги (1-5),
     * так и добавленные пользователем через метод {@link #create}.
     * </p>
     *
     * @return коллекция всех рейтингов в памяти
     */
    @Override
    public Collection<Mpa> findAll() {
        return mpaMap.values();
    }

    /**
     * Обновляет существующий рейтинг MPA.
     * <p>
     * Если рейтинг с указанным ID существует, обновляет его название и описание
     * (при условии, что новые значения не пустые). Предопределенные рейтинги
     * также могут быть обновлены.
     * </p>
     *
     * @param newMpa объект рейтинга с обновленными данными
     * @return {@link Optional} с обновленным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не найден
     */
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

    /**
     * Удаляет рейтинг MPA по его идентификатору.
     * <p>
     * <b>Внимание:</b> Позволяет удалять даже предопределенные рейтинги (1-5),
     * что может нарушить работу приложения. В текущей реализации не рекомендуется
     * удалять предопределенные значения.
     * </p>
     *
     * @param id идентификатор рейтинга
     * @return {@link Optional} с удаленным рейтингом, или пустой {@link Optional},
     *         если рейтинг с указанным id не найден
     */
    @Override
    public Optional<Mpa> removeById(Integer id) {
        return Optional.ofNullable(mpaMap.remove(id));
    }

    /**
     * Проверяет, существует ли рейтинг с указанным идентификатором.
     * <p>
     * В отличие от {@link MpaStorage#contains}, эта реализация проверяет
     * только наличие по ID, а не полное равенство объектов.
     * </p>
     *
     * @param mpa рейтинг для проверки
     * @return {@code true} если рейтинг с таким ID существует, иначе {@code false}
     */
    @Override
    public boolean contains(Mpa mpa) {
        return mpaMap.containsKey(mpa.getId());
    }

    /**
     * Генерирует следующий уникальный идентификатор для нового рейтинга.
     * <p>
     * Находит максимальный существующий ID и увеличивает его на 1.
     * Так как предопределенные рейтинги занимают ID 1-5, новые рейтинги
     * будут получать ID начиная с 6.
     * </p>
     *
     * @return следующий уникальный идентификатор
     */
    private Integer getNextId() {
        int currentMaxId = mpaMap.keySet()
                .stream()
                .reduce(Integer::max)
                .orElse(0);
        return ++currentMaxId;
    }
}