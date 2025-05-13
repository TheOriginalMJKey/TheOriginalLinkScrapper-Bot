// package backend.academy.scrapper;
//
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.Mockito.when;
//
// import backend.academy.scrapper.client.GithubClient;
// import backend.academy.scrapper.configuration.ScrapperConfig;
// import backend.academy.scrapper.dto.github.CommitDto;
// import backend.academy.scrapper.dto.github.IssueDto;
// import backend.academy.scrapper.dto.github.RepositoryDto;
// import backend.academy.scrapper.service.api.GithubService;
// import java.time.Duration;
// import java.time.OffsetDateTime;
// import java.util.List;
// import java.util.Optional;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// class GithubServiceTest {
//    @Mock
//    GithubClient githubClient;
//
//    @Mock
//    ScrapperConfig config;
//
//    @InjectMocks
//    GithubService service;
//
//    @Test
//    void testGetRepoCommitsResponse() {
//        RepositoryDto repo = new RepositoryDto("owner", "repo");
//        OffsetDateTime since = OffsetDateTime.now().minusDays(1);
//        CommitDto commit = new CommitDto(new CommitDto.Commit("message"), "https://url");
//
//        when(config.githubClient()).thenReturn(
//            new ScrapperConfig.GithubClient(
//                "https://api.github.com",
//                "token",
//                new ScrapperConfig.RetryConfig(ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null,
// List.of(500))
//            );
//
//        when(githubClient.getRepoCommits("owner", "repo", since, "Bearer token"))
//            .thenReturn(List.of(commit));
//
//        Optional<String> result = service.getRepoCommitsResponse(repo, since);
//        assertTrue(result.isPresent());
//        assertTrue(result.get().contains("message"));
//    }
//
//    @Test
//    void testGetIssuesAndPullsResponse() {
//        RepositoryDto repo = new RepositoryDto("owner", "repo");
//        OffsetDateTime since = OffsetDateTime.now().minusDays(1);
//        IssueDto issue = new IssueDto("https://issue-url", "title", OffsetDateTime.now());
//
//        when(config.githubClient()).thenReturn(
//            new ScrapperConfig.GithubClient(
//                "https://api.github.com",
//                "token",
//                new ScrapperConfig.RetryConfig(ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null,
// List.of(500))
//            )
//        );
//
//        when(githubClient.getIssuesAndPulls("owner", "repo", since, "Bearer token"))
//            .thenReturn(List.of(issue));
//
//        Optional<String> result = service.getIssuesAndPullsResponse(repo, since);
//        assertTrue(result.isPresent());
//        assertTrue(result.get().contains("title"));
//    }
// }
