package program.action.impl;

import program.action.Action;
import program.action.ActionFactory;
import program.action.AppContext;

public class HelpAction implements Action {
    private final ActionFactory actionFactory;

    public HelpAction(ActionFactory actionFactory) {
        this.actionFactory = actionFactory;
    }

    @Override
    public String execute(String[] args, AppContext context) {
        StringBuilder sb = new StringBuilder("Доступные команды:\n");

        for (Action action : actionFactory.getAllActions()) {
            String desc = action.getDescription();
            if (!desc.isEmpty()) {
                sb.append("  ").append(desc).append("\n");
            }
        }

        return sb.toString().trim();
    }

    @Override
    public String getDescription() {
        return "help                                          - показать список доступных команд";
    }
}