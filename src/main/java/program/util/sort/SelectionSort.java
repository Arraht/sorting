package program.util.sort;

import java.util.Comparator;
import java.util.List;

/**
 * Реализация алгоритма сортировки выбором (Selection Sort).
 *
 * @param <T> тип сортируемых элементов
 */
public class SelectionSort<T> implements SortStrategy<T> {

    @Override
    public void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() < 2) {
            return;
        }

        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(list.get(j), list.get(minIndex)) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                T temp = list.get(i);
                list.set(i, list.get(minIndex));
                list.set(minIndex, temp);
            }
        }
    }
}