package program.logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    // Уровни логирования для будущей реализации
    public enum Level {
        INFO, WARN, ERROR, DEBUG
    }

    // INFO
    public void info(String message) {
        log(Level.INFO, message);
    }

    // ERROR
    public void error(String message, Exception e) {
        log(Level.ERROR, message);
        if (e != null) {
            e.printStackTrace();
        }
    }

    private void log(Level level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.println("[" + timestamp + "] [" + level + "] " + message);
    }
}