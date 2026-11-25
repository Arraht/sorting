package program.action.impl;

import program.action.Action;
import program.action.AppContext;
import program.service.CarFileGenerator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FillAction implements Action {

    private enum Step {
        INITIAL,
        WAITING_FOR_PARAMS,
        WAITING_FOR_CONFIRMATION
    }

    private Step currentStep = Step.INITIAL;
    private Runnable pendingOverwriteAction;

    @Override
    public String execute(String[] args, AppContext context) {
        if (currentStep == Step.WAITING_FOR_CONFIRMATION) {
            return handleConfirmation(args, context);
        }

        // Если команда запущена без параметров (просто "fill") — переходим в пошаговый режим
        if (args.length == 0 && currentStep == Step.INITIAL) {
            currentStep = Step.WAITING_FOR_PARAMS;
            context.setActiveAction(this);
            return "Укажите режим заполнения (all | random <кол-во> | car <марка> <модель>) и при желании флаги (--append | --force):";
        }

        // Сбрасываем состояние ожидания параметров перед их разбором
        currentStep = Step.INITIAL;
        context.clearActiveAction();

        if (args.length == 0) {
            return "Ошибка: параметры не введены. Операция отменена.";
        }

        Path workFile = context.getWorkFile();
        CarFileGenerator carFileGenerator = context.getCarFileGenerator();

        boolean append = false;
        boolean force = false;

        int endIdx = args.length;
        String lastArg = args[endIdx - 1].toLowerCase();
        if ("--append".equals(lastArg)) {
            append = true;
            endIdx--;
        } else if ("--force".equals(lastArg)) {
            force = true;
            endIdx--;
        }

        if (endIdx < 1) {
            return "Ошибка: укажите режим заполнения перед флагами.";
        }

        String[] parts = Arrays.copyOf(args, endIdx);
        String mode = parts[0].toLowerCase();

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
                    if (parts.length < 2) {
                        return "Ошибка: укажите количество машин, например: 'random 5'.";
                    }
                    int count = Integer.parseInt(parts[1]);

                    boolean finalAppendRandom = append;
                    action = () -> carFileGenerator.fillRandomSubset(workFile, count, finalAppendRandom);
                    successMessage = append
                            ? "Успешно: в рабочий файл добавлено случайных машин: " + count + "."
                            : "Успешно: рабочий файл заполнен случайными машинами (" + count + " шт.).";
                    break;

                case "car":
                    if (parts.length < 3) {
                        return "Ошибка: укажите марку и модель, например: 'car BMW X5'.";
                    }
                    String brand = parts[1];
                    String model = String.join(" ", Arrays.copyOfRange(parts, 2, parts.length));

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
                this.pendingOverwriteAction = action;
                this.currentStep = Step.WAITING_FOR_CONFIRMATION;
                context.setActiveAction(this);
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

    private String handleConfirmation(String[] args, AppContext context) {
        String answer = String.join(" ", args).trim().toLowerCase();

        if ("y".equals(answer) || "yes".equals(answer) || "д".equals(answer) || "да".equals(answer)) {
            Runnable action = this.pendingOverwriteAction;
            this.pendingOverwriteAction = null;
            this.currentStep = Step.INITIAL;
            context.clearActiveAction();
            try {
                action.run();
                return "Успешно: существующий файл перезаписан.";
            } catch (Exception e) {
                return "Ошибка: " + e.getMessage();
            }
        } else if ("n".equals(answer) || "no".equals(answer) || "н".equals(answer) || "нет".equals(answer)) {
            this.pendingOverwriteAction = null;
            this.currentStep = Step.INITIAL;
            context.clearActiveAction();
            return "Операция отменена.";
        } else {
            return "Пожалуйста, введите 'y' (да) для перезаписи или 'n' (нет) для отмены:";
        }
    }

    @Override
    public String getDescription() {
        return """
                fill all [--append | --force]                 - заполнить всеми машинами из каталога
                  fill random <кол-во> [--append | --force]     - заполнить случайными машинами
                  fill car <марка> <модель> [--append | --force] - заполнить конкретной машиной
                Флаги для fill:
                  --append  добавить новые машины в существующий файл без перезаписи
                  --force   перезаписать существующий файл без подтверждения""";
    }
}