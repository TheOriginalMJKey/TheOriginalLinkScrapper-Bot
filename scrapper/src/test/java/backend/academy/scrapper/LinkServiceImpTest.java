// package backend.academy.scrapper;
//
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.*;
//
// import backend.academy.scrapper.configuration.ScrapperConfig;
// import backend.academy.scrapper.dto.response.LinkResponse;
// import backend.academy.scrapper.entity.Chat;
// import backend.academy.scrapper.entity.Link;
// import backend.academy.scrapper.enums.LinkType;
// import backend.academy.scrapper.repository.LinkRepository;
// import backend.academy.scrapper.service.storage.ChatServiceImp;
// import backend.academy.scrapper.service.storage.LinkServiceImp;
// import backend.academy.scrapper.util.LinkSourceUtil;
// import java.net.URI;
// import java.time.Duration;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// @ExtendWith(MockitoExtension.class)
// class LinkServiceImpTest {
//
//    @Mock
//    private LinkRepository linkRepository;
//
//    @Mock
//    private ChatServiceImp chatServiceImp;
//
//    @InjectMocks
//    private LinkServiceImp linkServiceImp;
//
//    @BeforeEach
//    void setUp() {
//        ScrapperConfig config = new ScrapperConfig(
//            30,
//            10,
//            new ScrapperConfig.LinkUpdaterScheduler(true, Duration.ofMinutes(5), Duration.ofMinutes(10)),
//            new ScrapperConfig.GithubClient(
//                "https://api.github.com",
//                "test-token",
//                new ScrapperConfig.RetryConfig(
//                    ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null, List.of(500))
//            ),
//            new ScrapperConfig.StackoverflowClient(
//                "https://api.stackexchange.com/2.3",
//                new ScrapperConfig.RetryConfig(
//                    ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null, List.of(500))
//            ),
//            new ScrapperConfig.BotClient(
//                "http://localhost:8080",
//                new ScrapperConfig.RetryConfig(
//                    ScrapperConfig.RetryStrategy.FIXED, 3, Duration.ofSeconds(1), null, List.of(500))
//            ),
//            Map.of(
//                LinkType.GITHUB,
//                new ScrapperConfig.LinkSource(
//                    "github.com",
//                    Map.of("repo", new ScrapperConfig.LinkSourceHandler(".*", "handler")))
//            ));
//        new LinkSourceUtil(config);
//    }
//
//    @Test
//    void addLinkToChat_ValidRequest_SavesLink() {
//        URI url = URI.create("https://github.com/owner/repo");
//        Link link = new Link();
//        link.url(url.toString());
//        Chat chat = new Chat();
//        chat.setChatId(1L);
//
//        when(chatServiceImp.findByChatId(1L)).thenReturn(chat);
//        when(linkRepository.findByUrl(url.toString())).thenReturn(Optional.empty());
//        when(linkRepository.save(any(Link.class))).thenReturn(link);
//
//        LinkResponse response = linkServiceImp.addLinkToChat(1L, url);
//
//        assertNotNull(response);
//        assertEquals(url, response.url());
//        verify(linkRepository).save(any(Link.class));
//    }
//
//    @Test
//    void addLinkToChat_DuplicateLink_ThrowsException() {
//        URI url = URI.create("https://github.com/owner/repo");
//        Link link = new Link();
//        link.url(url.toString());
//        Chat chat = new Chat();
//        chat.setChatId(1L);
//        chat.addLink(link);
//
//        when(chatServiceImp.findByChatId(1L)).thenReturn(chat);
//
//        assertThrows(RuntimeException.class, () -> linkServiceImp.addLinkToChat(1L, url));
//    }
//
//    @Test
//    void removeLinkFromChat_ValidRequest_RemovesLinkFromChat() {
//        URI url = URI.create("https://github.com/owner/repo");
//        Link link = new Link();
//        link.url(url.toString());
//        Chat chat = new Chat();
//        chat.setChatId(1L);
//        chat.addLink(link);
//
//        when(chatServiceImp.findByChatId(1L)).thenReturn(chat);
//
//        LinkResponse response = linkServiceImp.removeLinkFromChat(1L, url);
//
//        assertNotNull(response);
//        assertEquals(url, response.url());
//        assertFalse(chat.findLinkByUrl(url.toString()).isPresent());
//    }
// }
