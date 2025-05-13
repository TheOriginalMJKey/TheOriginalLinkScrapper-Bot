package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkUpdate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLinkUpdateRepository extends JpaRepository<LinkUpdate, Long> {
    List<LinkUpdate> findByLinkAndProcessedFalse(Link link, Pageable pageable);

    Optional<LinkUpdate> findByLinkAndUpdateId(Link link, String updateId);
}
