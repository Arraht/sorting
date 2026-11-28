
package program.action.impl;

import program.action.Action;
import program.action.AppContext;
import program.dto.CarDto;
import program.parser.WorkFileParser;
import program.util.CustomArrayList;

import java.util.List;

public class CountAction implements Action {

    @Override
    public String execute(String[] args, AppContext context) {

        if (args.length == 0) {
            return "Использование: count <brand> <количество_потоков>";
        }

        if (args.length < 2) {
            return "Укажите марку автомобиля и количество потоков.";
        }

        String brand = args[0];

        int threadCount;

        try {
            threadCount = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            return "Количество потоков должно быть числом.";
        }

        if (threadCount <= 0) {
            return "Количество потоков должно быть больше 0.";
        }

        List<CarDto> cars =
                WorkFileParser.parseWorkFile(context.getWorkFile());

        CustomArrayList<String> brands = new CustomArrayList<>();

        for (CarDto car : cars) {
            brands.add(car.getBrand());
        }

        int result = brands.countOccurrencesParallel(
                brand,
                threadCount
        );

        return "Подсчёт завершён. Результат: " + result;
    }

    @Override
    public String getDescription() {
        return "count <brand> <threads> - подсчёт автомобилей указанной марки в нескольких потоках";
    }
}
