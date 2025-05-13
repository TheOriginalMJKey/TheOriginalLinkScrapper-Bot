package backend.academy.bot.service;

import static org.junit.jupiter.api.Assertions.*;

import backend.academy.bot.dto.LinkUpdate;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageFormatterTest {

    private MessageFormatter messageFormatter;

    @BeforeEach
    void setUp() {
        messageFormatter = new MessageFormatter();
    }

    @Test
    void formatUpdateMessage_WhenStackOverflowQuestion_ReturnsFormattedMessage() {
        // Arrange
        String formattedMessage = "🔔 *Новое обновление на StackOverflow*\n" + "📌 *Тип:* Вопрос\n"
                + "📝 *Вопрос:* Test Question\n"
                + "👤 *Автор:* TestUser\n"
                + "⏰ *Время:* 2024-02-20 10:00:00\n"
                + "📄 *Превью:* This is a test question\n"
                + "🔗 https://stackoverflow.com/q/12345";

        LinkUpdate update = new LinkUpdate();
        update.setType("STACKOVERFLOW_QUESTION");
        update.setTitle("Test Question");
        update.setAuthor("TestUser");
        update.setCreatedAt(OffsetDateTime.now());
        update.setDescription(formattedMessage);
        update.setUrl("https://stackoverflow.com/q/12345");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertEquals(formattedMessage, result);
    }

    @Test
    void formatUpdateMessage_WhenStackOverflowAnswer_ReturnsFormattedMessage() {
        // Arrange
        LinkUpdate update = new LinkUpdate();
        update.setType("ANSWER");
        update.setTitle("Test Question");
        update.setAuthor("AnswerUser");
        update.setCreatedAt(OffsetDateTime.parse("2024-02-20T10:00:00Z"));
        update.setDescription("This is a test answer");
        update.setUrl("https://stackoverflow.com/a/67890");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertTrue(result.contains("🔔 *Новое обновление на StackOverflow*"));
        assertTrue(result.contains("📌 *Тип:* Ответ"));
        assertTrue(result.contains("📝 *Вопрос:* Test Question"));
        assertTrue(result.contains("👤 *Автор:* AnswerUser"));
        assertTrue(result.contains("⏰ *Время:* 2024-02-20 10:00:00"));
        assertTrue(result.contains("📄 *Превью:*\nThis is a test answer"));
        assertTrue(result.contains("🔗 https://stackoverflow.com/a/67890"));
    }

    @Test
    void formatUpdateMessage_WhenGitHubPullRequest_ReturnsFormattedMessage() {
        // Arrange
        LinkUpdate update = new LinkUpdate();
        update.setType("PULL_REQUEST");
        update.setTitle("Add new feature");
        update.setAuthor("developer");
        update.setCreatedAt(OffsetDateTime.parse("2024-02-20T10:00:00Z"));
        update.setDescription("Added awesome new feature");
        update.setUrl("https://github.com/owner/repo/pull/1");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertTrue(result.contains("🔔 *Новое обновление на GitHub*"));
        assertTrue(result.contains("📌 *Тип:* Pull Request"));
        assertTrue(result.contains("📝 *Название:* Add new feature"));
        assertTrue(result.contains("👤 *Автор:* developer"));
        assertTrue(result.contains("⏰ *Время:* 2024-02-20 10:00:00"));
        assertTrue(result.contains("📄 *Описание:*\nAdded awesome new feature"));
        assertTrue(result.contains("🔗 https://github.com/owner/repo/pull/1"));
    }

    @Test
    void formatUpdateMessage_WhenGitHubIssue_ReturnsFormattedMessage() {
        // Arrange
        LinkUpdate update = new LinkUpdate();
        update.setType("ISSUE");
        update.setTitle("Fix bug");
        update.setAuthor("reporter");
        update.setCreatedAt(OffsetDateTime.parse("2024-02-20T10:00:00Z"));
        update.setDescription("Found a critical bug");
        update.setUrl("https://github.com/owner/repo/issues/1");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertTrue(result.contains("🔔 *Новое обновление на GitHub*"));
        assertTrue(result.contains("📌 *Тип:* Issue"));
        assertTrue(result.contains("📝 *Название:* Fix bug"));
        assertTrue(result.contains("👤 *Автор:* reporter"));
        assertTrue(result.contains("⏰ *Время:* 2024-02-20 10:00:00"));
        assertTrue(result.contains("📄 *Описание:*\nFound a critical bug"));
        assertTrue(result.contains("🔗 https://github.com/owner/repo/issues/1"));
    }

    @Test
    void formatUpdateMessage_WhenGitHubCommit_ReturnsFormattedMessage() {
        // Arrange
        LinkUpdate update = new LinkUpdate();
        update.setType("COMMIT");
        update.setTitle("Update README.md");
        update.setAuthor("committer");
        update.setCreatedAt(OffsetDateTime.parse("2024-02-20T10:00:00Z"));
        update.setDescription("Updated project documentation");
        update.setUrl("https://github.com/owner/repo/commit/abc123");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertTrue(result.contains("🔔 *Новый коммит на GitHub*"));
        assertTrue(result.contains("📝 *Сообщение:* Update README.md"));
        assertTrue(result.contains("👤 *Автор:* committer"));
        assertTrue(result.contains("⏰ *Время:* 2024-02-20 10:00:00"));
        assertTrue(result.contains("📄 *Изменения:*\nUpdated project documentation"));
        assertTrue(result.contains("🔗 https://github.com/owner/repo/commit/abc123"));
    }

    @Test
    void formatUpdateMessage_WhenGitHubRepository_ReturnsFormattedMessage() {
        // Arrange
        LinkUpdate update = new LinkUpdate();
        update.setType("REPOSITORY");
        update.setTitle("awesome-project");
        update.setAuthor("owner");
        update.setCreatedAt(OffsetDateTime.parse("2024-02-20T10:00:00Z"));
        update.setDescription("Repository description");
        update.setUrl("https://github.com/owner/awesome-project");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertTrue(result.contains("🔔 *Обновление репозитория на GitHub*"));
        assertTrue(result.contains("📝 *Репозиторий:* awesome-project"));
        assertTrue(result.contains("👤 *Владелец:* owner"));
        assertTrue(result.contains("⏰ *Время:* 2024-02-20 10:00:00"));
        assertTrue(result.contains("📄 *Описание:*\nRepository description"));
        assertTrue(result.contains("🔗 https://github.com/owner/awesome-project"));
    }

    @Test
    void formatUpdateMessage_WhenNullUpdate_ReturnsDefaultMessage() {
        // Act
        String result = messageFormatter.formatUpdateMessage((LinkUpdate) null);

        // Assert
        assertEquals("Нет данных для отображения", result);
    }

    @Test
    void formatUpdateMessage_WhenUnknownType_ReturnsRawFormat() {
        // Arrange
        LinkUpdate update = new LinkUpdate();
        update.setType("UNKNOWN");
        update.setTitle("Title");
        update.setAuthor("Author");
        update.setCreatedAt(OffsetDateTime.parse("2024-02-20T10:00:00Z"));
        update.setDescription("Description");
        update.setUrl("https://example.com");

        // Act
        String result = messageFormatter.formatUpdateMessage(update);

        // Assert
        assertEquals("UNKNOWN||Title||Author||2024-02-20 10:00:00||Description||https://example.com", result);
    }

    @Test
    void formatUpdateMessage_WhenRawMessage_ParsesAndFormatsCorrectly() {
        // Arrange
        String rawMessage =
                "ANSWER||Test Question||AnswerUser||2024-02-20T10:00:00Z||This is a test answer||https://stackoverflow.com/a/67890";

        // Act
        String result = messageFormatter.formatUpdateMessage(rawMessage);

        // Assert
        assertTrue(result.contains("🔔 *Новое обновление на StackOverflow*"));
        assertTrue(result.contains("📌 *Тип:* Ответ"));
        assertTrue(result.contains("📝 *Вопрос:* Test Question"));
        assertTrue(result.contains("👤 *Автор:* AnswerUser"));
        assertTrue(result.contains("⏰ *Время:* 2024-02-20 10:00:00"));
        assertTrue(result.contains("📄 *Превью:*\nThis is a test answer"));
        assertTrue(result.contains("🔗 https://stackoverflow.com/a/67890"));
    }
}
