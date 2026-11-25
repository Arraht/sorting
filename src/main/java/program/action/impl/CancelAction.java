package program.action.impl;

import program.action.Action;
import program.action.AppContext;

public class CancelAction implements Action {

    @Override
    public String execute(String[] args, AppContext context) {
        if (context.getActiveAction() == null) {
            return "Нет активной операции для отмены.";
        }
        context.clearActiveAction();
        return "Операция отменена.";
    }

    @Override
    public String getDescription() {
        return "cancel                                        - отменить текущий пошаговый ввод";
    }
}