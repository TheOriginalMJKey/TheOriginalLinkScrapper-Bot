package backend.academy.scrapper.dto.response;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

public record LinkUpdate(
        Long id,
        URI url,
        String description,
        String type,
        String title,
        String author,
        OffsetDateTime createdAt,
        List<Long> tgChatIds) {}
