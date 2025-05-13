package backend.academy.scrapper.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record IssueDto(
        @JsonProperty("html_url") String htmlUrl,
        String title,
        @JsonProperty("updated_at") OffsetDateTime updatedAt,
        @JsonProperty("created_at") OffsetDateTime createdAt,
        @JsonProperty("number") Long number,
        String state,
        @JsonProperty("pull_request") PullRequestInfo pullRequest) {

    public String getResponseBulletPoint() {
        String type = isPullRequest() ? "PR" : "Issue";
        return String.format("➜ %s #%d was %s: %s [%s]", type, number, state, title, htmlUrl);
    }

    public boolean isPullRequest() {
        return pullRequest != null;
    }

    public record PullRequestInfo(
            @JsonProperty("html_url") String htmlUrl, @JsonProperty("merged_at") OffsetDateTime mergedAt) {}
}
