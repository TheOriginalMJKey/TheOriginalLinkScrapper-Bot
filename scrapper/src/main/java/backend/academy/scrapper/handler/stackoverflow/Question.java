package backend.academy.scrapper.handler.stackoverflow;

import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.handler.LinkUpdate;
import backend.academy.scrapper.handler.LinkUpdateHandler;
import backend.academy.scrapper.service.api.StackoverflowService;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.regex.MatchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class Question implements LinkUpdateHandler {

    @Value("${app.link-sources.stackoverflow.handlers.question.regex}")
    private String regex;

    private final StackoverflowService stackoverflowService;

    @Override
    public String regex() {
        return regex;
    }

    @Override
    public Optional<LinkUpdate> getLinkUpdate(Link link) {
        MatchResult matcher = linkMatcher(link);
        String id = matcher.group("id");

        // Получаем информацию о вопросе
        Optional<String> questionResponse = stackoverflowService.getQuestionResponse(id, link);

        // Получаем информацию об ответах
        Optional<String> answersResponse = Optional.empty();
        if (questionResponse.isPresent()) {
            answersResponse = stackoverflowService.getQuestionAnswersResponse(id, link.checkedAt());
        }

        // Если нет ни вопросов, ни ответов, возвращаем пустой результат
        if (questionResponse.isEmpty() && answersResponse.isEmpty()) {
            return Optional.empty();
        }

        // Определяем тип обновления и создаем соответствующий LinkUpdate
        if (questionResponse.isPresent()) {
            // Если обновился вопрос
            return Optional.of(new LinkUpdate(
                    id,
                    "STACKOVERFLOW_QUESTION",
                    "Вопрос #" + id,
                    "stackoverflow_user", // Имя пользователя будет извлечено из ответа
                    OffsetDateTime.now(),
                    questionResponse.orElseThrow(() -> new IllegalStateException("Question response is empty"))));
        } else if (answersResponse.isPresent()) {
            // Если появились новые ответы
            return Optional.of(new LinkUpdate(
                    id,
                    "ANSWER",
                    "Ответы на вопрос #" + id,
                    "stackoverflow_user", // Имя пользователя будет извлечено из ответа
                    OffsetDateTime.now(),
                    answersResponse.orElseThrow(() -> new IllegalStateException("Answers response is empty"))));
        }

        return Optional.empty();
    }
}
