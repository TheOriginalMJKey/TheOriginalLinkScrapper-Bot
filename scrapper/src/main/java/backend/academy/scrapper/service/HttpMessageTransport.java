package backend.academy.scrapper.service;

import backend.academy.scrapper.model.LinkUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.message-transport", havingValue = "HTTP")
public class HttpMessageTransport implements MessageTransport {
    private final RestTemplate restTemplate;
    
    @Override
    public void sendUpdate(LinkUpdate update) {
        restTemplate.postForObject("/updates", update, Void.class);
    }
} 