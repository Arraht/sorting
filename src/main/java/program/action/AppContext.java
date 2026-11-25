package program.action;

import program.service.CarFileGenerator;
import java.nio.file.Path;

public class AppContext {
    private final CarFileGenerator carFileGenerator;
    private Path workFile;
    private Action activeAction;

    public AppContext(CarFileGenerator carFileGenerator, Path workFile) {
        this.carFileGenerator = carFileGenerator;
        this.workFile = workFile;
    }

    public CarFileGenerator getCarFileGenerator() {
        return carFileGenerator;
    }

    public Path getWorkFile() {
        return workFile;
    }

    public void setWorkFile(Path workFile) {
        this.workFile = workFile;
    }

    public Action getActiveAction() {
        return activeAction;
    }

    public void setActiveAction(Action activeAction) {
        this.activeAction = activeAction;
    }

    public void clearActiveAction() {
        this.activeAction = null;
    }
}