package program.action;

public interface Action {
    /**
     * @param args аргументы команды (массив слов без самой команды)
     * @param context общее состояние приложения
     * @return строка для вывода пользователю или "EXIT" для завершения
     */
    String execute(String[] args, AppContext context);

    /**
     * Возвращает краткую справку по использованию команды для меню help.
     */
    default String getDescription() {
        return "";
    }
}