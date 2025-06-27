package ru.yandex.practicum.filmorate.util;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Map;
import java.util.TreeMap;

public class MpaUtil {
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
