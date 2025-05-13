package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Link;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaLinkRepository extends JpaRepository<Link, Long> {
    Optional<Link> findByUrl(String url);

    @Query("SELECT l FROM Link l WHERE l.checkedAt < :time")
    List<Link> findLinksToUpdate(@Param("time") OffsetDateTime time, Pageable pageable);
}
