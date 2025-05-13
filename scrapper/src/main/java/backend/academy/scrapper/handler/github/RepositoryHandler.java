package backend.academy.scrapper.handler.github;

import backend.academy.scrapper.dto.github.RepositoryDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.handler.LinkUpdate;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.api.GithubService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RepositoryHandler implements LinkUpdateHandler {

    private static final Logger log = LoggerFactory.getLogger(RepositoryHandler.class);

    @Value("${app.link-sources.github.handlers.repository.regex}")
    private String regex;

    private final GithubService githubService;

    @Override
    public String regex() {
        return regex;
    }

    @Override
    public Optional<LinkUpdate> getLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        RepositoryDto repository = new RepositoryDto(matcher.group("owner"), matcher.group("repo"));

        Optional<String> commits = githubService.getRepoCommitsResponse(repository, link.checkedAt());
        Optional<String> issuesAndPulls = githubService.getIssuesAndPullsResponse(repository, link.checkedAt());

        if (commits.isPresent()
                && commits.orElseThrow(() -> new IllegalStateException("Commits response is empty")) != null
                && !commits.orElseThrow(() -> new IllegalStateException("Commits response is empty"))
                        .isEmpty()) {
            String commitId = repository.owner() + "/" + repository.repo() + "/commits";
            if (!commitId.equals(link.getLastUpdateId())) {
                return Optional.of(new LinkUpdate(
                        commitId,
                        "COMMIT",
                        "New commits in repository",
                        repository.owner(),
                        OffsetDateTime.now(),
                        truncateDescription(
                                commits.orElseThrow(() -> new IllegalStateException("Commits response is empty")))));
            }
        }

        if (issuesAndPulls.isPresent()
                && issuesAndPulls.orElseThrow(() -> new IllegalStateException("Issues and pulls response is empty"))
                        != null
                && !issuesAndPulls
                        .orElseThrow(() -> new IllegalStateException("Issues and pulls response is empty"))
                        .isEmpty()) {
            String response =
                    issuesAndPulls.orElseThrow(() -> new IllegalStateException("Issues and pulls response is empty"));
            log.debug("Received response: {}", response);

            if (response.contains("✔ New pull requests:")) {
                log.debug("Found pull requests section");
                String prSection = extractPullRequestsSection(response);
                String prTitle = extractTitleFromResponse(prSection);

                if (prTitle != null && !prTitle.isEmpty() && !prTitle.equals("New update in repository")) {
                    String prUrl = extractUrlFromResponse(prSection);
                    String prId = prUrl != null ? prUrl : repository.owner() + "/" + repository.repo() + "/pull";

                    if (!prId.equals(link.getLastUpdateId())) {
                        String prDescription = extractDescriptionFromResponse(prSection);
                        return Optional.of(new LinkUpdate(
                                prId,
                                "PULL_REQUEST",
                                prTitle,
                                repository.owner(),
                                OffsetDateTime.now(),
                                truncateDescription(prDescription)));
                    }
                }
            }

            if (response.contains("✔ New issues:")) {
                log.debug("Found issues section");
                String issuesSection = extractIssuesSection(response);
                String issueTitle = extractTitleFromResponse(issuesSection);

                // Only create an update if we have a valid issue title
                if (issueTitle != null && !issueTitle.isEmpty() && !issueTitle.equals("New update in repository")) {
                    String issueUrl = extractUrlFromResponse(issuesSection);
                    String issueId =
                            issueUrl != null ? issueUrl : repository.owner() + "/" + repository.repo() + "/issue";

                    // Skip if this is the same issue update we've already sent
                    if (!issueId.equals(link.getLastUpdateId())) {
                        String issueDescription = extractDescriptionFromResponse(issuesSection);
                        return Optional.of(new LinkUpdate(
                                issueId,
                                "ISSUE",
                                issueTitle,
                                repository.owner(),
                                OffsetDateTime.now(),
                                truncateDescription(issueDescription)));
                    }
                }
            }
        }

        return Optional.empty();
    }

    private String extractPullRequestsSection(String response) {
        int startIndex = response.indexOf("✔ New pull requests:");
        if (startIndex == -1) {
            return "";
        }

        int endIndex = response.indexOf("✔ New issues:", startIndex);
        if (endIndex == -1) {
            return response.substring(startIndex);
        }

        return response.substring(startIndex, endIndex).trim();
    }

    private String extractIssuesSection(String response) {
        int startIndex = response.indexOf("✔ New issues:");
        if (startIndex == -1) {
            return "";
        }

        return response.substring(startIndex).trim();
    }

    private String extractTitleFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("➜\\s*([^\\[]+)\\s*\\[");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String title = matcher.group(1).trim();
            if (!title.isEmpty()) {
                return title;
            }
        }

        pattern = Pattern.compile("New\\s+(pull\\s+request|issue):\\s+([^\\n]+)");
        matcher = pattern.matcher(response);
        if (matcher.find()) {
            String title = matcher.group(2).trim();
            if (!title.isEmpty()) {
                return title;
            }
        }

        return null;
    }

    private String extractDescriptionFromResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "No description available";
        }

        Pattern pattern = Pattern.compile("Description:\\s+(.*?)(?=\\n\\n|$)", Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            String description = matcher.group(1).trim();
            if (!description.isEmpty()) {
                return description;
            }
        }

        if (response.contains("✔ New pull requests:") || response.contains("✔ New issues:")) {
            StringBuilder sb = new StringBuilder();

            if (response.contains("✔ New pull requests:")) {
                sb.append(extractSectionContent("✔ New pull requests:", response));
            }

            if (response.contains("✔ New issues:")) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(extractSectionContent("✔ New issues:", response));
            }

            if (sb.length() > 0) {
                return sb.toString();
            }
        }

        return "A new update has been made to the repository.";
    }

    private String extractSectionContent(String header, String response) {
        int startIndex = response.indexOf(header);
        if (startIndex == -1) {
            return "";
        }

        // Find the end of the section (next header or end of string)
        int endIndex = response.indexOf("✔", startIndex + header.length());
        if (endIndex == -1) {
            return response.substring(startIndex).trim();
        }

        return response.substring(startIndex, endIndex).trim();
    }

    private String truncateDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.length() > 200 ? description.substring(0, 200) + "..." : description;
    }

    private String extractUrlFromResponse(String response) {
        Pattern pattern = Pattern.compile("\\[(https?://[^\\]]+)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
