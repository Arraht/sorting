package program.action.impl;

import program.action.Action;
import program.action.AppContext;
import program.dto.CarDto;
import program.parser.WorkFileParser;
import program.util.CustomArrayList;
import program.util.sort.BubbleSort;
import program.util.sort.CarComparatorBuilder;
import program.util.sort.InsertionSort;
import program.util.sort.SelectionSort;
import program.util.sort.SortStrategy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SortAction implements Action {
    private boolean waitingForFields = false;

    @Override
    public String execute(String[] args, AppContext context) {
        Path workFile = context.getWorkFile();

        if (!Files.exists(workFile)) {
            context.clearActiveAction();
            return "Ошибка: рабочий файл не найден. Сначала создайте его с помощью команды 'fill'.";
        }

        List<CarDto> cars = WorkFileParser.parseWorkFile(workFile);
        if (cars.size() <= 1) {
            context.clearActiveAction();
            return "Ошибка: для сортировки в файле должно быть больше одной машины (сейчас: " + cars.size() + ").";
        }

        // Если команда вызвана без аргументов — переходим в пошаговый режим
        if (args.length == 0 && !waitingForFields) {
            waitingForFields = true;
            context.setActiveAction(this);
            return "Введите поля для сортировки (brand, model, power, year) и при желании алгоритм (например: brand year или selection brand):";
        }

        // Сбрасываем активное состояние, так как сейчас будем обрабатывать полученные аргументы
        waitingForFields = false;
        context.clearActiveAction();

        String firstArg = args[0].toLowerCase();
        SortStrategy<CarDto> sorter;
        String algorithmName;
        int fieldsStartIndex;

        switch (firstArg) {
            case "bubble", "пузырьком", "пузырек", "пузырёк" -> {
                sorter = new BubbleSort<>();
                algorithmName = "BubbleSort (пузырьком)";
                fieldsStartIndex = 1;
            }
            case "selection", "выбором" -> {
                sorter = new SelectionSort<>();
                algorithmName = "SelectionSort (выбором)";
                fieldsStartIndex = 1;
            }
            case "insertion", "вставками" -> {
                sorter = new InsertionSort<>();
                algorithmName = "InsertionSort (вставками)";
                fieldsStartIndex = 1;
            }
            default -> {
                sorter = new BubbleSort<>();
                algorithmName = "BubbleSort (по умолчанию)";
                fieldsStartIndex = 0;
            }
        }

        List<String> fields = Arrays.stream(args)
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
            context.getCarFileGenerator().writeCarsToJsonFile(cars, workFile);

            return "Успешно: файл отсортирован (" + algorithmName + ") по полям ["
                    + String.join(" -> ", fields) + "] и перезаписан.";
        } catch (IllegalArgumentException e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    @Override
    public String getDescription() {
        return """
                sort [алгоритм] <поле1> [поле2] [поле3]       - отсортировать файл по 1–3 полям (brand, model, power, year)
                Алгоритмы для sort (необязательно, по умолчанию bubble):
                  bubble (пузырьком) | selection (выбором) | insertion (вставками)""";
    }
}