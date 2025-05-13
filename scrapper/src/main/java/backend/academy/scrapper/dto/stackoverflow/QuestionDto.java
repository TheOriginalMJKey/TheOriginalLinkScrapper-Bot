package backend.academy.scrapper.dto.stackoverflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record QuestionDto(@JsonProperty("items") List<Question> questions) {

    public record Question(
            @JsonProperty("last_activity_date") OffsetDateTime updatedAt,
            @JsonProperty("creation_date") OffsetDateTime createdAt,
            String title,
            @JsonProperty("body") String body,
            @JsonProperty("owner") Owner owner,
            @JsonProperty("question_id") String id) {}

    public record Owner(@JsonProperty("display_name") String displayName, @JsonProperty("user_id") String userId) {}
}
