package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Map;
import java.util.TreeMap;

/**
 * Утилитный класс для инициализации справочника возрастных рейтингов MPA.
 * <p>
 * Предоставляет методы для создания предопределенного набора рейтингов
 * Motion Picture Association (MPA), используемых в приложении для
 * возрастной классификации фильмов.
 * </p>
 *
 * <p>Создаваемые рейтинги MPA (с соответствующими ID):
 * <ul>
 *   <li>1 - G (General Audiences) — у фильма нет возрастных ограничений</li>
 *   <li>2 - PG (Parental Guidance Suggested) — детям рекомендуется смотреть фильм с родителями</li>
 *   <li>3 - PG-13 (Parents Strongly Cautioned) — детям до 13 лет просмотр не желателен</li>
 *   <li>4 - R (Restricted) — лицам до 17 лет просматривать фильм можно только в присутствии взрослого</li>
 *   <li>5 - NC-17 (Adults Only) — лицам до 18 лет просмотр запрещён</li>
 * </ul>
 * </p>
 *
 * <p>Рейтинги хранятся в {@link TreeMap} с сортировкой по ключу (ID),
 * что гарантирует упорядоченность при итерации.</p>
 *
 * @see ru.yandex.practicum.filmorate.model.Mpa
 * @see ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaStorage
 */
public class MpaUtil {

    /**
     * Создает и заполняет карту предопределенных рейтингов MPA.
     * <p>
     * Метод создает объекты рейтингов с ID от 1 до 5, соответствующими
     * названиями (аббревиатурами) и описаниями из предопределенных массивов.
     * Рейтинги помещаются в {@link TreeMap}, где ключом является ID рейтинга,
     * что обеспечивает естественную сортировку при последующем использовании.
     * </p>
     *
     * <p>Процесс создания:
     * <ol>
     *   <li>Создается пустая {@link TreeMap} с компаратором по Integer</li>
     *   <li>Для каждого индекса от 1 до длины массива названий:
     *       <ul>
     *           <li>Создается новый объект {@link Mpa}</li>
     *           <li>Устанавливается ID равный текущему индексу</li>
     *           <li>Устанавливается название (аббревиатура) из массива mpaNames</li>
     *           <li>Устанавливается описание из массива mpaDescription</li>
     *           <li>Рейтинг помещается в карту под своим ID</li>
     *       </ul>
     *   </li>
     * </ol>
     * </p>
     *
     * <p>Соответствие индексов массива и рейтингов:
     * <ul>
     *   <li>Индекс 0 → ID 1: G</li>
     *   <li>Индекс 1 → ID 2: PG</li>
     *   <li>Индекс 2 → ID 3: PG-13</li>
     *   <li>Индекс 3 → ID 4: R</li>
     *   <li>Индекс 4 → ID 5: NC-17</li>
     * </ul>
     * </p>
     *
     * <p><b>Важно:</b> Данный метод используется для инициализации
     * in-memory хранилища рейтингов при старте приложения и гарантирует
     * наличие базового набора рейтингов MPA.</p>
     *
     * @return {@link Map} с ключом - ID рейтинга (Integer) и значением - объект {@link Mpa},
     *         содержащая все предопределенные рейтинги MPA, отсортированные по ID
     *
     * @see java.util.TreeMap
     * @see ru.yandex.practicum.filmorate.storage.mpa.InMemoryMpaStorage
     */
    public static Map<Integer, Mpa> fillMpa() {
        Map<Integer, Mpa> mpaMap = new TreeMap<>(Integer::compareTo);

        String[] mpaNames = {
                "G",
                "PG",
                "PG-13",
                "R",
                "NC-17"
        };

        String[] mpaDescription = {
                "у фильма нет возрастных ограничений",
                "детям рекомендуется смотреть фильм с родителями",
                "детям до 13 лет просмотр не желателен",
                "лицам до 17 лет просматривать фильм можно только в присутствии взрослого",
                "лицам до 18 лет просмотр запрещён"
        };

        for (int i = 1; i <= mpaNames.length; i++) {
            Mpa mpa = new Mpa();
            mpa.setId(i);
            mpa.setName(mpaNames[i - 1]);
            mpa.setDescription(mpaDescription[i - 1]);
            mpaMap.put(mpa.getId(), mpa);
        }

        return mpaMap;
    }
}