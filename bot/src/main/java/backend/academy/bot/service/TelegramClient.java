package backend.academy.bot.service;

import backend.academy.bot.BotConfig;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.HashMap;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@SuppressFBWarnings({"SLF4J_FORMAT_SHOULD_BE_CONST", "SLF4J_PLACE_HOLDER_MISMATCH"})
public class TelegramClient {

    private final BotConfig botConfig;
    private final WebClient webClient;
    private final Logger logger = LoggerFactory.getLogger(TelegramClient.class);

    public TelegramClient(BotConfig botConfig, WebClient.Builder webClientBuilder) {
        this.botConfig = botConfig;
        this.webClient = webClientBuilder.baseUrl("https://api.telegram.org").build();
    }

    public void sendMessage(Long chatId, String text) {
        if (chatId == null) {
            logger.error("Cannot send message: chatId is null");
            return;
        }

        if (text == null || text.isEmpty()) {
            logger.error("Cannot send message: text is empty");
            return;
        }

        String token = botConfig.telegramToken();
        String url = "/bot" + token + "/sendMessage";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("chat_id", chatId);
        requestBody.put("text", text);
        requestBody.put("parse_mode", "Markdown");

        logger.debug("Sending message to chat {}: {}", chatId, text);

        webClient
                .post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> logger.info(
                                "Сообщение успешно отправлено", StructuredArguments.keyValue("response", response)),
                        error -> {
                            if (error instanceof WebClientResponseException clientExc) {
                                logger.error(
                                        "Ошибка при отправке сообщения: {} - {}",
                                        clientExc.getStatusCode(),
                                        clientExc.getResponseBodyAsString());
                            } else {
                                logger.error("Ошибка при отправке сообщения: {}", error.getMessage());
                            }
                        });
    }
}
