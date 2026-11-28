package program.util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Objects;

public class CustomArrayList<E> extends AbstractList<E> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elementData;
    private int size;

    /**
     * Конструктор по умолчанию (ёмкость 10)
     */
    public CustomArrayList() {
        this.elementData = new Object[DEFAULT_CAPACITY];
    }

    /**
     * Конструктор с заданной начальной ёмкостью
     */
    public CustomArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Некорректная ёмкость: " + initialCapacity);
        }
        this.elementData = new Object[initialCapacity];
    }

    /**
     * Добавление элемента в конец списка
     */
    @Override
    public boolean add(E element) {
        ensureCapacity(size + 1);
        elementData[size++] = element;
        return true;
    }

    /**
     * Вставка элемента по индексу со сдвигом вправо
     */
    @Override
    public void add(int index, E element) {
        checkIndexForAdd(index);
        ensureCapacity(size + 1);
        System.arraycopy(elementData, index, elementData, index + 1, size - index);
        elementData[index] = element;
        size++;
    }

    /**
     * Получение элемента по индексу
     */
    @SuppressWarnings("unchecked")
    @Override
    public E get(int index) {
        Objects.checkIndex(index, size);
        return (E) elementData[index];
    }

    /**
     * Замена элемента по индексу
     */
    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        Objects.checkIndex(index, size);
        E oldValue = (E) elementData[index];
        elementData[index] = element;
        return oldValue;
    }

    /**
     * Удаление элемента по индексу со сдвигом влево
     */
    @SuppressWarnings("unchecked")
    @Override
    public E remove(int index) {
        Objects.checkIndex(index, size);
        E oldValue = (E) elementData[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index, numMoved);
        }
        elementData[--size] = null;
        return oldValue;
    }

    /**
     * Текущее количество элементов
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Проверка, пуст ли список
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Очистка списка
     */
    @Override
    public void clear() {
        Arrays.fill(elementData, 0, size, null);
        size = 0;
    }

    /**
     * Динамическое увеличение массива в 1.5 раза при нехватке места
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elementData.length) {
            int oldCapacity = elementData.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }
//Метод подсчета
public int countOccurrencesParallel(E element, int threadCount) {
    if (threadCount <= 0) {
        throw new IllegalArgumentException("Количество потоков должно быть больше 0");
    }

    if (size == 0) {
        System.out.println("Количество вхождений: 0");
        return 0;
    }

    int actualThreads = Math.min(threadCount, size);
    int partSize = (size + actualThreads - 1) / actualThreads;

    Thread[] threads = new Thread[actualThreads];
    int[] results = new int[actualThreads];

    for (int i = 0; i < actualThreads; i++) {
        final int threadIndex = i;
        final int start = i * partSize;
        final int end = Math.min(start + partSize, size);

        threads[i] = new Thread(() -> {
            int count = 0;

            for (int j = start; j < end; j++) {
                if (Objects.equals(elementData[j], element)) {
                    count++;
                }
            }

            results[threadIndex] = count;
        });

        threads[i].start();
    }

    for (Thread thread : threads) {
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Поток был прерван", e);
        }
    }

    int totalCount = 0;

    for (int result : results) {
        totalCount += result;
    }

    System.out.println("Количество вхождений элемента " + element + ": " + totalCount);

    return totalCount;
}

    @Override
    public String toString() {
        if (size == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elementData[i]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}