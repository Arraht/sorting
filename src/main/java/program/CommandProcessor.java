package program;

import program.action.Action;
import program.action.ActionFactory;
import program.action.AppContext;
import program.service.CarFileGenerator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;

/**
 * Обработчик консольных команд пользователя.
 * Отвечает за маршрутизацию ввода в соответствующие Action и обработку подтверждений.
 */
public class CommandProcessor {
    private final ActionFactory actionFactory = new ActionFactory();
    private final AppContext context;

    public CommandProcessor(CarFileGenerator carFileGenerator, Path workFile) {
        this.context = new AppContext(carFileGenerator, workFile);
    }

    public String process(String command) {
        String trimmed = command.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String[] rawParts = trimmed.split("\\s+");
        String mainCommand = rawParts[0].toLowerCase();

        // 1. Команды-прерыватели (exit, cancel) срабатывают всегда
        if (actionFactory.isInterruptCommand(mainCommand)) {
            Optional<Action> interruptAction = actionFactory.getAction(mainCommand);
            if (interruptAction.isPresent()) {
                String[] args = Arrays.copyOfRange(rawParts, 1, rawParts.length);
                return interruptAction.get().execute(args, context);
            }
        }

        // 2. Если есть активная многошаговая команда — передаём ей весь ввод пользователя
        if (context.getActiveAction() != null) {
            return context.getActiveAction().execute(rawParts, context);
        }

        // 3. Обычный вызов новой команды
        Optional<Action> action = actionFactory.getAction(mainCommand);
        if (action.isPresent()) {
            String[] args = Arrays.copyOfRange(rawParts, 1, rawParts.length);
            return action.get().execute(args, context);
        }

        return "Неизвестная команда. Введите 'help' для просмотра списка команд.";
    }
}