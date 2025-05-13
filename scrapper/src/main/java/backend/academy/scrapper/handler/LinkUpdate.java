package backend.academy.scrapper.handler;

import java.time.OffsetDateTime;

public record LinkUpdate(
        String id, String type, String title, String username, OffsetDateTime createdAt, String description) {}
