package program.action.impl;

import program.action.Action;
import program.action.AppContext;

public class ExitAction implements Action {
    @Override
    public String execute(String[] args, AppContext context) {
        System.out.println("Exiting program LVN");
        return "EXIT";
    }

    @Override
    public String getDescription() {
        return "exit                                          - выход из приложения";
    }
}