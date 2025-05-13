package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import backend.academy.bot.command.BotCommand;
import backend.academy.bot.dto.TelegramResponse;
import backend.academy.bot.dto.TelegramUpdate;
import backend.academy.bot.state.StateMachine;
import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class TelegramPollingService {
    private final BotConfig botConfig;
    private final WebClient webClient;
    private final StateMachine stateMachine;
    private long lastUpdateId = 0;

    public TelegramPollingService(BotConfig botConfig, WebClient.Builder webClientBuilder, StateMachine stateMachine) {
        this.botConfig = botConfig;
        this.stateMachine = stateMachine;
        this.webClient = webClientBuilder.baseUrl(botConfig.telegramApiUrl()).build();
    }

    @PostConstruct
    public void startPolling() {
        setMyCommands(); // Регистрируем команды при старте
        new Thread(this::pollUpdatesLoop).start(); // Запускаем polling в отдельном потоке
    }

    private void setMyCommands() {
        String token = botConfig.telegramToken();
        String url = "/bot" + token + "/setMyCommands";

        // Создаем список команд
        List<Map<String, String>> commands = Arrays.stream(BotCommand.values())
                .map(command -> Map.of("command", command.getCommand(), "description", command.getDescription()))
                .collect(Collectors.toList());

        // Отправляем запрос к Telegram API
        webClient
                .post()
                .uri(url)
                .bodyValue(Map.of("commands", commands))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private void pollUpdatesLoop() {
        while (true) { // Бесконечный цикл для polling
            try {
                pollUpdates(); // Запрашиваем обновления
                Thread.sleep(1000); // Задержка между запросами (1 секунда)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                // Логируем ошибку и продолжаем работу
                System.err.println("Ошибка при запросе обновлений: " + e.getMessage());
            }
        }
    }

    private void pollUpdates() {
        String token = botConfig.telegramToken();
        String url = "/bot" + token + "/getUpdates?offset=" + (lastUpdateId + 1);

        TelegramResponse response = webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(TelegramResponse.class)
                .block();

        if (response != null && response.isOk() && response.getResult() != null) {
            for (TelegramUpdate update : response.getResult()) {
                if (update.getUpdateId() > lastUpdateId) {
                    lastUpdateId = update.getUpdateId();
                }
                if (update.getMessage() != null && update.getMessage().getText() != null) {
                    Long chatId = update.getMessage().getChat().getId();
                    String text = update.getMessage().getText();
                    stateMachine.start(chatId, text);
                }
            }
        }
    }
}
