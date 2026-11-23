package program;

public class CommandProcessor {

    private static final String EXIT_COMMAND = "exit";

    public String process(String command) {
        String normalized = command.trim().toLowerCase();

        if (normalized.isEmpty()) {
            return ""; // Пустой ввод, ничего не делаем
        }

        if (EXIT_COMMAND.equals(normalized)) {
            return "EXIT";
        }

        // Здесь будет логика других команд
        if ("test".equals(normalized)) {
            return "test message";
        }

        return "unknown command";
    }
}