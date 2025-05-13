package backend.academy.scrapper;

import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.service.LinkUpdateManager;
import backend.academy.scrapper.service.LinkUpdaterService;
import backend.academy.scrapper.service.sender.UpdateSender;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUpdaterServiceTest {

    @Mock
    private LinkService linkService;

    @Mock
    private UpdateSender updateSender;

    @Mock
    private LinkUpdateHandler linkUpdateHandler;

    @Mock
    private LinkUpdateManager linkUpdateManager;

    private LinkUpdaterService linkUpdaterService;

    @BeforeEach
    void setUp() {
        linkUpdaterService =
                new LinkUpdaterService(linkService, updateSender, List.of(linkUpdateHandler), linkUpdateManager);
    }
}
