package backend.academy.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkUpdate {
    @JsonProperty("tgChatId")
    private Long tgChatId;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("updateType")
    private UpdateType updateType;
    
    public enum UpdateType {
        ADD,
        REMOVE,
        UPDATE
    }
} 