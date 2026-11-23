package program.util.sort;

import java.util.Comparator;
import java.util.List;

public class BubbleSort<T> implements SortStrategy<T> {

    @Override
    public void sort(List<T> list, Comparator<T> comparator) {
        if (list == null || list.size() < 2) {
            return;
        }

        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;

            for (int j = 0; j < n - i - 1; j++) {
                T left = list.get(j);
                T right = list.get(j + 1);

                if (comparator.compare(left, right) > 0) {
                    list.set(j, right);
                    list.set(j + 1, left);
                    swapped = true;
                }
            }

            if (!swapped) {
                break;
            }
        }
    }
}