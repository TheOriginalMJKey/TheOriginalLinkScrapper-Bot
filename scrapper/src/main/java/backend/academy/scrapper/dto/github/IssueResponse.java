package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record IssueResponse(
        int number, String title, String body, UserInfo user, @JsonProperty("created_at") OffsetDateTime createdAt) {
    public record UserInfo(String login) {}
}
