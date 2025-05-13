package backend.academy.scrapper.service.storage;

import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.util.LinkParser;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
@SuppressWarnings("PMD.UnusedPrivateMethod")
public class LinkServiceImp implements LinkService {

    private final LinkRepository linkRepository;
    private final ChatServiceImp chatServiceImp;

    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        List<Link> links = linkRepository.findAllWithStatusAndOlderThan(
                LinkStatus.ACTIVE, OffsetDateTime.now().minusMinutes(minutes), PageRequest.of(0, limit));
        return links;
    }

    @Transactional
    @Override
    public void updateLinkStatus(Link link, LinkStatus status) {
        log.debug("link{id={}} status was changed to {}", link.id(), status.name());
        link.status(status);
    }

    @Transactional
    @Override
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        log.debug("link{id={}} was updated at {}", link.id(), checkedAt);
        link.checkedAt(checkedAt);
    }

    @Transactional
    @Override
    public void updateLastUpdateId(Long linkId, String updateId) {
        log.debug("link{id={}} last update id was changed to {}", linkId, updateId);
        linkRepository.findById(linkId).ifPresent(link -> link.lastUpdateId(updateId));
    }

    @Transactional
    @Override
    public LinkResponse addLinkToChat(Long chatId, URI url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatServiceImp.findByChatId(chatId);

        // Проверка на существующую ссылку
        Optional<Link> existingLink = chat.findLinkByUrl(parsedLink.url());
        if (existingLink.isPresent()) {
            throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(parsedLink.url());
        }

        Link link = linkRepository.findByUrl(parsedLink.url()).orElseGet(() -> linkRepository.save(parsedLink));

        chat.addLink(link);
        log.debug("Added link{id={}} to chat{id={}}", link.id(), chat.id());
        return new LinkResponse(link.id(), url);
    }

    @Transactional
    @Override
    public LinkResponse removeLinkFromChat(Long chatId, URI url) {
        Link parsedLink = LinkParser.parseLink(url);
        Chat chat = chatServiceImp.findByChatId(chatId);
        Link link = processLinkForDeletion(parsedLink, chat);
        chat.removeLink(link);
        log.debug("remove link{id={}} from chat{id={}}", link.id(), chat.id());
        return new LinkResponse(link.id(), URI.create(link.url()));
    }

    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        Chat chat = chatServiceImp.findByChatId(chatId);
        List<LinkResponse> trackedLinks = linkRepository.findAllByChat(chat).stream()
                .map(link -> new LinkResponse(link.id(), URI.create(link.url())))
                .toList();
        return new ListLinksResponse(trackedLinks, trackedLinks.size());
    }

    private Link processLinkForAdding(Link parsedLink, Chat chat) {
        chat.findLinkByUrl(parsedLink.url()).ifPresent(it -> {
            throw ApiExceptionType.LINK_ALREADY_EXISTS.toException(it.url());
        });
        return linkRepository.findByUrl(parsedLink.url()).orElseGet(() -> linkRepository.save(parsedLink));
    }

    private Link processLinkForDeletion(Link parsedLink, Chat chat) {
        return chat.findLinkByUrl(parsedLink.url())
                .orElseThrow(() -> ApiExceptionType.LINK_NOT_FOUND.toException(parsedLink.url()));
    }
}
