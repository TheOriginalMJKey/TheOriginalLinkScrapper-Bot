package backend.academy.bot.command;

public enum BotCommand {
    START("/start", "🚀 Начать работу"),
    HELP("/help", "❓ Помощь по командам"),
    TRACK("/track", "➕ Добавить ссылку"),
    UNTRACK("/untrack", "➖ Удалить ссылку"),
    LIST("/list", "📋 Список отслеживаемых ссылок");

    private final String command;
    private final String description;

    BotCommand(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }
}
