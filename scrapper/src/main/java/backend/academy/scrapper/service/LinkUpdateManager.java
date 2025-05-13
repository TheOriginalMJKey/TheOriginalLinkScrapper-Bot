package backend.academy.scrapper.service;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.entity.LinkUpdate;
import backend.academy.scrapper.repository.JdbcLinkUpdateRepository;
import backend.academy.scrapper.repository.JpaLinkUpdateRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LinkUpdateManager {

    private final JpaLinkUpdateRepository jpaRepository;
    private final JdbcLinkUpdateRepository jdbcRepository;

    @Value("${app.access-type:SQL}")
    private String accessType;

    @Value("${app.description-preview-length:200}")
    private int previewLength;

    public LinkUpdateManager(
            Optional<JpaLinkUpdateRepository> jpaRepository, Optional<JdbcLinkUpdateRepository> jdbcRepository) {
        this.jpaRepository = jpaRepository.orElse(null);
        this.jdbcRepository = jdbcRepository.orElse(null);
    }

    @Transactional
    public Optional<LinkUpdate> createUpdate(
            Link link,
            String updateId,
            String updateType,
            String title,
            String username,
            OffsetDateTime createdAt,
            String description) {

        // Check if update already exists
        Optional<LinkUpdate> existingUpdate = findByLinkAndUpdateId(link, updateId);
        if (existingUpdate.isPresent()) {
            return existingUpdate;
        }

        // Generate preview text
        String preview = description != null && description.length() > previewLength
                ? description.substring(0, previewLength) + "..."
                : description;

        // Create new update
        LinkUpdate update = LinkUpdate.builder()
                .link(link)
                .updateId(updateId)
                .updateType(updateType)
                .title(title)
                .username(username)
                .createdAt(createdAt)
                .descriptionPreview(preview)
                .processed(false)
                .build();

        // Save update
        saveUpdate(update);
        return Optional.of(update);
    }

    @Transactional
    public void markAsProcessed(LinkUpdate update) {
        update.setProcessed(true);
        saveUpdate(update);
    }

    private Optional<LinkUpdate> findByLinkAndUpdateId(Link link, String updateId) {
        if ("ORM".equals(accessType)) {
            if (jpaRepository == null) {
                throw new IllegalStateException("JPA repository is not available");
            }
            return jpaRepository.findByLinkAndUpdateId(link, updateId);
        } else {
            if (jdbcRepository == null) {
                throw new IllegalStateException("JDBC repository is not available");
            }
            return jdbcRepository.findByLinkAndUpdateId(link, updateId);
        }
    }

    private void saveUpdate(LinkUpdate update) {
        if ("ORM".equals(accessType)) {
            if (jpaRepository == null) {
                throw new IllegalStateException("JPA repository is not available");
            }
            jpaRepository.save(update);
        } else {
            if (jdbcRepository == null) {
                throw new IllegalStateException("JDBC repository is not available");
            }
            jdbcRepository.save(update);
        }
    }

    /** Formats a link update notification message with raw data. */
    public String formatUpdateMessage(LinkUpdate update) {
        Link link = update.getLink();

        // Format the message based on the update type
        return String.format(
                "%s||%s||%s||%s||%s||%s",
                update.getUpdateType(),
                escapeMarkdown(update.getTitle()),
                escapeMarkdown(update.getUsername()),
                formatDateTime(update.getCreatedAt()),
                escapeMarkdown(update.getDescriptionPreview()),
                link.getUrl());
    }

    /** Formats a date-time value for raw data */
    private String formatDateTime(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.toString().replace("T", " ").substring(0, 19);
    }

    /** Escapes special Markdown characters in the text */
    private String escapeMarkdown(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("~", "\\~")
                .replace("`", "\\`")
                .replace(">", "\\>")
                .replace("#", "\\#")
                .replace("+", "\\+")
                .replace("-", "\\-")
                .replace("=", "\\=")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace(".", "\\.")
                .replace("!", "\\!");
    }
}
