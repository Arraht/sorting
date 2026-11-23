package program.util.sort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import program.dto.CarDto;
import program.util.CustomArrayList;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortingTest {

    private List<CarDto> cars;

    @BeforeEach
    void setUp() {
        cars = new CustomArrayList<>();
        cars.add(new CarDto("BMW", "X5", 340, 2020));
        cars.add(new CarDto("AUDI", "A6", 249, 2018));
        cars.add(new CarDto("BMW", "3 Series", 184, 2022));
        cars.add(new CarDto("BMW", "X5", 249, 2019));
    }

    @Test
    void testBubbleSortCascadeByBrandModelYear() {
        List<String> fields = List.of("brand", "model", "year");
        Comparator<CarDto> comparator = CarComparatorBuilder.build(fields);
        SortStrategy<CarDto> sorter = new BubbleSort<>();

        sorter.sort(cars, comparator);

        // Ожидаемый порядок:
        // 1. AUDI A6 (2018)
        // 2. BMW 3 Series (2022)
        // 3. BMW X5 (2019)
        // 4. BMW X5 (2020)
        assertEquals("AUDI", cars.get(0).getBrand());
        assertEquals("3 Series", cars.get(1).getModel());
        assertEquals(2019, cars.get(2).getYear());
        assertEquals(2020, cars.get(3).getYear());
    }

    @Test
    void testSelectionSortByPower() {
        Comparator<CarDto> comparator = CarComparatorBuilder.build(List.of("power"));
        SortStrategy<CarDto> sorter = new SelectionSort<>();

        sorter.sort(cars, comparator);

        assertEquals(184, cars.get(0).getHorsePower());
        assertEquals(249, cars.get(1).getHorsePower());
        assertEquals(249, cars.get(2).getHorsePower());
        assertEquals(340, cars.get(3).getHorsePower());
    }

    @Test
    void testInsertionSortByYearAndBrand() {
        Comparator<CarDto> comparator = CarComparatorBuilder.build(List.of("year", "brand"));
        SortStrategy<CarDto> sorter = new InsertionSort<>();

        sorter.sort(cars, comparator);

        assertEquals(2018, cars.get(0).getYear());
        assertEquals(2019, cars.get(1).getYear());
        assertEquals(2020, cars.get(2).getYear());
        assertEquals(2022, cars.get(3).getYear());
    }

    @Test
    void testComparatorValidationThrowsOnInvalidInput() {
        // Пустой список полей
        assertThrows(IllegalArgumentException.class, () -> CarComparatorBuilder.build(List.of()));

        // Больше 3 полей
        assertThrows(IllegalArgumentException.class,
                () -> CarComparatorBuilder.build(List.of("brand", "model", "power", "year")));

        // Повторяющиеся поля (включая синоним horsepower)
        assertThrows(IllegalArgumentException.class,
                () -> CarComparatorBuilder.build(List.of("power", "horsepower")));

        // Несуществующее поле
        assertThrows(IllegalArgumentException.class,
                () -> CarComparatorBuilder.build(List.of("color")));
    }
}