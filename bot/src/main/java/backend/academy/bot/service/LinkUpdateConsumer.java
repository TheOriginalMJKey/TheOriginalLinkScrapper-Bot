package backend.academy.bot.service;

import backend.academy.bot.model.LinkUpdate;
import backend.academy.bot.model.LinkUpdate.UpdateType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class LinkUpdateConsumer {
    private final LinkCacheService cacheService;
    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    
    @Value("${kafka.topics.dead-letter}")
    private String deadLetterTopic;
    
    @KafkaListener(topics = "${kafka.topics.updates}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUpdate(LinkUpdate update) {
        try {
            processUpdate(update);
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
            // Send to dead letter queue
            kafkaTemplate.send(deadLetterTopic, update.getTgChatId().toString(), update);
        }
    }
    
    private void processUpdate(LinkUpdate update) {
        switch (update.getUpdateType()) {
            case ADD:
                cacheService.addLink(update.getTgChatId(), update.getUrl());
                break;
            case REMOVE:
                cacheService.removeLink(update.getTgChatId(), update.getUrl());
                break;
            case UPDATE:
                cacheService.invalidateCache(update.getTgChatId());
                break;
        }
    }
} 