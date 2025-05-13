package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<String, Link> linkStorage = new HashMap<>();
    private final Map<Long, Link> linkById = new HashMap<>();
    private final Map<Long, List<Link>> chatLinks = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @Override
    public void add(Link link) {
        if (link.getId() == null) {
            link.setId(idCounter.getAndIncrement());
        }
        linkStorage.put(link.getUrl(), link);
        linkById.put(link.getId(), link);
    }

    @Override
    public List<Link> findAll() {
        return new ArrayList<>(linkStorage.values());
    }

    @Override
    public Optional<Link> findById(Long id) {
        return Optional.ofNullable(linkById.get(id));
    }

    @Override
    public List<Link> findAllByChat(Chat chat) {
        return chatLinks.getOrDefault(chat.getChatId(), List.of());
    }

    @Override
    public Link save(Link link) {
        add(link);
        return link;
    }

    // Добавьте метод для обновления связей
    public void addLinkToChat(Long chatId, Link link) {
        List<Link> links = chatLinks.getOrDefault(chatId, new ArrayList<>());
        links.add(link);
        chatLinks.put(chatId, links);
    }

    @Override
    public Optional<Link> findByUrl(String url) {
        return Optional.ofNullable(linkStorage.get(url));
    }

    @Override
    public void updateStatus(Long id, LinkStatus status) {
        Link link = linkById.get(id);
        if (link != null) {
            link.setStatus(status);
        }
    }

    @Override
    public boolean updateStatus(Link link, LinkStatus status) {
        updateStatus(link.getId(), status);
        return true;
    }

    @Override
    public void updateCheckedAt(Long id, OffsetDateTime checkedAt) {
        Link link = linkById.get(id);
        if (link != null) {
            link.setCheckedAt(checkedAt);
        }
    }

    @Override
    public boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        updateCheckedAt(link.getId(), checkedAt);
        return true;
    }

    @Override
    public void updateLastUpdateId(Long id, String lastUpdateId) {
        Link link = linkById.get(id);
        if (link != null) {
            link.setLastUpdateId(lastUpdateId);
        }
    }

    @Override
    public List<Link> findLinksToUpdate(OffsetDateTime before, int limit) {
        List<Link> result = new ArrayList<>();
        for (Link link : linkStorage.values()) {
            if (link.getCheckedAt().isBefore(before) && link.getStatus() == LinkStatus.ACTIVE) {
                result.add(link);
                if (result.size() >= limit) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Pageable pageable) {
        return findLinksToUpdate(checkedAt, pageable.getPageSize());
    }
}
