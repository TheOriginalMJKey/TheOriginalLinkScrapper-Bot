package backend.academy.bot.command;

import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import org.springframework.stereotype.Component;

@Component
public class StartCommandHandler implements CommandHandler {

    private final ScrapperClient scrapperClient;
    private final TelegramClient telegramClient;

    public StartCommandHandler(ScrapperClient scrapperClient, TelegramClient telegramClient) {
        this.scrapperClient = scrapperClient;
        this.telegramClient = telegramClient;
    }

    @Override
    public String getCommand() {
        return "/start";
    }

    @Override
    public void handle(Long chatId, String messageText) {
        // Регистрируем чат в Scrapper
        scrapperClient
                .registerChat(chatId)
                .doOnSuccess(unused -> telegramClient.sendMessage(chatId, getWelcomeMessage()))
                .doOnError(error -> telegramClient.sendMessage(chatId, "Ошибка регистрации: " + error.getMessage()))
                .subscribe();
    }

    private String getWelcomeMessage() {
        return """
           🌟 *Добро пожаловать в LinkTrackerBot!* 🌟

           Я помогу отслеживать изменения на:
           • GitHub репозиториях
           • StackOverflow вопросах

           📌 *Основные команды:*
           /track - добавить новую ссылку
           /untrack - удалить ссылку
           /list - показать все отслеживаемые ссылки

           ✨ *Пример использования:*
           1. `/track https://github.com/user/repo`
           2. Я начну проверять обновления
           3. Как только что-то изменится - пришлю уведомление!

           🔍 Попробуйте добавить первую ссылку!
           """;
    }
}
