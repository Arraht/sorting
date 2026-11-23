package program.util.sort;

import java.util.Comparator;
import java.util.List;

/**
 * Общий интерфейс для алгоритмов сортировки кастомных коллекций.
 *
 * @param <T> тип сортируемых элементов
 */
public interface SortStrategy<T> {

    /**
     * Сортирует переданный список на месте согласно заданному компаратору.
     *
     * @param list       список для сортировки
     * @param comparator правило сравнения элементов
     */
    void sort(List<T> list, Comparator<T> comparator);
}