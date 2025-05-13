package backend.academy.bot.command;

import backend.academy.bot.ApiError;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class ListCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public ListCommandHandler(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/list";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        scrapperClient
                .getLinks(chatId)
                .subscribe(
                        response -> {
                            if (response.getLinks().isEmpty()) {
                                telegramClient.sendMessage(
                                        chatId,
                                        "📭 Ваш список отслеживаемых ссылок пуст.\n"
                                                + "Добавьте первую ссылку командой /track");
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append("📋 *Ваши отслеживаемые ссылки:*\n\n");

                                response.getLinks().forEach(link -> {
                                    sb.append("🔗 ").append(link.getUrl()).append("\n");
                                    sb.append("   └ ID: ").append(link.getId()).append("\n\n");
                                });

                                sb.append("Всего: ").append(response.getSize()).append(" ссылок");
                                telegramClient.sendMessage(chatId, sb.toString());
                            }
                        },
                        error -> sendErrorInfo(chatId, error, "⚠ Ошибка получения списка"));
    }

    private void sendErrorInfo(Long chatId, Throwable error, String msg) {
        if (error instanceof ApiError apiError) {
            telegramClient.sendMessage(chatId, msg + ":\n " + apiError.getDescription());
        } else {
            telegramClient.sendMessage(chatId, msg + ":\n " + error.getMessage());
        }
    }
}
