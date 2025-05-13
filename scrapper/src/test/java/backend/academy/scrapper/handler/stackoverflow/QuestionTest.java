package backend.academy.scrapper.handler.stackoverflow;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.scrapper.service.api.StackoverflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QuestionTest {

    @Mock
    private StackoverflowService stackoverflowService;

    private Question questionHandler;

    @BeforeEach
    void setUp() {
        questionHandler = new Question(stackoverflowService);
        ReflectionTestUtils.setField(questionHandler, "regex", "/(?:questions|q)/(?<id>[\\d]+)[/\\w-\\d]*");
    }
}
