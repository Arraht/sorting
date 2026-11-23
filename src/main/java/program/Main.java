package program;

import program.logger.*;

public class Main {

    public static void main(String[] args) {
        Logger logger = new Logger();

        try {
            CommandProcessor commandProcessor = new CommandProcessor();
            App app = new App(commandProcessor, logger);

            app.start();

        } catch (Exception e) {
            System.err.println("fatal error: application has crashed.");
            e.printStackTrace();
        } finally {
            logger.info("application has finished.");
        }
    }
}