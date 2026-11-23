package program.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

/**
 * Кастомная реализация хеш-таблицы на основе массива бакетов и односвязных списков
 * для разрешения коллизий. Наследуется от {@link AbstractMap} для полной совместимости
 * со стандартным интерфейсом {@link Map} и Stream API.
 *
 * @param <K> тип ключей
 * @param <V> тип значений
 */
public class CustomHashMap<K, V> extends AbstractMap<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Node<K, V>[] table;
    private int size;
    private int threshold;
    private final float loadFactor;

    /**
     * Внутренний узел односвязного списка (бакета), реализующий {@link Map.Entry}.
     */
    private static class Node<K, V> implements Map.Entry<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Map.Entry<?, ?> e)) return false;
            return Objects.equals(key, e.getKey()) && Objects.equals(value, e.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }
    }

    /**
     * Создаёт пустую хеш-таблицу с начальной ёмкостью 16 и коэффициентом загрузки 0.75.
     */
    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        this.table = (Node<K, V>[]) new Node[DEFAULT_INITIAL_CAPACITY];
        this.threshold = (int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR);
    }

    /**
     * Связывает указанное значение с указанным ключом в карте.
     * Если ключ уже присутствовал в карте, старое значение заменяется новым.
     *
     * @param key   ключ (допускается {@code null})
     * @param value значение
     * @return предыдущее значение, связанное с ключом, или {@code null}, если ключа не было
     */
    @Override
    public V put(K key, V value) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;

        Node<K, V> current = table[index];
        while (current != null) {
            if (current.hash == hash && Objects.equals(current.key, key)) {
                V oldValue = current.value;
                current.value = value;
                return oldValue;
            }
            current = current.next;
        }
        table[index] = new Node<>(hash, key, value, table[index]);
        size++;

        if (size > threshold) {
            resize();
        }
        return null;
    }

    /**
     * Возвращает значение, соответствующее указанному ключу, или {@code null}, если ключ не найден.
     *
     * @param key ключ для поиска
     * @return значение или {@code null}
     */
    @Override
    public V get(Object key) {
        Node<K, V> node = getNode(key);
        return node == null ? null : node.value;
    }

    /**
     * Проверяет, содержит ли карта запись с указанным ключом.
     *
     * @param key ключ для проверки
     * @return {@code true}, если ключ найден в карте
     */
    @Override
    public boolean containsKey(Object key) {
        return getNode(key) != null;
    }

    /**
     * Удаляет запись для указанного ключа из карты, если она присутствует.
     *
     * @param key ключ удаляемой записи
     * @return предыдущее значение, связанное с ключом, или {@code null}, если ключ не найден
     */
    @Override
    public V remove(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;

        Node<K, V> current = table[index];
        Node<K, V> prev = null;

        while (current != null) {
            if (current.hash == hash && Objects.equals(current.key, key)) {
                if (prev == null) {
                    table[index] = current.next;
                } else {
                    prev.next = current.next;
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }
        return null;
    }

    /**
     * Возвращает количество пар ключ-значение в карте.
     *
     * @return текущий размер карты
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Проверяет, пуста ли карта.
     *
     * @return {@code true}, если в карте нет ни одной записи
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Удаляет все записи из карты.
     */
    @Override
    public void clear() {
        if (table != null && size > 0) {
            size = 0;
            for (int i = 0; i < table.length; i++) {
                table[i] = null;
            }
        }
    }

    /**
     * Возвращает представление множества записей {@link Set} для поддержки итерации,
     * представлений {@code keySet()}, {@code values()} и Stream API.
     *
     * @return множество записей карты
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<>() {
                    int bucketIndex = 0;
                    Node<K, V> current = null;
                    Node<K, V> lastReturned = null;

                    {
                        advanceToNextBucket();
                    }

                    private void advanceToNextBucket() {
                        while (current == null && bucketIndex < table.length) {
                            current = table[bucketIndex++];
                        }
                    }

                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public Entry<K, V> next() {
                        if (current == null) {
                            throw new NoSuchElementException();
                        }
                        lastReturned = current;
                        current = current.next;
                        if (current == null) {
                            advanceToNextBucket();
                        }
                        return lastReturned;
                    }

                    @Override
                    public void remove() {
                        if (lastReturned == null) {
                            throw new IllegalStateException();
                        }
                        CustomHashMap.this.remove(lastReturned.key);
                        lastReturned = null;
                    }
                };
            }

            @Override
            public int size() {
                return CustomHashMap.this.size;
            }
        };
    }

    /**
     * Внутренний метод поиска узла по ключу.
     *
     * @param key ключ для поиска
     * @return найденный узел {@link Node} или {@code null}
     */
    private Node<K, V> getNode(Object key) {
        int hash = hash(key);
        int index = (table.length - 1) & hash;
        Node<K, V> current = table[index];

        while (current != null) {
            if (current.hash == hash && Objects.equals(current.key, key)) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    /**
     * Удваивает размер массива бакетов и перераспределяет существующие узлы
     * при превышении порога загрузки ({@code threshold}).
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] oldTable = table;
        int newCapacity = oldTable.length * 2;
        Node<K, V>[] newTable = (Node<K, V>[]) new Node[newCapacity];
        threshold = (int) (newCapacity * loadFactor);

        for (Node<K, V> head : oldTable) {
            while (head != null) {
                Node<K, V> next = head.next;
                int newIndex = (newCapacity - 1) & head.hash;
                head.next = newTable[newIndex];
                newTable[newIndex] = head;
                head = next;
            }
        }
        table = newTable;
    }

    /**
     * Вычисляет сглаженный хеш ключа путём смешивания старших и младших битов.
     *
     * @param key ключ
     * @return хеш-значение (0 для {@code null})
     */
    private int hash(Object key) {
        if (key == null) return 0;
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    @Override
    public String toString() {
        if (size == 0) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Node<K, V> head : table) {
            Node<K, V> current = head;
            while (current != null) {
                if (!first) sb.append(", ");
                sb.append(current.key).append("=").append(current.value);
                first = false;
                current = current.next;
            }
        }
        return sb.append("}").toString();
    }
}