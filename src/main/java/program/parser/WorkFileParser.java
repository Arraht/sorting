package program.parser;

import lombok.experimental.UtilityClass;
import program.dto.CarDto;
import program.util.CustomArrayList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Утилитный парсер для чтения рабочего JSON-файла пользователя с конкретными автомобилями.
 * Использует Stream API для формирования кастомного списка.
 */
@UtilityClass
public class WorkFileParser {

    private static final Pattern CAR_PATTERN = Pattern.compile(
            "\\{\\s*\"brand\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*" +
                    "\"model\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*" +
                    "\"horsePower\"\\s*:\\s*(\\d+)\\s*,\\s*" +
                    "\"year\"\\s*:\\s*(\\d+)\\s*\\}",
            Pattern.DOTALL
    );

    /**
     * Считывает рабочий JSON-файл с диска и преобразует его содержимое в {@link CustomArrayList} через стрим.
     *
     * @param filePath путь к рабочему JSON-файлу
     * @return кастомный список автомобилей из рабочего файла
     */
    public List<CarDto> parseWorkFile(Path filePath) {
        if (!Files.exists(filePath)) {
            return new CustomArrayList<>();
        }

        try {
            String json = Files.readString(filePath, StandardCharsets.UTF_8);

            return CAR_PATTERN.matcher(json).results()
                    .map(match -> CarDto.builder()
                            .brand(match.group(1))
                            .model(match.group(2))
                            .horsePower(Integer.parseInt(match.group(3)))
                            .year(Integer.parseInt(match.group(4)))
                            .build())
                    .collect(Collectors.toCollection(CustomArrayList::new));

        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения рабочего файла: " + filePath, e);
        }
    }
}