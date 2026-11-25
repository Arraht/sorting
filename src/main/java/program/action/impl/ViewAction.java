package program.action.impl;

import program.action.Action;
import program.action.AppContext;
import program.dto.CarDto;
import program.parser.WorkFileParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ViewAction implements Action {

    @Override
    public String execute(String[] args, AppContext context) {
        Path workFile = context.getWorkFile();

        if (!Files.exists(workFile)) {
            return "Рабочий файл '" + workFile + "' ещё не создан. Сначала заполните его командой 'fill'.";
        }

        List<CarDto> cars = WorkFileParser.parseWorkFile(workFile);

        if (cars.isEmpty()) {
            return "Файл '" + workFile + "' пуст. В нём пока нет ни одного автомобиля.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Список автомобилей в файле '").append(workFile).append("' (всего: ").append(cars.size()).append("):\n");

        for (int i = 0; i < cars.size(); i++) {
            CarDto car = cars.get(i);
            sb.append(String.format("%d. %s %s | Год: %d | Мощность: %d л.с.%n",
                    i + 1,
                    car.getBrand(),
                    car.getModel(),
                    car.getYear(),
                    car.getHorsePower()));
        }

        return sb.toString().trim();
    }

    @Override
    public String getDescription() {
        return "view                                          - показать список автомобилей в текущем файле";
    }
}