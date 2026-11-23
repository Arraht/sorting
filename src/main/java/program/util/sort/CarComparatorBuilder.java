package program.util.sort;

import program.dto.CarDto;

import java.util.Comparator;
import java.util.List;

/**
 * Утилита для построения составного компаратора {@link CarDto} по 1, 2 или 3 полям.
 */
public class CarComparatorBuilder {

    /**
     * Создаёт каскадный компаратор по переданному списку полей (от 1 до 3) с помощью Stream API.
     *
     * @param fields список уникальных названий полей ({@code brand}, {@code model}, {@code power}, {@code year})
     * @return готовый {@link Comparator}
     */
    public static Comparator<CarDto> build(List<String> fields) {
        if (fields == null || fields.isEmpty() || fields.size() > 3) {
            throw new IllegalArgumentException(
                    "Укажите от 1 до 3 полей для сортировки (brand, model, power, year)."
            );
        }

        long uniqueCount = fields.stream()
                .map(CarComparatorBuilder::normalizeFieldName)
                .distinct()
                .count();

        if (uniqueCount != fields.size()) {
            throw new IllegalArgumentException("Поля для сортировки не должны повторяться.");
        }

        return fields.stream()
                .map(CarComparatorBuilder::getSingleFieldComparator)
                .reduce(Comparator::thenComparing)
                .orElseThrow();
    }

    private static String normalizeFieldName(String field) {
        String lower = field.toLowerCase();
        return "horsepower".equals(lower) ? "power" : lower;
    }

    private static Comparator<CarDto> getSingleFieldComparator(String field) {
        return switch (normalizeFieldName(field)) {
            case "brand" -> Comparator.comparing(CarDto::getBrand, String.CASE_INSENSITIVE_ORDER);
            case "model" -> Comparator.comparing(CarDto::getModel, String.CASE_INSENSITIVE_ORDER);
            case "power" -> Comparator.comparingInt(CarDto::getHorsePower);
            case "year" -> Comparator.comparingInt(CarDto::getYear);
            default -> throw new IllegalArgumentException(
                    "Неизвестное поле '" + field + "'. Доступны: brand, model, power, year."
            );
        };
    }
}