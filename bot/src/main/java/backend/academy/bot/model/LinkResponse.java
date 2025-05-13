package backend.academy.bot.model;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class LinkResponse {
    private Long id;
    private String url;
    private List<String> tags;
    private List<String> filters;

    // Геттеры и сеттеры
}
