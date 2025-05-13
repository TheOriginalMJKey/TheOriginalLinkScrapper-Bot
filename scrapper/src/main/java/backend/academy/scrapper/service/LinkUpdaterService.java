package backend.academy.scrapper.service;

import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.response.LinkUpdate;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.sender.UpdateSender;
import backend.academy.scrapper.util.LinkSourceUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class LinkUpdaterService {

    @Value("${app.link-update-batch-size}")
    private Integer batchSize;

    @Value("${app.link-age}")
    private Integer linkAgeInMinutes;

    private final LinkService linkService;
    private final UpdateSender updateSender;
    private final Map<String, LinkUpdateHandler> linkUpdateHandlers;
    private final LinkUpdateManager linkUpdateManager;

    public LinkUpdaterService(
            LinkService linkService,
            UpdateSender updateSender,
            List<LinkUpdateHandler> linkUpdateHandlers,
            LinkUpdateManager linkUpdateManager) {
        this.linkService = linkService;
        this.updateSender = updateSender;
        this.linkUpdateHandlers = linkUpdateHandlers.stream()
                .collect(Collectors.toMap(it -> it.getClass().getCanonicalName(), Function.identity()));
        this.linkUpdateManager = linkUpdateManager;
    }

    @Transactional
    public void updateLinks() {
        List<Link> updates = linkService.getLinksToUpdate(linkAgeInMinutes, batchSize);
        updates.forEach(this::processLinkUpdate);
    }

    private void processLinkUpdate(Link link) {
        OffsetDateTime now = OffsetDateTime.now();
        LinkSourceUtil.getLinkSource(link.getLinkType())
                .flatMap(it -> getLinkUpdateHandler(link, it))
                .ifPresentOrElse(
                        handler -> {
                            try {
                                Optional<backend.academy.scrapper.handler.LinkUpdate> updateDetails =
                                        handler.getLinkUpdate(link);

                                updateDetails.ifPresent(details -> {
                                    log.info("Found updates for link: {}", link.getUrl());

                                    Optional<backend.academy.scrapper.entity.LinkUpdate> updateEntity =
                                            linkUpdateManager.createUpdate(
                                                    link,
                                                    details.id(),
                                                    details.type(),
                                                    details.title(),
                                                    details.username(),
                                                    details.createdAt(),
                                                    details.description());

                                    updateEntity.ifPresent(update -> {
                                        String message = linkUpdateManager.formatUpdateMessage(update);

                                        notifyBot(link, message);

                                        linkUpdateManager.markAsProcessed(update);
                                    });

                                    linkService.updateCheckedAt(link, now);
                                });
                            } catch (RuntimeException ex) {
                                log.error("Error processing link {}: {}", link.getUrl(), ex.getMessage());
                                linkService.updateCheckedAt(link, now.minusMinutes(5));
                            }
                        },
                        () -> log.error("No update handler for link: {}", link.getUrl()));
    }

    private Optional<LinkUpdateHandler> getLinkUpdateHandler(Link link, ScrapperConfig.LinkSource linkSource) {
        return linkSource.handlers().values().stream()
                .filter(it -> Pattern.matches("https://" + linkSource.domain() + it.regex(), link.getUrl()))
                .map(ScrapperConfig.LinkSourceHandler::handler)
                .map(linkUpdateHandlers::get)
                .findFirst();
    }

    public void notifyBot(Link link, String message) {
        List<Long> tgChatIds = link.getChats().stream()
                .map(Chat::getChatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tgChatIds.isEmpty()) {
            log.warn("No active chats for link: {}", link.getUrl());
            return;
        }

        String[] parts = message.split("\\|\\|");
        String type = parts.length > 0 ? parts[0] : "UNKNOWN";
        String title = parts.length > 1 ? parts[1] : "Untitled";
        String author = parts.length > 2 ? parts[2] : "Unknown";
        String time = parts.length > 3 ? parts[3] : OffsetDateTime.now().toString();
        String description = parts.length > 4 ? parts[4] : "";
        String url = parts.length > 5 ? parts[5] : link.getUrl();

        LinkUpdate update = new LinkUpdate(
                link.getId(),
                URI.create(url),
                description,
                type,
                title,
                author,
                OffsetDateTime.parse(time.replace(" ", "T") + "Z"),
                tgChatIds);

        boolean isSent = updateSender.send(update);
        log.info("Sent update for {} chats: {}", tgChatIds.size(), isSent);
    }

    private void handleClientExceptionOnLinkUpdate(RuntimeException ex, Link link) {
        log.error("Client error on link update: {}", ex.getMessage());
        if (ex instanceof WebClientResponseException clientExc) {
            HttpStatusCode status = clientExc.getStatusCode();
            if (status.equals(HttpStatus.NOT_FOUND) || status.equals(HttpStatus.BAD_REQUEST)) {
                linkService.updateLinkStatus(link, LinkStatus.BROKEN);
            }
        }
    }

    public void updateLink(Link link) {
        OffsetDateTime now = OffsetDateTime.now();
        try {
            Optional<backend.academy.scrapper.handler.LinkUpdate> update = getLinkUpdateHandler(
                            link,
                            LinkSourceUtil.getLinkSource(link.getLinkType()).orElse(null))
                    .map(handler -> handler.getLinkUpdate(link))
                    .orElse(Optional.empty());

            if (update.isPresent()) {
                backend.academy.scrapper.handler.LinkUpdate handlerUpdate =
                        update.orElseThrow(() -> new IllegalStateException("Update is empty"));

                // Create the update record
                Optional<backend.academy.scrapper.entity.LinkUpdate> updateEntity = linkUpdateManager.createUpdate(
                        link,
                        handlerUpdate.id(),
                        handlerUpdate.type(),
                        handlerUpdate.title(),
                        handlerUpdate.username(),
                        handlerUpdate.createdAt(),
                        handlerUpdate.description());

                updateEntity.ifPresent(entity -> {
                    String message = linkUpdateManager.formatUpdateMessage(entity);

                    notifyBot(link, message);

                    linkUpdateManager.markAsProcessed(entity);

                    linkService.updateLastUpdateId(link.getId(), handlerUpdate.id());
                });

                linkService.updateCheckedAt(link, now);
            }
        } catch (Exception e) {
            log.error("Error updating link {}: {}", link.getUrl(), e.getMessage());
            linkService.updateCheckedAt(link, now.minusMinutes(5));
        }
    }
}
