package backend.academy.bot.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public class LinkUpdate {
    private Long id;

    @NotNull
    private String url;

    @NotNull
    private String description;

    private String type;

    private String title;

    private String author;

    private OffsetDateTime createdAt;

    @NotEmpty
    private List<@NotNull Long> tgChatIds; // Запрещаем null в списке

    public LinkUpdate() {}

    public LinkUpdate(Long id, String url, String description, List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.tgChatIds = tgChatIds;
    }

    public LinkUpdate(
            Long id,
            String url,
            String description,
            String type,
            String title,
            String author,
            OffsetDateTime createdAt,
            List<Long> tgChatIds) {
        this.id = id;
        this.url = url;
        this.description = description;
        this.type = type;
        this.title = title;
        this.author = author;
        this.createdAt = createdAt;
        this.tgChatIds = tgChatIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Long> getTgChatIds() {
        return tgChatIds;
    }

    public void setTgChatIds(List<Long> tgChatIds) {
        this.tgChatIds = tgChatIds;
    }

    public boolean isEmpty() {
        return id == null || url == null || description == null || tgChatIds == null;
    }
}
