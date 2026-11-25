package program.action.impl;

import program.action.Action;
import program.action.AppContext;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileAction implements Action {

    @Override
    public String execute(String[] args, AppContext context) {
        Path currentWorkFile = context.getWorkFile();

        if (args.length == 0) {
            return "Текущий рабочий файл: " + currentWorkFile
                    + (Files.exists(currentWorkFile) ? " (существует)" : " (ещё не создан)");
        }

        String fileName = args[0].trim();

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return "Ошибка: укажите только имя файла без путей (например: file garage_1).";
        }

        if (!fileName.toLowerCase().endsWith(".json")) {
            fileName += ".json";
        }

        Path parentDir = currentWorkFile.getParent() != null ? currentWorkFile.getParent() : Path.of("data");
        Path newWorkFile = parentDir.resolve(fileName);
        context.setWorkFile(newWorkFile);

        String status = Files.exists(newWorkFile)
                ? "файл уже существует (можно сортировать или дописывать)."
                : "новый файл (будет создан при выполнении команды 'fill').";

        return "Рабочий файл переключён на '" + newWorkFile + "' — " + status;
    }

    @Override
    public String getDescription() {
        return "file [имя_файла]                              - показать текущий или переключить рабочий файл";
    }
}