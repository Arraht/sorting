package program;

import program.service.CarFileGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import program.dto.CarDto;
import program.parser.WorkFileParser;
import program.util.CustomArrayList;
import program.util.sort.BubbleSort;
import program.util.sort.CarComparatorBuilder;
import program.util.sort.InsertionSort;
import program.util.sort.SelectionSort;
import program.util.sort.SortStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Обработчик консольных команд пользователя.
 * Отвечает за разбор введённых команд, валидацию аргументов,
 * управление диалогом подтверждения перезаписи и вызов сервисов генерации данных.
 */
public class CommandProcessor {
    private static final String EXIT_COMMAND = "exit";
    private final CarFileGenerator carFileGenerator;
    private Path workFile;
    private Runnable pendingOverwriteAction;

    /**
     * Создаёт экземпляр обработчика команд.
     *
     * @param carFileGenerator сервис для генерации и записи данных об автомобилях
     * @param workFile         путь к рабочему JSON-файлу пользователя
     */
    public CommandProcessor(CarFileGenerator carFileGenerator, Path workFile) {
        this.carFileGenerator = carFileGenerator;
        this.workFile = workFile;
    }

    /**
     * Обрабатывает строку, введённую пользователем в консоли.
     *
     * @param command исходная строка команды
     * @return результат выполнения команды для вывода пользователю или {@code "EXIT"} для завершения работы
     */
    public String process(String command) {
        String trimmed = command.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        // Если программа ждёт ответа на вопрос о перезаписи файла
        if (pendingOverwriteAction != null) {
            return handleConfirmation(trimmed);
        }

        String[] rawParts = trimmed.split("\\s+");
        String mainCommand = rawParts[0].toLowerCase();

        return switch (mainCommand) {
            case EXIT_COMMAND -> "EXIT";
            case "help" -> """
                    Доступные команды:
                      file [имя_файла]                              - показать текущий или переключить рабочий файл
                      fill all [--append | --force]                 - заполнить всеми машинами из каталога
                      fill random <кол-во> [--append | --force]     - заполнить случайными машинами
                      fill car <марка> <модель> [--append | --force] - заполнить конкретной машиной
                      sort [алгоритм] <поле1> [поле2] [поле3]       - отсортировать файл по 1–3 полям (brand, model, power, year)
                      exit                                          - выход из приложения
                    Алгоритмы для sort (необязательно, по умолчанию bubble):
                      bubble (пузырьком) | selection (выбором) | insertion (вставками)
                    Флаги для fill:
                      --append  добавить новые машины в существующий файл без перезаписи
                      --force   перезаписать существующий файл без подтверждения
                    """;
            case "file" -> handleFileCommand(rawParts);
            case "fill" -> handleFillCommand(rawParts);
            case "sort" -> handleSortCommand(rawParts);
            default -> "Неизвестная команда. Введите 'help' для просмотра списка команд.";
        };
    }

    /**
     * Показывает текущий рабочий файл или переключает программу на новый файл в директории {@code data/}.
     * Если расширение {@code .json} не указано, оно добавляется автоматически.
     *
     * @param rawParts массив слов введённой команды
     * @return сообщение с информацией о текущем или новом рабочем файле
     */
    private String handleFileCommand(String[] rawParts) {
        if (rawParts.length < 2) {
            return "Текущий рабочий файл: " + workFile
                    + (Files.exists(workFile) ? " (существует)" : " (ещё не создан)");
        }

        String fileName = rawParts[1].trim();

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return "Ошибка: укажите только имя файла без путей (например: file garage_1).";
        }

        if (!fileName.toLowerCase().endsWith(".json")) {
            fileName += ".json";
        }

        Path parentDir = workFile.getParent() != null ? workFile.getParent() : Path.of("data");
        this.workFile = parentDir.resolve(fileName);

        String status = Files.exists(workFile)
                ? "файл уже существует (можно сортировать или дописывать)."
                : "новый файл (будет создан при выполнении команды 'fill').";

        return "Рабочий файл переключён на '" + workFile + "' — " + status;
    }


    private String handleSortCommand(String[] rawParts) {
        if (!Files.exists(workFile)) {
            return "Ошибка: рабочий файл не найден. Сначала создайте его с помощью команды 'fill'.";
        }

        List<CarDto> cars = WorkFileParser.parseWorkFile(workFile);
        if (cars.size() <= 1) {
            return "Ошибка: для сортировки в файле должно быть больше одной машины (сейчас: " + cars.size() + ").";
        }

        if (rawParts.length < 2) {
            return "Ошибка: укажите поля для сортировки (brand, model, year). Пример: sort brand year";
        }

        String firstArg = rawParts[1].toLowerCase();
        SortStrategy<CarDto> sorter;
        String algorithmName;
        int fieldsStartIndex;

        switch (firstArg) {
            case "bubble", "пузырьком", "пузырек", "пузырёк" -> {
                sorter = new BubbleSort<>();
                algorithmName = "BubbleSort (пузырьком)";
                fieldsStartIndex = 2;
            }
            case "selection", "выбором" -> {
                sorter = new SelectionSort<>();
                algorithmName = "SelectionSort (выбором)";
                fieldsStartIndex = 2;
            }
            case "insertion", "вставками" -> {
                sorter = new InsertionSort<>();
                algorithmName = "InsertionSort (вставками)";
                fieldsStartIndex = 2;
            }
            default -> {
                sorter = new BubbleSort<>();
                algorithmName = "BubbleSort (по умолчанию)";
                fieldsStartIndex = 1;
            }
        }

        List<String> fields = Arrays.stream(rawParts)
                .skip(fieldsStartIndex)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(CustomArrayList::new));

        if (fields.isEmpty() || fields.size() > 3) {
            return "Ошибка: укажите от 1 до 3 полей для сортировки (brand, model, power, year). " +
                    "Пример: sort brand power year";
        }

        try {
            Comparator<CarDto> comparator = CarComparatorBuilder.build(fields);
            sorter.sort(cars, comparator);
            carFileGenerator.writeCarsToJsonFile(cars, workFile);

            return "Успешно: файл отсортирован (" + algorithmName + ") по полям ["
                    + String.join(" -> ", fields) + "] и перезаписан.";
        } catch (IllegalArgumentException e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    /**
     * Обрабатывает ответ пользователя на запрос подтверждения перезаписи существующего файла.
     *
     * @param input ответ пользователя (ожидается y/yes/д/да или n/no/н/нет)
     * @return сообщение о результате выполнения или отмене операции
     */
    private String handleConfirmation(String input) {
        String answer = input.toLowerCase();
        if ("y".equals(answer) || "yes".equals(answer) || "д".equals(answer) || "да".equals(answer)) {
            Runnable action = pendingOverwriteAction;
            pendingOverwriteAction = null;
            try {
                action.run();
                return "Успешно: существующий файл перезаписан.";
            } catch (Exception e) {
                return "Ошибка: " + e.getMessage();
            }
        } else if ("n".equals(answer) || "no".equals(answer) || "н".equals(answer) || "нет".equals(answer)) {
            pendingOverwriteAction = null;
            return "Операция отменена.";
        } else {
            return "Пожалуйста, введите 'y' (да) для перезаписи или 'n' (нет) для отмены:";
        }
    }

    /**
     * Разбирает аргументы и флаги команды {@code fill}, а также запускает выбранный режим генерации.
     *
     * @param rawParts массив слов, полученных при разбиении введённой команды
     * @return сообщение о результате операции или текст ошибки при неверных аргументах
     */
    private String handleFillCommand(String[] rawParts) {
        if (rawParts.length < 2) {
            return "Ошибка: укажите режим заполнения (all | random <кол-во> | car <марка> <модель>).";
        }

        boolean append = false;
        boolean force = false;

        int endIdx = rawParts.length;
        String lastArg = rawParts[endIdx - 1].toLowerCase();
        if ("--append".equals(lastArg)) {
            append = true;
            endIdx--;
        } else if ("--force".equals(lastArg)) {
            force = true;
            endIdx--;
        }

        String[] parts = Arrays.copyOf(rawParts, endIdx);
        String mode = parts[1].toLowerCase();

        try {
            Runnable action;
            String successMessage;

            switch (mode) {
                case "all":
                    boolean finalAppendAll = append;
                    action = () -> carFileGenerator.fillAllFromCatalog(workFile, finalAppendAll);
                    successMessage = append
                            ? "Успешно: все машины из каталога добавлены в рабочий файл."
                            : "Успешно: рабочий файл заполнен всеми машинами из каталога.";
                    break;

                case "random":
                    if (parts.length < 3) {
                        return "Ошибка: укажите количество машин, например: 'fill random 5'.";
                    }
                    int count = Integer.parseInt(parts[2]);

                    boolean finalAppendRandom = append;
                    action = () -> carFileGenerator.fillRandomSubset(workFile, count, finalAppendRandom);
                    successMessage = append
                            ? "Успешно: в рабочий файл добавлено случайных машин: " + count + "."
                            : "Успешно: рабочий файл заполнен случайными машинами (" + count + " шт.).";
                    break;

                case "car":
                    if (parts.length < 4) {
                        return "Ошибка: укажите марку и модель, например: 'fill car BMW X5'.";
                    }
                    String brand = parts[2];
                    String model = String.join(" ", Arrays.copyOfRange(parts, 3, parts.length));

                    boolean finalAppendCar = append;
                    action = () -> carFileGenerator.fillWithSpecificCar(workFile, brand, model, finalAppendCar);
                    successMessage = append
                            ? "Успешно: автомобиль " + brand + " " + model + " добавлен в рабочий файл."
                            : "Успешно: рабочий файл заполнен автомобилем " + brand + " " + model + ".";
                    break;

                default:
                    return "Ошибка: неизвестный режим заполнения '" + mode + "'.";
            }

            if (!append && !force && Files.exists(workFile)) {
                pendingOverwriteAction = action;
                return "Файл '" + workFile + "' уже существует. Перезаписать? (y/n)";
            }

            action.run();
            return successMessage;

        } catch (NumberFormatException e) {
            return "Ошибка: количество должно быть целым числом.";
        } catch (IllegalArgumentException e) {
            return "Ошибка: " + e.getMessage();
        }
    }
}