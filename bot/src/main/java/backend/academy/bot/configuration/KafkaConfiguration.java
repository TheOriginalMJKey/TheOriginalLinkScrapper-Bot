package backend.academy.bot.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(name = "app.message-transport", havingValue = "KAFKA")
public class KafkaConfiguration {
    @Value("${kafka.topics.updates}")
    private String updatesTopic;
    
    @Value("${kafka.topics.notifications}")
    private String notificationsTopic;
    
    @Value("${kafka.topics.dead-letter}")
    private String deadLetterTopic;
    
    @Bean
    public NewTopic updatesTopic() {
        return TopicBuilder.name(updatesTopic)
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(notificationsTopic)
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(deadLetterTopic)
            .partitions(1)
            .replicas(1)
            .build();
    }
} 