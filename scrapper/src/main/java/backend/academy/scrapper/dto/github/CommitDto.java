package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record CommitDto(Commit commit, @JsonProperty("html_url") String htmlUrl) {

    public String getResponseBulletPoint() {
        return String.format("➜ %s [%s]", commit.message, htmlUrl);
    }

    public OffsetDateTime getCommittedAt() {
        return commit.committer.committedAt;
    }

    public record Commit(String message, @JsonProperty("committer") Committer committer) {}

    public record Committer(@JsonProperty("date") OffsetDateTime committedAt) {}
}
