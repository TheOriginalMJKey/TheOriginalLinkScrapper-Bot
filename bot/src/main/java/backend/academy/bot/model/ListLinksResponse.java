package backend.academy.bot.model;

import java.util.List;

public class ListLinksResponse {
    private List<LinkResponse> links;
    private Integer size;

    // Конструкторы, геттеры/сеттеры
    public List<LinkResponse> getLinks() { return links; }
    public void setLinks(List<LinkResponse> links) { this.links = links; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
}
