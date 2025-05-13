package backend.academy.scrapper.service;

import backend.academy.scrapper.model.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaMessageTransport implements MessageTransport {
    private final KafkaTemplate<String, LinkUpdate> kafkaTemplate;
    
    @Value("${kafka.topics.updates}")
    private String updatesTopic;
    
    @Override
    public void sendUpdate(LinkUpdate update) {
        kafkaTemplate.send(updatesTopic, update.getTgChatId().toString(), update);
    }
} 