package program.util.sort;

import java.util.Comparator;
import java.util.List;

/**
 * Реализация алгоритма сортировки вставками (Insertion Sort).
 *
 * @param <T> тип сортируемых элементов
 */
public class InsertionSort<T> implements SortStrategy<T> {

    @Override
    public void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() < 2) {
            return;
        }

        int n = list.size();
        for (int i = 1; i < n; i++) {
            T key = list.get(i);
            int j = i - 1;

            while (j >= 0 && comparator.compare(list.get(j), key) > 0) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }
}