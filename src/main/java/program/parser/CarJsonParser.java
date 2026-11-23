package program.parser;

import lombok.experimental.UtilityClass;
import program.dto.BrandCarsDto;
import program.util.CustomArrayList;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Утилитный парсер для чтения эталонного JSON-каталога автомобилей из ресурсов приложения.
 * Заполнение кастомных коллекций выполняется с использованием Stream API.
 */
@UtilityClass
public class CarJsonParser {

    private static final Pattern BRAND_PATTERN =
            Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[(.*?)\\]", Pattern.DOTALL);
    private static final Pattern MODEL_PATTERN =
            Pattern.compile("\"([^\"]+)\"");

    /**
     * Читает JSON-файл каталога из директории {@code resources} и преобразует его в список DTO через стримы.
     *
     * @param fileName имя файла в папке ресурсов (например, {@code "cars.json"})
     * @return кастомный список {@link CustomArrayList}, содержащий объекты {@link BrandCarsDto}
     */
    public List<BrandCarsDto> parseFromResources(String fileName) {
        try (InputStream is = CarJsonParser.class.getClassLoader().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new IllegalArgumentException("Файл не найден в resources: " + fileName);
            }

            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            return BRAND_PATTERN.matcher(json).results()
                    .map(brandMatch -> {
                        String brand = brandMatch.group(1);
                        String modelsBlock = brandMatch.group(2);

                        List<String> models = MODEL_PATTERN.matcher(modelsBlock).results()
                                .map(modelMatch -> modelMatch.group(1))
                                .collect(Collectors.toCollection(CustomArrayList::new));

                        return new BrandCarsDto(brand, models);
                    })
                    .collect(Collectors.toCollection(CustomArrayList::new));

        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении JSON файла: " + fileName, e);
        }
    }
}