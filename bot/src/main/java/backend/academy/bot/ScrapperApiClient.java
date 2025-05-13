package backend.academy.bot;

import backend.academy.bot.model.AddLinkRequest;
import backend.academy.bot.model.LinkResponse;
import backend.academy.bot.model.ListLinksResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class ScrapperApiClient {

    private final BotConfig botConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public ScrapperApiClient(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    public void registerChat(long chatId) {
        String url = botConfig.scrapperApiUrl() + "/tg-chat/" + chatId;
        restTemplate.postForEntity(url, null, Void.class);
    }

    public void unregisterChat(long chatId) {
        String url = botConfig.scrapperApiUrl() + "/tg-chat/" + chatId;
        restTemplate.delete(url);
    }

    public void addLink(long chatId, String link, List<String> tags, List<String> filters) {
        String url = botConfig.scrapperApiUrl() + "/links";
        var request = new AddLinkRequest(link, tags, filters); // Убедитесь, что конструктор есть

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<AddLinkRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.postForEntity(url, entity, Void.class);
    }

    public void removeLink(long chatId, String link) {
        String url = botConfig.scrapperApiUrl() + "/links";
        var request = new RemoveLinkRequest(link); // Убедитесь, что конструктор есть

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        HttpEntity<RemoveLinkRequest> entity = new HttpEntity<>(request, headers);
        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }

    public List<LinkResponse> getLinks(long chatId) {
        String url = botConfig.scrapperApiUrl() + "/links";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Tg-Chat-Id", String.valueOf(chatId));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<ListLinksResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity, ListLinksResponse.class);
        return response.getBody().getLinks(); // Убедитесь, что ListLinksResponse имеет метод getLinks()
    }
}
