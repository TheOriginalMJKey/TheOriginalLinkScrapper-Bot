// package backend.academy.scrapper;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertThrows;
//
// import backend.academy.scrapper.configuration.ScrapperConfig;
// import backend.academy.scrapper.entity.Link;
// import backend.academy.scrapper.enums.LinkType;
// import backend.academy.scrapper.util.LinkParser;
// import backend.academy.scrapper.util.LinkSourceUtil;
// import java.net.URI;
// import java.time.Duration;
// import java.util.List;
// import java.util.Map;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
//
// class LinkParserTest {
//
//    @BeforeEach
//    void setUp() {
//        ScrapperConfig config = new ScrapperConfig(
//                30,
//                10,
//                new ScrapperConfig.LinkUpdaterScheduler(true, Duration.ofMinutes(5), Duration.ofMinutes(10)),
//                new ScrapperConfig.GithubClient( // Исправленный конструктор
//                        "https://api.github.com",
//                        "test-token", // Добавлен токен
//                        new ScrapperConfig.RetryConfig(
//                                ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null, List.of(500))),
//                new ScrapperConfig.StackoverflowClient(
//                        "https://api.stackexchange.com/2.3",
//                        new ScrapperConfig.RetryConfig(
//                                ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null, List.of(500))),
//                new ScrapperConfig.BotClient(
//                        "http://localhost:8080",
//                        new ScrapperConfig.RetryConfig(
//                                ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null, List.of(500))),
//                Map.of(
//                        LinkType.GITHUB,
//                        new ScrapperConfig.LinkSource(
//                                "github.com", Map.of("repo", new ScrapperConfig.LinkSourceHandler(".*",
// "handler")))));
//        new LinkSourceUtil(config);
//    }
//
//    @Test
//    void parseLink_ValidGithubUrl_ReturnsLink() {
//        URI url = URI.create("https://github.com/owner/repo");
//        Link link = LinkParser.parseLink(url);
//
//        assertEquals(LinkType.GITHUB, link.linkType());
//        assertEquals("https://github.com/owner/repo", link.url());
//    }
//
//    @Test
//    void parseLink_InvalidUrl_ThrowsException() {
//        URI url = URI.create("invalid-url");
//        assertThrows(RuntimeException.class, () -> LinkParser.parseLink(url));
//    }
// }
