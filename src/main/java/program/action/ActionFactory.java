package program.action;

import program.action.impl.*;

import java.util.*;
import java.util.function.Supplier;

public class ActionFactory {
    private final Map<String, Supplier<Action>> creators = new LinkedHashMap<>();
    private final Set<String> interruptCommands = Set.of("exit", "cancel");

    public ActionFactory() {
        register("help", () -> new HelpAction(this));
        register("view", ViewAction::new);
        register("file", FileAction::new);
        register("fill", FillAction::new);
        register("sort", SortAction::new);
        register("count", CountAction::new);
        register("cancel", CancelAction::new);
        register("exit", ExitAction::new);
    }

    private void register(String name, Supplier<Action> creator) {
        creators.put(name.toLowerCase(), creator);
    }

    public Optional<Action> getAction(String commandName) {
        Supplier<Action> creator = creators.get(commandName.toLowerCase());
        return creator != null ? Optional.of(creator.get()) : Optional.empty();
    }

    public boolean isInterruptCommand(String commandName) {
        return interruptCommands.contains(commandName.toLowerCase());
    }

    public Collection<Action> getAllActions() {
        List<Action> list = new ArrayList<>();
        for (Supplier<Action> creator : creators.values()) {
            list.add(creator.get());
        }
        return list;
    }
}

//public class ActionFactory {
//    // LinkedHashMap сохраняет порядок добавления команд для красивого вывода в help
//    private final Map<String, Action> actions = new LinkedHashMap<>();
//
//    public ActionFactory() {
//        register("help", new HelpAction(this));
//        register("file", new FileAction());
//        register("fill", new FillAction());
//        register("sort", new SortAction());
//        register("exit", new ExitAction());
//    }
//
//    private void register(String name, Action action) {
//        actions.put(name.toLowerCase(), action);
//    }
//
//    public Optional<Action> getAction(String commandName) {
//        return Optional.ofNullable(actions.get(commandName.toLowerCase()));
//    }
//
//    public Collection<Action> getAllActions() {
//        return actions.values();
//    }
//}