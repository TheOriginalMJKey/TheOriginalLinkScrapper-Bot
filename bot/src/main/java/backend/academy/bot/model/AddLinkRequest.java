package backend.academy.bot.model;

import java.util.List;

public class AddLinkRequest {
    private String link;
    private List<String> tags;
    private List<String> filters;

    // Конструктор с параметрами
    public AddLinkRequest(String link, List<String> tags, List<String> filters) {
        this.link = link;
        this.tags = tags;
        this.filters = filters;
    }

    // Геттеры и сеттеры
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public List<String> getFilters() { return filters; }
    public void setFilters(List<String> filters) { this.filters = filters; }
}
