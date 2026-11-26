package program;

import java.util.Scanner;
import program.logger.*;

public class App {

    private final CommandProcessor commandProcessor;
    private final Logger logger;
    private final Scanner scanner;
    private boolean isRunning;

    // Зависимости передаются через конструктор
    public App(CommandProcessor commandProcessor, Logger logger) {
        this.commandProcessor = commandProcessor;
        this.logger = logger;
        this.scanner = new Scanner(System.in);
        this.isRunning = false;
    }

    public void start() {
        isRunning = true;
        logger.info("The application is running. Enter 'exit' to terminate.");

        while (isRunning) {
            System.out.print("> ");
            String input = scanner.nextLine();

            if (!input.trim().isEmpty()) {
                logger.info("user input: " + input.trim());
            }

            String response = commandProcessor.process(input);

            if ("EXIT".equals(response)) {
                stop();
            } else if (!response.isEmpty()) {
                System.out.println(response);
            }
        }
    }

    private void stop() {
        isRunning = false;
        logger.info("terminating the application");
    }

    // Метод для корректного закрытия ресурсов (вызовем из main)
    public void close() {
        scanner.close();
        logger.info("scanner is closed");
    }
}