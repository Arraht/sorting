package program.action.impl;

import program.action.Action;
import program.action.AppContext;
import program.dto.CarDto;
import program.parser.WorkFileParser;
import program.service.CarFileGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class AddAction implements Action {

    private static final String USAGE = """
            Использование:
              add manual <марка> <модель> <мощность> <год>   (пример: add manual BMW X5 340 2020)
              add file <имя_файла>                           (пример: add file garage_bmw)
              add random""";

    @Override
    public String execute(String[] args, AppContext context) {
        if (args.length == 0) {
            return USAGE;
        }

        String mode = args[0].toLowerCase();

        return switch (mode) {
            case "manual" -> addManual(args, context);
            case "file" -> addFromFile(args, context);
            case "random" -> addRandom(context);
            default -> "Ошибка: неизвестный режим '" + mode + "'.\n" + USAGE;
        };
    }

    private String addManual(String[] args, AppContext context) {
        if (args.length < 5) {
            return "Ошибка: недостаточно аргументов.\n" + USAGE;
        }

        int horsePower;
        int year;
        try {
            horsePower = Integer.parseInt(args[args.length - 2]);
            year = Integer.parseInt(args[args.length - 1]);
        } catch (NumberFormatException e) {
            return "Ошибка: мощность и год должны быть целыми числами. Пример: add manual BMW X5 340 2020";
        }

        int maxYear = LocalDate.now().getYear();
        if (horsePower < 1 || horsePower > 2000) {
            return "Ошибка: мощность должна быть от 1 до 2000 л.с.";
        }
        if (year < 1886 || year > maxYear) {
            return "Ошибка: год выпуска должен быть от 1886 до " + maxYear + ".";
        }

        String brand = args[1];
        String model = String.join(" ", Arrays.copyOfRange(args, 2, args.length - 2));

        try {
            CarFileGenerator generator = context.getCarFileGenerator();
            CarDto car = generator.createCatalogCar(brand, model, horsePower, year);
            generator.addCar(context.getWorkFile(), car);
            return "Успешно: добавлена машина " + car.getBrand() + " " + car.getModel()
                    + ", " + car.getHorsePower() + " л.с., " + car.getYear() + " г.";
        } catch (IllegalArgumentException e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    private String addFromFile(String[] args, AppContext context) {
        if (args.length < 2) {
            return "Ошибка: укажите имя файла, например: add file garage_bmw";
        }

        String fileName = args[1].trim();

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return "Ошибка: укажите только имя файла без путей (например: add file garage_1).";
        }

        if (!fileName.toLowerCase().endsWith(".json")) {
            fileName += ".json";
        }

        Path workFile = context.getWorkFile();
        Path parentDir = workFile.getParent() != null ? workFile.getParent() : Path.of("data");
        Path sourceFile = parentDir.resolve(fileName);

        if (!Files.exists(sourceFile)) {
            return "Ошибка: файл '" + sourceFile + "' не найден.";
        }
        if (sourceFile.equals(workFile)) {
            return "Ошибка: нельзя добавить файл сам в себя.";
        }

        List<CarDto> cars = WorkFileParser.parseWorkFile(sourceFile);
        if (cars.isEmpty()) {
            return "Ошибка: в файле '" + sourceFile + "' нет машин.";
        }

        context.getCarFileGenerator().addCars(workFile, cars);
        return "Успешно: из файла '" + sourceFile + "' добавлено машин: " + cars.size() + ".";
    }

    private String addRandom(AppContext context) {
        context.getCarFileGenerator().fillRandomSubset(context.getWorkFile(), 1, true);
        return "Успешно: в рабочий файл добавлена одна случайная машина.";
    }

    @Override
    public String getDescription() {
        return """
                add manual <марка> <модель> <мощность> <год>  - добавить одну машину (из каталога)
                  add file <имя_файла>                          - добавить машины из другого файла в data/
                  add random                                    - добавить одну случайную машину""";
    }
}
