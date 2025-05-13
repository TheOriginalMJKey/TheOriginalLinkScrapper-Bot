package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface LinkRepository {
    void add(Link link);

    List<Link> findAll();

    Optional<Link> findById(Long id);

    Optional<Link> findByUrl(String url);

    void updateStatus(Long id, LinkStatus status);

    void updateCheckedAt(Long id, OffsetDateTime checkedAt);

    void updateLastUpdateId(Long id, String lastUpdateId);

    List<Link> findLinksToUpdate(OffsetDateTime before, int limit);

    // Methods to satisfy existing code
    default Link save(Link link) {
        if (link.getId() == null) {
            add(link);
        }
        return link;
    }

    default boolean updateStatus(Link link, LinkStatus status) {
        updateStatus(link.getId(), status);
        return true;
    }

    default boolean updateCheckedAt(Link link, OffsetDateTime checkedAt) {
        updateCheckedAt(link.getId(), checkedAt);
        return true;
    }

    default List<Link> findAllByChat(Chat chat) {
        return chat.getLinks().stream().toList();
    }

    default List<Link> findAllWithStatusAndOlderThan(LinkStatus status, OffsetDateTime checkedAt, Pageable pageable) {
        return findLinksToUpdate(checkedAt, pageable.getPageSize());
    }
}
