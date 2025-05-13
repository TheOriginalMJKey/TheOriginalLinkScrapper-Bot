package backend.academy.bot;

import com.github.pengrad.telegrambot.model.Message;
import com.github.pengrad.telegrambot.model.Update;
import com.github.pengrad.telegrambot.request.SendMessage;
import com.github.pengrad.telegrambot.response.SendResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelegramBotHandler {

    private final BotConfig botConfig;
    private final ScrapperApiClient scrapperApiClient;
    private final DialogStateManager stateManager;

    @Autowired
    public TelegramBotHandler(BotConfig botConfig, ScrapperApiClient scrapperApiClient, DialogStateManager stateManager) {
        this.botConfig = botConfig;
        this.scrapperApiClient = scrapperApiClient;
        this.stateManager = stateManager;
    }

    public void processUpdates(Update[] updates) {
        for (Update update : updates) {
            Message message = update.message();
            if (message != null && message.text() != null) {
                handleCommand(message.chat().id(), message.text());
            }
        }
    }

    private void handleCommand(long chatId, String command) {
        switch (command) {
            case "/start" -> handleStart(chatId);
            case "/help" -> handleHelp(chatId);
            case "/track" -> handleTrack(chatId);
            case "/untrack" -> handleUntrack(chatId);
            case "/list" -> handleList(chatId);
            default -> sendTextMessage(chatId, "Неизвестная команда. Введите /help для справки.");
        }
    }

    private void handleStart(long chatId) {
        scrapperApiClient.registerChat(chatId);
        sendTextMessage(chatId, "Добро пожаловать! Отправьте /help для справки.");
    }

    private void handleHelp(long chatId) {
        StringBuilder helpMessage = new StringBuilder("Доступные команды:\n");
        helpMessage.append("/start - Регистрация\n");
        helpMessage.append("/help - Справка\n");
        helpMessage.append("/track - Начать отслеживание ссылки\n");
        helpMessage.append("/untrack - Прекратить отслеживание ссылки\n");
        helpMessage.append("/list - Показать список отслеживаемых ссылок\n");
        sendTextMessage(chatId, helpMessage.toString());
    }

    private void handleTrack(long chatId) {
        stateManager.setState(chatId, DialogStateManager.DialogState.WAITING_LINK);
        sendTextMessage(chatId, "Введите ссылку для отслеживания:");
    }

    private void handleUntrack(long chatId) {
        stateManager.setState(chatId, DialogStateManager.DialogState.WAITING_UNTRACK_LINK);
        sendTextMessage(chatId, "Введите ссылку для прекращения отслеживания:");
    }

    private void handleList(long chatId) {
        var links = scrapperApiClient.getLinks(chatId);
        if (links.isEmpty()) {
            sendTextMessage(chatId, "Список отслеживаемых ссылок пуст.");
        } else {
            StringBuilder listMessage = new StringBuilder("Отслеживаемые ссылки:\n");
            for (var link : links) {
                listMessage.append("- ").append(link.getUrl()).append("\n");
            }
            sendTextMessage(chatId, listMessage.toString());
        }
    }

    private void sendTextMessage(long chatId, String text) {
        var bot = TelegramBotAdapter.build(botConfig.telegramToken());
        SendResponse response = bot.execute(new SendMessage(chatId, text));
        if (!response.isOk()) {
            System.err.println("Failed to send message: " + response.description());
        }
    }
}
