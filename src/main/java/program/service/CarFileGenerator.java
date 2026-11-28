package program.service;

import program.dto.CarDto;
import program.parser.CarJsonParser;
import program.parser.WorkFileParser;
import program.util.CustomArrayList;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Сервис для генерации и сохранения рабочего JSON-файла с автомобилями.
 * Все операции по сбору и объединению кастомных коллекций выполняются посредством Stream API.
 */
public class CarFileGenerator {
    private static final String CATALOG_FILE = "cars.json";

    /**
     * Заполняет рабочий файл всеми автомобилями из каталога со случайными характеристиками.
     *
     * @param targetFile путь к целевому рабочему JSON-файлу
     * @param append     {@code true} — дописать в конец существующего файла, {@code false} — перезаписать файл
     */
    public void fillAllFromCatalog(Path targetFile, boolean append) {
        List<CarDto> generatedCars = getAllCatalogCarsStream()
                .collect(Collectors.toCollection(CustomArrayList::new));

        saveCars(generatedCars, targetFile, append);
    }

    /**
     * Заполняет рабочий файл указанным количеством случайных уникальных автомобилей из каталога.
     *
     * @param targetFile путь к целевому рабочему JSON-файлу
     * @param count      количество случайных автомобилей
     * @param append     {@code true} — дописать в конец существующего файла, {@code false} — перезаписать файл
     */
    public void fillRandomSubset(Path targetFile, int count, boolean append) {
        List<CarDto> allPossibleCars = getAllCatalogCarsStream()
                .collect(Collectors.toCollection(CustomArrayList::new));

        if (count <= 0 || count >= allPossibleCars.size()) {
            throw new IllegalArgumentException(
                    "Количество должно быть от 1 до " + (allPossibleCars.size() - 1)
            );
        }

        Collections.shuffle(allPossibleCars, ThreadLocalRandom.current());

        List<CarDto> randomSubset = allPossibleCars.stream()
                .limit(count)
                .collect(Collectors.toCollection(CustomArrayList::new));

        saveCars(randomSubset, targetFile, append);
    }

    /**
     * Заполняет рабочий файл одним конкретным автомобилем со случайными мощностью и годом выпуска.
     *
     * @param targetFile путь к целевому рабочему JSON-файлу
     * @param brand      марка автомобиля
     * @param model      модель автомобиля
     * @param append     {@code true} — дописать в конец существующего файла, {@code false} — перезаписать файл
     */
    public void fillWithSpecificCar(Path targetFile, String brand, String model, boolean append) {
        List<CarDto> singleCarList = Stream.of(createRandomCar(brand, model))
                .collect(Collectors.toCollection(CustomArrayList::new));

        saveCars(singleCarList, targetFile, append);
    }

    /**
     * Создаёт автомобиль с указанными характеристиками, если такая марка и модель есть в каталоге.
     * Регистр марки и модели не важен, в результате используется написание из каталога.
     *
     * @param brand      марка автомобиля
     * @param model      модель автомобиля
     * @param horsePower мощность в л.с.
     * @param year       год выпуска
     * @return автомобиль с написанием марки и модели из каталога
     * @throws IllegalArgumentException если такой машины нет в каталоге
     */
    public CarDto createCatalogCar(String brand, String model, int horsePower, int year) {
        return CarJsonParser.parseFromResources(CATALOG_FILE).stream()
                .filter(brandDto -> brandDto.getBrand().equalsIgnoreCase(brand))
                .flatMap(brandDto -> brandDto.getModels().stream()
                        .filter(catalogModel -> catalogModel.equalsIgnoreCase(model))
                        .map(catalogModel -> CarDto.builder()
                                .brand(brandDto.getBrand())
                                .model(catalogModel)
                                .horsePower(horsePower)
                                .year(year)
                                .build()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "автомобиль '" + brand + " " + model + "' отсутствует в каталоге."));
    }

    /**
     * Дописывает один автомобиль в конец рабочего файла (файл создаётся, если его нет).
     *
     * @param targetFile путь к целевому рабочему JSON-файлу
     * @param car        автомобиль для добавления
     */
    public void addCar(Path targetFile, CarDto car) {
        addCars(targetFile, List.of(car));
    }

    /**
     * Дописывает список автомобилей в конец рабочего файла (файл создаётся, если его нет).
     *
     * @param targetFile путь к целевому рабочему JSON-файлу
     * @param cars       автомобили для добавления
     */
    public void addCars(Path targetFile, List<CarDto> cars) {
        saveCars(cars, targetFile, true);
    }

    /**
     * Возвращает поток всех возможных автомобилей из каталога со случайными характеристиками.
     */
    private Stream<CarDto> getAllCatalogCarsStream() {
        return CarJsonParser.parseFromResources(CATALOG_FILE).stream()
                .flatMap(brandDto -> brandDto.getModels().stream()
                        .map(model -> createRandomCar(brandDto.getBrand(), model)));
    }

    /**
     * Объединяет существующие и новые записи в единый {@link CustomArrayList} с помощью {@link Stream#concat}.
     */
    private void saveCars(List<CarDto> newCars, Path targetFile, boolean append) {
        Stream<CarDto> existingStream = (append && Files.exists(targetFile))
                ? WorkFileParser.parseWorkFile(targetFile).stream()
                : Stream.empty();

        List<CarDto> finalCars = Stream.concat(existingStream, newCars.stream())
                .collect(Collectors.toCollection(CustomArrayList::new));

        writeCarsToJsonFile(finalCars, targetFile);
    }

    private CarDto createRandomCar(String brand, String model) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return CarDto.builder()
                .brand(brand)
                .model(model)
                .horsePower(random.nextInt(90, 651))
                .year(random.nextInt(2000, 2027))
                .build();
    }

    public void writeCarsToJsonFile(List<CarDto> cars, Path targetFile) {
        String json = cars.stream()
                .map(car -> String.format("""
                          {
                            "brand": "%s",
                            "model": "%s",
                            "horsePower": %d,
                            "year": %d
                          }""", car.getBrand(), car.getModel(), car.getHorsePower(), car.getYear()))
                .collect(Collectors.joining(",\n", "[\n", "\n]"));

        try {
            if (targetFile.getParent() != null) {
                Files.createDirectories(targetFile.getParent());
            }
            Files.writeString(targetFile, json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи в рабочий файл: " + targetFile, e);
        }
    }
}
