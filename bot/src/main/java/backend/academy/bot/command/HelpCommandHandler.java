package backend.academy.bot.command;

import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class HelpCommandHandler implements CommandHandler {

    private final TelegramClient telegramClient;

    public HelpCommandHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return BotCommand.HELP.getCommand();
    }

    @Override
    public void handle(Long chatId, String messageText) {
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("ℹ *Справка по командам:*\n\n");

        for (BotCommand command : BotCommand.values()) {
            helpMessage
                    .append("• *")
                    .append(command.getCommand())
                    .append("* - ")
                    .append(command.getDescription())
                    .append("\n");
        }

        helpMessage.append("\n📌 *Подсказка:* Вы можете нажать на команду, чтобы быстро её ввести!");
        telegramClient.sendMessage(chatId, helpMessage.toString());
    }
}
