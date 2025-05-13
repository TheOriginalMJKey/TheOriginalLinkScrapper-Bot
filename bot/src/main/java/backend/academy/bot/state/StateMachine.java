package backend.academy.bot.state;

import backend.academy.bot.ApiError;
import backend.academy.bot.command.BotCommand;
import backend.academy.bot.command.CommandHandler;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.MessageFormatter;
import backend.academy.bot.service.ScrapperClient;
import backend.academy.bot.service.TelegramClient;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@SuppressFBWarnings({"SLF4J_PLACE_HOLDER_MISMATCH", "VA_FORMAT_STRING_USES_NEWLINE"})
public class StateMachine {
    public final Map<Long, Conversation> conversations = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;
    private final ScrapperClient scrapperClient;
    private final MessageFormatter messageFormatter;
    private final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    private final Map<String, CommandHandler> commandHandlers = new HashMap<>();

    public StateMachine(
            TelegramClient telegramClient,
            ScrapperClient scrapperClient,
            MessageFormatter messageFormatter,
            List<CommandHandler> handlers) {
        this.telegramClient = telegramClient;
        this.scrapperClient = scrapperClient;
        this.messageFormatter = messageFormatter;
        for (CommandHandler handler : handlers) {
            commandHandlers.put(handler.getCommand().toLowerCase(), handler);
        }
    }

    public void start(Long chatId, String messageText) {
        Conversation conv = conversations.computeIfAbsent(chatId, id -> new Conversation());
        State state = conv.getState();
        if (state == State.START) {
            startScript(chatId, messageText, conv);
            return;
        }
        if (state == State.COMMAND_WAITING) {
            processCommand(chatId, messageText, conv);
            return;
        }
        switch (state) {
            case ADD_LINK:
                addLinkScript(chatId, messageText, conv);
                break;
            case ASK_ABT_TAG:
                askAbtTagScript(chatId, messageText, conv);
                break;
            case WAITING_FOR_TAGS:
                waitingForTagsScript(chatId, messageText, conv);
                break;
            case WAITING_FOR_FILTERS:
                waitingForFiltersScript(chatId, messageText, conv);
                break;
            default:
                telegramClient.sendMessage(
                        chatId,
                        "Неверное состояние. Введите " + BotCommand.HELP.getCommand()
                                + " для получения списка команд.");
                conv.reset();
                logger.error("Недопустимое состояние", StructuredArguments.keyValue("chatId", chatId));
        }
    }

    private void waitingForFiltersScript(Long chatId, String messageText, Conversation conv) {
        conv.setFilters(parseInput(messageText));
        completeTrack(chatId, conv, conv.getTags(), conv.getFilters());
    }

    private void waitingForTagsScript(Long chatId, String messageText, Conversation conv) {
        conv.setTags(parseInput(messageText));
        conv.setState(State.WAITING_FOR_FILTERS);
        telegramClient.sendMessage(chatId, "Введите фильтры (через пробел):");
    }

    private void askAbtTagScript(Long chatId, String messageText, Conversation conv) {
        if (messageText.trim().equalsIgnoreCase("n")) {
            completeTrack(chatId, conv, Collections.emptyList(), Collections.emptyList());
        } else if (messageText.trim().equalsIgnoreCase("y")) {
            conv.setState(State.WAITING_FOR_TAGS);
            telegramClient.sendMessage(chatId, "Введите теги (через пробел):");
        } else {
            telegramClient.sendMessage(chatId, "Пожалуйста, ответьте 'y' или 'n'.");
        }
    }

    private void addLinkScript(Long chatId, String messageText, Conversation conv) {
        conv.setLink(messageText.trim());
        conv.setState(State.ASK_ABT_TAG);
        telegramClient.sendMessage(chatId, "Хотите настроить теги и фильтры? (y/n)");
    }

    private void startScript(Long chatId, String messageText, Conversation conv) {
        if (messageText.trim().equalsIgnoreCase(BotCommand.START.getCommand())) {
            conv.setState(State.COMMAND_WAITING);
            commandHandlers.get(BotCommand.START.getCommand()).handle(chatId, messageText);
        } else {
            telegramClient.sendMessage(
                    chatId, "Введите " + BotCommand.START.getCommand() + " для начала работы с ботом.");
        }
    }

    private void processCommand(Long chatId, String messageText, Conversation conv) {
        String lower = messageText.trim().toLowerCase();
        if (lower.startsWith(BotCommand.START.getCommand())) {
            conv.setState(State.COMMAND_WAITING);
            commandHandlers.get(BotCommand.START.getCommand()).handle(chatId, messageText);
        } else if (lower.startsWith(BotCommand.TRACK.getCommand())) {
            String[] parts = messageText.split(" ", 2);
            if (parts.length >= 2 && !parts[1].isBlank()) {
                addLinkScript(chatId, parts[1], conv);
            } else {
                conv.setState(State.ADD_LINK);
                telegramClient.sendMessage(chatId, "Введите ссылку для отслеживания:");
            }
        } else if (lower.startsWith(BotCommand.HELP.getCommand())
                || lower.startsWith(BotCommand.LIST.getCommand())
                || lower.startsWith(BotCommand.UNTRACK.getCommand())) {
            commandHandlers.get(lower.split(" ")[0]).handle(chatId, messageText);
            conv.reset();
        } else {
            telegramClient.sendMessage(
                    chatId, "Неизвестная команда. Введите " + BotCommand.HELP.getCommand() + " для списка команд.");
            logger.info(
                    "Неизвестная команда: ",
                    StructuredArguments.keyValue("message", messageText),
                    StructuredArguments.keyValue("chatId", chatId));
        }
    }

    private void completeTrack(Long chatId, Conversation conv, List<String> tags, List<String> filters) {
        String link = conv.getLink();
        Mono<LinkResponse> result = scrapperClient.addLink(chatId, link, tags, filters);
        result.subscribe(
                response -> telegramClient.sendMessage(chatId, "Ссылка успешно добавлена: " + response.getUrl()),
                error -> sendErrorInfo(chatId, error));
        conv.reset();
    }

    private void sendErrorInfo(Long chatId, Throwable error) {
        if (error instanceof ApiError apiError) {
            telegramClient.sendMessage(chatId, "Ошибка при добавлении ссылки: " + apiError.getDescription());
        } else {
            telegramClient.sendMessage(chatId, "Ошибка при добавлении ссылки: " + error.getMessage());
        }
    }

    private List<String> parseInput(String input) {
        return Arrays.stream(input.trim().split("\\s+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    public void notifyUser(Long chatId, LinkUpdate update) {
        // Format the message using the MessageFormatter
        String message = messageFormatter.formatUpdateMessage(update);
        telegramClient.sendMessage(chatId, message);
    }
}
