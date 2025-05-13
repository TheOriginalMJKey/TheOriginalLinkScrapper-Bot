package backend.academy.scrapper.service.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.client.StackoverflowClient;
import backend.academy.scrapper.dto.stackoverflow.QuestionAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.QuestionDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.service.LinkService;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackoverflowServiceTest {

    @Mock
    private StackoverflowClient stackoverflowClient;

    @Mock
    private LinkService linkService;

    private StackoverflowService stackoverflowService;

    @BeforeEach
    void setUp() {
        stackoverflowService = new StackoverflowService(stackoverflowClient, linkService);
    }

    @Test
    void getQuestionResponse_WhenQuestionExists_ReturnsFormattedMessage() {
        // Arrange
        String id = "12345";
        Link link = new Link();
        link.setId(1L);
        link.setUrl("https://stackoverflow.com/questions/12345");
        link.setCheckedAt(OffsetDateTime.now().minusHours(1));

        QuestionDto.Question question = new QuestionDto.Question(
                OffsetDateTime.now(),
                OffsetDateTime.now().minusHours(2),
                "Test Question",
                "<p>This is a test question body</p>",
                new QuestionDto.Owner("TestUser", "123"),
                id);

        QuestionDto questionDto = new QuestionDto(List.of(question));

        when(stackoverflowClient.getQuestion(eq(id))).thenReturn(questionDto);

        // Act
        Optional<String> result = stackoverflowService.getQuestionResponse(id, link);

        // Assert
        assertTrue(result.isPresent());
        String message = result.get();
        assertTrue(message.contains("🔔 *Новое обновление на StackOverflow*"));
        assertTrue(message.contains("📌 *Тип:* Вопрос"));
        assertTrue(message.contains("📝 *Вопрос:* Test Question"));
        assertTrue(message.contains("👤 *Автор:* TestUser"));
        assertTrue(message.contains("⏰ *Время:*"));
        assertTrue(message.contains("📄 *Превью:*"));
        assertTrue(message.contains("This is a test question body"));
        assertTrue(message.contains("🔗 https://stackoverflow.com/q/12345"));
    }

    @Test
    void getQuestionResponse_WhenQuestionDoesNotExist_ReturnsEmpty() {
        // Arrange
        String id = "12345";
        Link link = new Link();
        link.setId(1L);
        link.setUrl("https://stackoverflow.com/questions/12345");
        link.setCheckedAt(OffsetDateTime.now().minusHours(1));

        QuestionDto questionDto = new QuestionDto(List.of());

        when(stackoverflowClient.getQuestion(eq(id))).thenReturn(questionDto);

        // Act
        Optional<String> result = stackoverflowService.getQuestionResponse(id, link);

        // Assert
        assertFalse(result.isPresent());
        verify(linkService).updateLinkStatus(eq(link), eq(LinkStatus.BROKEN));
    }

    @Test
    void getQuestionResponse_WhenQuestionNotUpdated_ReturnsEmpty() {
        // Arrange
        String id = "12345";
        Link link = new Link();
        link.setId(1L);
        link.setUrl("https://stackoverflow.com/questions/12345");
        link.setCheckedAt(OffsetDateTime.now().plusHours(1)); // Время проверки позже времени обновления

        QuestionDto.Question question = new QuestionDto.Question(
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(3),
                "Test Question",
                "<p>This is a test question body</p>",
                new QuestionDto.Owner("TestUser", "123"),
                id);

        QuestionDto questionDto = new QuestionDto(List.of(question));

        when(stackoverflowClient.getQuestion(eq(id))).thenReturn(questionDto);

        // Act
        Optional<String> result = stackoverflowService.getQuestionResponse(id, link);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getQuestionAnswersResponse_WhenAnswersExist_ReturnsFormattedMessage() {
        // Arrange
        String id = "12345";
        OffsetDateTime lastCheckedAt = OffsetDateTime.now().minusHours(1);

        QuestionAnswerDto.Answer answer1 = new QuestionAnswerDto.Answer(
                OffsetDateTime.now(),
                OffsetDateTime.now().minusMinutes(30),
                "67890",
                id,
                "<p>This is a test answer body 1</p>",
                new QuestionAnswerDto.Owner("AnswerUser1", "456"));

        QuestionAnswerDto.Answer answer2 = new QuestionAnswerDto.Answer(
                OffsetDateTime.now(),
                OffsetDateTime.now().minusMinutes(15),
                "67891",
                id,
                "<p>This is a test answer body 2</p>",
                new QuestionAnswerDto.Owner("AnswerUser2", "457"));

        QuestionAnswerDto answerDto = new QuestionAnswerDto(Arrays.asList(answer1, answer2));

        when(stackoverflowClient.getQuestionAnswers(eq(id))).thenReturn(answerDto);

        // Act
        Optional<String> result = stackoverflowService.getQuestionAnswersResponse(id, lastCheckedAt);

        // Assert
        assertTrue(result.isPresent());
        String message = result.get();
        assertTrue(message.contains("🔔 *Новое обновление на StackOverflow*"));
        assertTrue(message.contains("📌 *Тип:* Ответ"));
        assertTrue(message.contains("👤 *Автор:* AnswerUser1"));
        assertTrue(message.contains("👤 *Автор:* AnswerUser2"));
        assertTrue(message.contains("⏰ *Время:*"));
        assertTrue(message.contains("📄 *Превью:*"));
        assertTrue(message.contains("This is a test answer body 1"));
        assertTrue(message.contains("This is a test answer body 2"));
        assertTrue(message.contains("🔗 https://stackoverflow.com/a/67890"));
        assertTrue(message.contains("🔗 https://stackoverflow.com/a/67891"));
    }

    @Test
    void getQuestionAnswersResponse_WhenNoAnswers_ReturnsEmpty() {
        // Arrange
        String id = "12345";
        OffsetDateTime lastCheckedAt = OffsetDateTime.now().minusHours(1);

        QuestionAnswerDto answerDto = new QuestionAnswerDto(List.of());

        when(stackoverflowClient.getQuestionAnswers(eq(id))).thenReturn(answerDto);

        // Act
        Optional<String> result = stackoverflowService.getQuestionAnswersResponse(id, lastCheckedAt);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getQuestionAnswersResponse_WhenAnswersNotUpdated_ReturnsEmpty() {
        // Arrange
        String id = "12345";
        OffsetDateTime lastCheckedAt = OffsetDateTime.now().plusHours(1); // Время проверки позже времени обновления

        QuestionAnswerDto.Answer answer = new QuestionAnswerDto.Answer(
                OffsetDateTime.now().minusHours(2),
                OffsetDateTime.now().minusHours(3),
                "67890",
                id,
                "<p>This is a test answer body</p>",
                new QuestionAnswerDto.Owner("AnswerUser", "456"));

        QuestionAnswerDto answerDto = new QuestionAnswerDto(List.of(answer));

        when(stackoverflowClient.getQuestionAnswers(eq(id))).thenReturn(answerDto);

        // Act
        Optional<String> result = stackoverflowService.getQuestionAnswersResponse(id, lastCheckedAt);

        // Assert
        assertFalse(result.isPresent());
    }
}
