package backend.academy.scrapper.handler.github;

import backend.academy.scrapper.dto.github.RepositoryDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.handler.LinkUpdate;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.api.GithubService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PullRequestHandler implements LinkUpdateHandler {

    @Value("${app.link-sources.github.handlers.pull-request.regex}")
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
        String num = matcher.group("num");

        return githubService
                .getPullRequestResponse(repository, num, link.checkedAt())
                .map(response -> {
                    String username = "unknown";
                    if (response.contains("user")) {
                        int userIndex = response.indexOf("\"login\":\"") + 9;
                        int endIndex = response.indexOf("\"", userIndex);
                        if (userIndex > 8 && endIndex > userIndex) {
                            username = response.substring(userIndex, endIndex);
                        }
                    }

                    return new LinkUpdate(
                            num,
                            "PULL_REQUEST",
                            extractTitle(response),
                            username,
                            extractCreatedAt(response),
                            truncateDescription(extractBody(response)));
                });
    }

    private String truncateDescription(String description) {
        if (description == null) {
            return "";
        }
        return description.length() > 200 ? description.substring(0, 200) + "..." : description;
    }

    private String extractTitle(String response) {
        // Simple extraction - in a real implementation, parse JSON properly
        int titleIndex = response.indexOf("\"title\":\"") + 9;
        int endIndex = response.indexOf("\"", titleIndex);
        if (titleIndex > 8 && endIndex > titleIndex) {
            return response.substring(titleIndex, endIndex);
        }
        return "Unknown Title";
    }

    private String extractBody(String response) {
        int bodyIndex = response.indexOf("\"body\":\"") + 8;
        if (bodyIndex > 7) {
            int endIndex = response.indexOf("\"", bodyIndex);
            if (endIndex > bodyIndex) {
                return response.substring(bodyIndex, endIndex);
            }
        }
        return "";
    }

    private OffsetDateTime extractCreatedAt(String response) {
        int dateIndex = response.indexOf("\"created_at\":\"") + 13;
        int endIndex = response.indexOf("\"", dateIndex);
        if (dateIndex > 12 && endIndex > dateIndex) {
            String dateStr = response.substring(dateIndex, endIndex);
            try {
                return OffsetDateTime.parse(dateStr);
            } catch (Exception e) {
                return OffsetDateTime.now();
            }
        }
        return OffsetDateTime.now();
    }
}
