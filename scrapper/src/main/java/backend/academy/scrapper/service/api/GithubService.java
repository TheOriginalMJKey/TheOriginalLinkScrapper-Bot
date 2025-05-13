package backend.academy.scrapper.service.api;

import backend.academy.scrapper.client.GithubClient;
import backend.academy.scrapper.configuration.ScrapperConfig;
import backend.academy.scrapper.dto.github.CommitDto;
import backend.academy.scrapper.dto.github.IssueDto;
import backend.academy.scrapper.dto.github.RepositoryDto;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@RequiredArgsConstructor
@Service
@Slf4j
@SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
public class GithubService {

    private final GithubClient githubClient;
    private final ScrapperConfig config;

    public Optional<String> getRepoCommitsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        OffsetDateTime adjustedTime = lastCheckedAt.plusSeconds(1).withOffsetSameInstant(ZoneOffset.UTC);
        log.debug("Fetching commits for {}/{} since {}", repository.owner(), repository.repo(), adjustedTime);

        try {
            List<CommitDto> commits = githubClient.getCommits(
                    repository.owner(),
                    repository.repo(),
                    "Bearer " + config.githubClient().githubToken());
            return getCommitsResponse(commits, lastCheckedAt);
        } catch (Exception e) {
            log.error("Error fetching commits: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getBranchCommitsResponse(
            RepositoryDto repository, String branch, OffsetDateTime lastCheckedAt) {
        OffsetDateTime adjustedTime = lastCheckedAt.plusSeconds(1).withOffsetSameInstant(ZoneOffset.UTC);
        log.debug(
                "Fetching branch commits for {}/{} branch {} since {}",
                repository.owner(),
                repository.repo(),
                branch,
                adjustedTime);

        try {
            List<CommitDto> commits = githubClient.getCommits(
                    repository.owner(),
                    repository.repo(),
                    "Bearer " + config.githubClient().githubToken());
            return getCommitsResponse(commits, lastCheckedAt);
        } catch (Exception e) {
            log.error("Error fetching branch commits: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getIssuesAndPullsResponse(RepositoryDto repository, OffsetDateTime lastCheckedAt) {
        OffsetDateTime adjustedTime = lastCheckedAt.plusSeconds(1).withOffsetSameInstant(ZoneOffset.UTC);
        log.debug("Fetching issues/pulls for {}/{} since {}", repository.owner(), repository.repo(), adjustedTime);

        try {
            List<IssueDto> issues = githubClient.getIssues(
                    repository.owner(),
                    repository.repo(),
                    adjustedTime.toString(),
                    "Bearer " + config.githubClient().githubToken());
            List<IssueDto> pulls = githubClient.getPullRequests(
                    repository.owner(),
                    repository.repo(),
                    adjustedTime.toString(),
                    "Bearer " + config.githubClient().githubToken());

            List<IssueDto> allIssues = new ArrayList<>();
            allIssues.addAll(issues);
            allIssues.addAll(pulls);

            return Optional.of(allIssues)
                    .filter(it -> !CollectionUtils.isEmpty(allIssues))
                    .map(issuesList -> getIssuesAndPullsResponseMessage(issuesList, lastCheckedAt));
        } catch (Exception e) {
            log.error("Error fetching issues/pulls: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getIssueResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        OffsetDateTime adjustedTime = lastCheckedAt.plusSeconds(1).withOffsetSameInstant(ZoneOffset.UTC);
        log.debug("Fetching issue {}/{} #{} since {}", repository.owner(), repository.repo(), num, adjustedTime);

        try {
            // Get the raw JSON response
            String response = githubClient.getIssueRaw(
                    repository.owner(),
                    repository.repo(),
                    num,
                    "Bearer " + config.githubClient().githubToken());

            // Check if the issue was updated after lastCheckedAt
            if (response.contains("\"updated_at\":")) {
                int dateIndex = response.indexOf("\"updated_at\":\"") + 13;
                int endIndex = response.indexOf("\"", dateIndex);
                if (dateIndex > 12 && endIndex > dateIndex) {
                    String dateStr = response.substring(dateIndex, endIndex);
                    try {
                        OffsetDateTime updatedAt = OffsetDateTime.parse(dateStr);
                        if (updatedAt.isAfter(adjustedTime)) {
                            return Optional.of(response);
                        }
                    } catch (Exception e) {
                        log.error("Error parsing date: {}", e.getMessage());
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching issue: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<String> getPullRequestResponse(RepositoryDto repository, String num, OffsetDateTime lastCheckedAt) {
        OffsetDateTime adjustedTime = lastCheckedAt.plusSeconds(1).withOffsetSameInstant(ZoneOffset.UTC);
        log.debug("Fetching PR {}/{} #{} since {}", repository.owner(), repository.repo(), num, adjustedTime);

        try {
            // Get the raw JSON response
            String response = githubClient.getPullRequestRaw(
                    repository.owner(),
                    repository.repo(),
                    num,
                    "Bearer " + config.githubClient().githubToken());

            if (response.contains("\"updated_at\":")) {
                int dateIndex = response.indexOf("\"updated_at\":\"") + 13;
                int endIndex = response.indexOf("\"", dateIndex);
                if (dateIndex > 12 && endIndex > dateIndex) {
                    String dateStr = response.substring(dateIndex, endIndex);
                    try {
                        OffsetDateTime updatedAt = OffsetDateTime.parse(dateStr);
                        if (updatedAt.isAfter(adjustedTime)) {
                            return Optional.of(response);
                        }
                    } catch (Exception e) {
                        log.error("Error parsing date: {}", e.getMessage());
                    }
                }
            }

            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching PR: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<String> getCommitsResponse(List<CommitDto> commits, OffsetDateTime lastCheckedAt) {
        if (CollectionUtils.isEmpty(commits)) {
            return Optional.empty();
        }
        List<CommitDto> recentCommits = commits.stream()
                .filter(commit -> commit.getCommittedAt() != null
                        && commit.getCommittedAt().isAfter(lastCheckedAt))
                .collect(Collectors.toList());
        if (recentCommits.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of("✔ New commits:\n"
                + recentCommits.stream().map(CommitDto::getResponseBulletPoint).collect(Collectors.joining("\n")));
    }

    private String getIssuesAndPullsResponseMessage(List<IssueDto> issues, OffsetDateTime lastCheckedAt) {
        List<IssueDto> recentIssues = issues.stream()
                .filter(issue -> {
                    if (issue.updatedAt() == null) return false;
                    // Для pull requests проверяем также mergedAt
                    if (issue.isPullRequest() && issue.pullRequest().mergedAt() != null) {
                        return issue.pullRequest().mergedAt().isAfter(lastCheckedAt);
                    }
                    return issue.updatedAt().isAfter(lastCheckedAt);
                })
                .collect(Collectors.toList());
        if (recentIssues.isEmpty()) {
            return null;
        }

        List<IssueDto> pullRequests =
                recentIssues.stream().filter(IssueDto::isPullRequest).collect(Collectors.toList());

        List<IssueDto> regularIssues =
                recentIssues.stream().filter(issue -> !issue.isPullRequest()).collect(Collectors.toList());

        StringBuilder result = new StringBuilder();

        if (!pullRequests.isEmpty()) {
            result.append("✔ New pull requests:\n");
            for (IssueDto pr : pullRequests) {
                result.append(String.format("➜ %s [%s]\n", pr.title(), pr.htmlUrl()));

                try {
                    String prDetails = githubClient.getPullRequestRaw(
                            pr.htmlUrl().split("/")[3], // owner
                            pr.htmlUrl().split("/")[4], // repo
                            pr.number().toString(),
                            "Bearer " + config.githubClient().githubToken());

                    // Extract description from the response
                    String description = extractDescriptionFromJson(prDetails);
                    if (description != null && !description.isEmpty()) {
                        result.append("Description: ").append(description).append("\n\n");
                    }
                } catch (Exception e) {
                    log.error("Error fetching PR details: {}", e.getMessage());
                }
            }
        }

        if (!regularIssues.isEmpty()) {
            if (result.length() > 0) {
                result.append("\n");
            }
            result.append("✔ New issues:\n");
            for (IssueDto issue : regularIssues) {
                result.append(String.format("➜ %s [%s]\n", issue.title(), issue.htmlUrl()));

                try {
                    String issueDetails = githubClient.getIssueRaw(
                            issue.htmlUrl().split("/")[3], // owner
                            issue.htmlUrl().split("/")[4], // repo
                            issue.number().toString(),
                            "Bearer " + config.githubClient().githubToken());

                    String description = extractDescriptionFromJson(issueDetails);
                    if (description != null && !description.isEmpty()) {
                        result.append("Description: ").append(description).append("\n\n");
                    }
                } catch (Exception e) {
                    log.error("Error fetching issue details: {}", e.getMessage());
                }
            }
        }

        return result.toString();
    }

    private String extractDescriptionFromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        int bodyIndex = json.indexOf("\"body\":");
        if (bodyIndex == -1) {
            return null;
        }

        int startIndex = json.indexOf("\"", bodyIndex + 7);
        if (startIndex == -1) {
            return null;
        }
        startIndex++;

        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }

        String description = json.substring(startIndex, endIndex);

        description = description
                .replace("\\n", "\n")
                .replace("\\r", "")
                .replace("\\\"", "\"")
                .replace("\\t", "\t");

        return description;
    }
}
