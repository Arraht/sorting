package program;

import program.logger.Logger;
import program.service.CarFileGenerator;

import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        Logger logger = new Logger();
        App app = null;

        
        try {
            Path workFile = Path.of("data/user_cars.json");
            CarFileGenerator carFileGenerator = new CarFileGenerator();

            CommandProcessor commandProcessor = new CommandProcessor(carFileGenerator, workFile);
            app = new App(commandProcessor, logger);

            app.start();

        } catch (Exception e) {
            System.err.println("fatal error: application has crashed.");
            e.printStackTrace();
        } finally {
            if (app != null) {
                app.close();
            }
            logger.info("application has finished.");
        }
    }
}