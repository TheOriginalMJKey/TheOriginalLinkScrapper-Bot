package backend.academy.bot.contoller;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.state.StateMachine;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.validation.Valid;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressFBWarnings("SLF4J_PLACE_HOLDER_MISMATCH")
public class LinkUpdateController {

    private final StateMachine stateMachine;
    private final Logger logger = LoggerFactory.getLogger(LinkUpdateController.class);

    public LinkUpdateController(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    @PostMapping("/updates")
    public ResponseEntity<Void> receiveUpdate(@Valid @RequestBody LinkUpdate update) {
        logger.info(
                "Получено обновление: type={}, title={}, author={}, createdAt={}, description={}, url={}",
                update.getType(),
                update.getTitle(),
                update.getAuthor(),
                update.getCreatedAt(),
                update.getDescription(),
                update.getUrl());

        if (update.getTgChatIds() == null || update.getTgChatIds().isEmpty()) {
            logger.warn("Получено обновление без chat_id");
            return ResponseEntity.badRequest().build();
        }

        try {
            for (Long chatId : update.getTgChatIds()) {
                logger.info("Отправка обновления для чата {}", chatId);
                stateMachine.notifyUser(chatId, update);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Ошибка при обработке обновления", StructuredArguments.keyValue("error", e.getMessage()));
            return ResponseEntity.badRequest().build();
        }
    }
}
