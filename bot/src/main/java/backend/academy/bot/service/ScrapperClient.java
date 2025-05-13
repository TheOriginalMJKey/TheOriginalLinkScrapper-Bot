package backend.academy.bot.service;

import backend.academy.bot.ApiError;
import backend.academy.bot.dto.AddLinkRequest;
import backend.academy.bot.dto.ApiErrorResponse;
import backend.academy.bot.dto.LinkResponse;
import backend.academy.bot.dto.ListLinksResponse;
import backend.academy.bot.dto.RemoveLinkRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@SuppressFBWarnings("UnusedFormalParameter")
public class ScrapperClient {

    private final WebClient webClient;

    public ScrapperClient(WebClient.Builder webClientBuilder, @Value("${scrapper.base-url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    public Mono<LinkResponse> addLink(Long tgChatId, String link, List<String> tags, List<String> filters) {
        AddLinkRequest request = new AddLinkRequest(link, tags, filters);
        return webClient
                .post()
                .uri("/links")
                .header("Tg-Chat-Id", tgChatId.toString())
                .bodyValue(request)
                .exchangeToMono(response -> getResponseMono(response, LinkResponse.class));
    }

    public Mono<LinkResponse> removeLink(Long tgChatId, String link) {
        RemoveLinkRequest request = new RemoveLinkRequest(link);
        return webClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header("Tg-Chat-Id", tgChatId.toString())
                .bodyValue(request)
                .exchangeToMono(response -> getResponseMono(response, LinkResponse.class));
    }

    public Mono<ListLinksResponse> getLinks(Long tgChatId) {
        return webClient
                .get()
                .uri("/links")
                .header("Tg-Chat-Id", tgChatId.toString())
                .exchangeToMono(response -> getResponseMono(response, ListLinksResponse.class));
    }

    public Mono<Void> registerChat(Long tgChatId) {
        return webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/tg-chat/{id}").build(tgChatId))
                .exchangeToMono(response -> getResponseMono(response, Void.class));
    }

    public Mono<Void> deleteChat(Long tgChatId) {
        return webClient
                .delete()
                .uri(uriBuilder -> uriBuilder.path("/tg-chat/{id}").build(tgChatId))
                .exchangeToMono(response -> getResponseMono(response, Void.class));
    }

    private static <T> @NotNull Mono<T> getResponseMono(ClientResponse response, Class<T> responseType) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(responseType);
        } else {
            return response.bodyToMono(ApiErrorResponse.class)
                    .flatMap(apiErrorResponse -> Mono.error(new ApiError(
                            apiErrorResponse.getDescription(),
                            apiErrorResponse.getCode(),
                            apiErrorResponse.getExceptionName(),
                            apiErrorResponse.getExceptionMessage(),
                            apiErrorResponse.getStackTrace())));
        }
    }
}
