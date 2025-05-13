package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.github.CommitDto;
import backend.academy.scrapper.dto.github.IssueDto;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

public interface GithubClient {
    @GetExchange("/repos/{owner}/{repo}/commits")
    List<CommitDto> getCommits(
            @PathVariable String owner, @PathVariable String repo, @RequestHeader("Authorization") String auth);

    @GetExchange("/repos/{owner}/{repo}/commits/{sha}")
    CommitDto getCommit(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String sha,
            @RequestHeader("Authorization") String auth);

    @GetExchange("/repos/{owner}/{repo}/issues")
    List<IssueDto> getIssues(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam("since") String since,
            @RequestHeader("Authorization") String auth);

    @GetExchange("/repos/{owner}/{repo}/pulls")
    List<IssueDto> getPullRequests(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam("since") String since,
            @RequestHeader("Authorization") String auth);

    @GetExchange("/repos/{owner}/{repo}/issues/{num}")
    IssueDto getIssue(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String num,
            @RequestHeader("Authorization") String auth);

    @GetExchange("/repos/{owner}/{repo}/pulls/{num}")
    IssueDto getPullRequest(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String num,
            @RequestHeader("Authorization") String auth);

    @GetExchange(value = "/repos/{owner}/{repo}/issues/{num}", accept = MediaType.APPLICATION_JSON_VALUE)
    String getIssueRaw(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String num,
            @RequestHeader("Authorization") String auth);

    @GetExchange(value = "/repos/{owner}/{repo}/pulls/{num}", accept = MediaType.APPLICATION_JSON_VALUE)
    String getPullRequestRaw(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String num,
            @RequestHeader("Authorization") String auth);
}
