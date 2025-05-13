package backend.academy.bot.service;

import backend.academy.bot.dto.LinkUpdate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class MessageFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String formatUpdateMessage(LinkUpdate update) {
        if (update == null) {
            return "Нет данных для отображения";
        }

        String type = update.getType() != null ? update.getType() : "UNKNOWN";
        String title = update.getTitle() != null ? update.getTitle() : "Untitled";
        String username = update.getAuthor() != null ? update.getAuthor() : "Unknown";
        String time = update.getCreatedAt() != null ? update.getCreatedAt().format(DATE_FORMATTER) : "Unknown time";
        String description = update.getDescription() != null ? update.getDescription() : "";
        String url = update.getUrl() != null ? update.getUrl() : "";

        StringBuilder sb = new StringBuilder();

        switch (type) {
            case "ANSWER":
            case "COMMENT":
                sb.append("🔔 *Новое обновление на StackOverflow*\n\n");
                sb.append("📌 *Тип:* ")
                        .append(type.equals("ANSWER") ? "Ответ" : "Комментарий")
                        .append("\n");
                sb.append("📝 *Вопрос:* ").append(title).append("\n");
                sb.append("👤 *Автор:* ").append(username).append("\n");
                sb.append("⏰ *Время:* ").append(time).append("\n");
                if (description != null && !description.isEmpty()) {
                    sb.append("\n📄 *Превью:*\n").append(description);
                }
                sb.append("\n\n🔗 ").append(url);
                break;

            case "STACKOVERFLOW_QUESTION":
                // Для вопросов StackOverflow мы уже форматируем сообщение в StackoverflowService
                // и передаем его в поле description, поэтому просто возвращаем его как есть
                return description;

            case "PULL_REQUEST":
            case "ISSUE":
                sb.append("🔔 *Новое обновление на GitHub*\n\n");
                sb.append("📌 *Тип:* ")
                        .append(type.equals("PULL_REQUEST") ? "Pull Request" : "Issue")
                        .append("\n");
                sb.append("📝 *Название:* ").append(title).append("\n");
                sb.append("👤 *Автор:* ").append(username).append("\n");
                sb.append("⏰ *Время:* ").append(time).append("\n");
                if (description != null && !description.isEmpty()) {
                    sb.append("\n📄 *Описание:*\n").append(description);
                }
                sb.append("\n\n🔗 ").append(url);
                break;

            case "COMMIT":
                sb.append("🔔 *Новый коммит на GitHub*\n\n");
                sb.append("📝 *Сообщение:* ").append(title).append("\n");
                sb.append("👤 *Автор:* ").append(username).append("\n");
                sb.append("⏰ *Время:* ").append(time).append("\n");
                if (description != null && !description.isEmpty()) {
                    sb.append("\n📄 *Изменения:*\n").append(description);
                }
                sb.append("\n\n🔗 ").append(url);
                break;

            case "REPOSITORY":
                sb.append("🔔 *Обновление репозитория на GitHub*\n\n");
                sb.append("📝 *Репозиторий:* ").append(title).append("\n");
                sb.append("👤 *Владелец:* ").append(username).append("\n");
                sb.append("⏰ *Время:* ").append(time).append("\n");
                if (description != null && !description.isEmpty()) {
                    sb.append("\n📄 *Описание:*\n").append(description);
                }
                sb.append("\n\n🔗 ").append(url);
                break;

            default:
                return String.format("%s||%s||%s||%s||%s||%s", type, title, username, time, description, url);
        }

        return sb.toString();
    }

    public String formatUpdateMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isEmpty()) {
            return "Нет данных для отображения";
        }

        String[] parts = rawMessage.split("\\|\\|");
        if (parts.length < 6) {
            return rawMessage;
        }

        LinkUpdate update = new LinkUpdate();
        update.setType(parts[0]);
        update.setTitle(parts[1]);
        update.setAuthor(parts[2]);
        try {
            update.setCreatedAt(OffsetDateTime.parse(parts[3]));
        } catch (Exception e) {
            // Если не удалось распарсить дату, используем null
            update.setCreatedAt(null);
        }
        update.setDescription(parts[4]);
        update.setUrl(parts[5]);

        return formatUpdateMessage(update);
    }
}
