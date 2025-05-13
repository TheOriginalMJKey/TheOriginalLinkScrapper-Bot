package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.enums.LinkType;
import backend.academy.scrapper.repository.JpaChatRepository;
import backend.academy.scrapper.repository.JpaLinkRepository;
import backend.academy.scrapper.util.LinkSourceUtil;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "ORM")
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatRepository chatRepository;

    public JpaLinkService(JpaLinkRepository linkRepository, JpaChatRepository chatRepository) {
        this.linkRepository = linkRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    public List<Link> getLinksToUpdate(Integer minutes, Integer limit) {
        OffsetDateTime updateTime = OffsetDateTime.now().minusMinutes(minutes);
        return linkRepository.findLinksToUpdate(updateTime, PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public void updateLinkStatus(Link link, LinkStatus status) {
        link.setStatus(status);
        linkRepository.save(link);
    }

    @Override
    @Transactional
    public void updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        link.setCheckedAt(checkedAt);
        linkRepository.save(link);
    }

    @Override
    @Transactional
    public void updateLastUpdateId(Long linkId, String updateId) {
        linkRepository.findById(linkId).ifPresent(link -> {
            link.setLastUpdateId(updateId);
            linkRepository.save(link);
        });
    }

    @Override
    @Transactional
    public LinkResponse addLinkToChat(Long chatId, URI url) {
        Chat chat = chatRepository.findByChatId(chatId).orElseGet(() -> {
            Chat newChat = Chat.builder().chatId(chatId).build();
            return chatRepository.save(newChat);
        });

        String urlString = url.toString();
        Optional<Link> existingLink = linkRepository.findByUrl(urlString);

        if (existingLink.isPresent()) {
            Link link = existingLink.orElseThrow(() -> new IllegalStateException("Link not found: " + urlString));
            if (chat.findLinkByUrl(urlString).isEmpty()) {
                chat.addLink(link);
                chatRepository.save(chat);
            }
            return createLinkResponse(link);
        } else {
            LinkType linkType = LinkSourceUtil.getLinkType(url);
            Link newLink = Link.builder().url(urlString).linkType(linkType).build();

            newLink = linkRepository.save(newLink);
            chat.addLink(newLink);
            chatRepository.save(chat);

            return createLinkResponse(newLink);
        }
    }

    @Override
    @Transactional
    public LinkResponse removeLinkFromChat(Long chatId, URI url) {
        Chat chat = chatRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));

        String urlString = url.toString();
        Link link = chat.findLinkByUrl(urlString)
                .orElseThrow(() -> new IllegalArgumentException("Link not found: " + urlString));

        chat.removeLink(link);
        chatRepository.save(chat);

        return createLinkResponse(link);
    }

    @Override
    public ListLinksResponse getChatLinks(Long chatId) {
        Chat chat = chatRepository
                .findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));

        List<LinkResponse> links =
                chat.getLinks().stream().map(this::createLinkResponse).collect(Collectors.toList());

        return new ListLinksResponse(links, links.size());
    }

    private LinkResponse createLinkResponse(Link link) {
        return new LinkResponse(link.getId(), URI.create(link.getUrl()));
    }
}
 