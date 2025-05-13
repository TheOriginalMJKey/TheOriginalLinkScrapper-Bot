package backend.academy.scrapper.service.api;

import backend.academy.scrapper.client.StackoverflowClient;
import backend.academy.scrapper.dto.stackoverflow.QuestionAnswerDto;
import backend.academy.scrapper.dto.stackoverflow.QuestionDto;
import backend.academy.scrapper.entity.Link;
import backend.academy.scrapper.enums.LinkStatus;
import backend.academy.scrapper.service.LinkService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
public class StackoverflowService {

    private static final String QUESTION_URL = "https://stackoverflow.com/q/%s";
    private static final String ANSWER_URL = "https://stackoverflow.com/a/%s";
    private final StackoverflowClient stackoverflowClient;
    private final LinkService linkService;

    public Optional<String> getQuestionResponse(String id, Link link) {
        return Optional.of(stackoverflowClient.getQuestion(id))
                .map(QuestionDto::questions)
                .flatMap(questions -> {
                    if (CollectionUtils.isEmpty(questions)) {
                        linkService.updateLinkStatus(link, LinkStatus.BROKEN);
                        return Optional.empty();
                    } else {
                        return Optional.of(questions.getFirst());
                    }
                })
                .filter(question -> question.updatedAt().isAfter(link.checkedAt()))
                .map(this::getQuestionResponseMessage);
    }

    public Optional<String> getQuestionAnswersResponse(String id, OffsetDateTime lastCheckedAt) {
        return Optional.of(stackoverflowClient.getQuestionAnswers(id))
                .map(QuestionAnswerDto::answers)
                .filter(answers -> !CollectionUtils.isEmpty(answers))
                .map(answers -> answers.stream()
                        .filter(ans -> ans.updatedAt().isAfter(lastCheckedAt))
                        .collect(Collectors.toList()))
                .map(this::getQuestionAnswersResponseMessage);
    }

    private String getQuestionResponseMessage(QuestionDto.Question question) {
        String username = question.owner() != null ? question.owner().displayName() : "Unknown";
        String time = question.createdAt() != null
                ? question.createdAt().toString().replace("T", " ").substring(0, 19)
                : "Unknown time";

        StringBuilder sb = new StringBuilder();
        sb.append("🔔 *Новое обновление на StackOverflow*\n\n");
        sb.append("📌 *Тип:* Вопрос\n");
        sb.append("📝 *Вопрос:* ").append(question.title()).append("\n");
        sb.append("👤 *Автор:* ").append(username).append("\n");
        sb.append("⏰ *Время:* ").append(time).append("\n");

        if (StringUtils.hasText(question.body())) {
            String preview = truncateText(question.body(), 200);
            sb.append("\n📄 *Превью:*\n").append(preview);
        }

        sb.append("\n\n🔗 ").append(String.format(QUESTION_URL, question.id()));

        return sb.toString();
    }

    private String getQuestionAnswersResponseMessage(List<QuestionAnswerDto.Answer> answers) {
        if (answers.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (QuestionAnswerDto.Answer answer : answers) {
            String username = answer.owner() != null ? answer.owner().displayName() : "Unknown";
            String time = answer.createdAt() != null
                    ? answer.createdAt().toString().replace("T", " ").substring(0, 19)
                    : "Unknown time";

            sb.append("🔔 *Новое обновление на StackOverflow*\n\n");
            sb.append("📌 *Тип:* Ответ\n");
            sb.append("👤 *Автор:* ").append(username).append("\n");
            sb.append("⏰ *Время:* ").append(time).append("\n");

            if (StringUtils.hasText(answer.body())) {
                String preview = truncateText(answer.body(), 200);
                sb.append("\n📄 *Превью:*\n").append(preview);
            }

            sb.append("\n\n🔗 ").append(String.format(ANSWER_URL, answer.id()));
            sb.append("\n\n---\n\n");
        }

        return sb.toString();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        // Remove HTML tags
        String plainText = text.replaceAll("<[^>]*>", "");

        if (plainText.length() <= maxLength) {
            return plainText;
        }

        return plainText.substring(0, maxLength) + "...";
    }
}
