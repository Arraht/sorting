package program;

import program.service.CarFileGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Обработчик консольных команд пользователя.
 * Отвечает за разбор введённых команд, валидацию аргументов,
 * управление диалогом подтверждения перезаписи и вызов сервисов генерации данных.
 */
public class CommandProcessor {
    private static final String EXIT_COMMAND = "exit";
    private final CarFileGenerator carFileGenerator;
    private final Path workFile;
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
                      fill all [--append | --force]                 - заполнить всеми машинами из каталога
                      fill random <кол-во> [--append | --force]     - заполнить случайными машинами
                      fill car <марка> <модель> [--append | --force] - заполнить конкретной машиной
                      sort [параметр]                               - отсортировать машины (в разработке)
                      exit                                          - выход из приложения
                    Флаги:
                      --append  добавить новые машины в существующий файл без перезаписи
                      --force   перезаписать существующий файл без подтверждения
                    """;
            case "fill" -> handleFillCommand(rawParts);
            case "sort" -> "[Заглушка] Команда сортировки находится в разработке.";
            default -> "Неизвестная команда. Введите 'help' для просмотра списка команд.";
        };
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