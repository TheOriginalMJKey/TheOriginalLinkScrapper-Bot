package backend.academy.scrapper.service.storage;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.exception.ApiExceptionType;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.service.ChatService;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "SQL")
@SuppressFBWarnings("DLS_DEAD_LOCAL_STORE")
@SuppressWarnings("PMD.UnusedLocalVariable")
public class ChatServiceImp implements ChatService {

    private final ChatRepository chatRepository;

    @Override
    public Chat findByChatId(Long chatId) {
        return chatRepository
                .findWithLinksByChatId(chatId)
                .orElseThrow(() -> ApiExceptionType.CHAT_NOT_FOUND.toException(chatId));
    }

    @Transactional
    @Override
    public void registerChat(Long chatId) {
        if (chatRepository.existsByChatId(chatId)) {
            throw ApiExceptionType.CHAT_ALREADY_EXISTS.toException(chatId);
        }
        Chat chat = new Chat();
        chat.setChatId(chatId); // Явная установка chatId
        chatRepository.save(chat);
        log.debug("new chat{chatId={}} was registered", chatId);
    }

    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        Chat chat = chatRepository
                .findByChatId(chatId)
                .orElseThrow(() -> ApiExceptionType.CHAT_NOT_FOUND.toException(chatId));
        chatRepository.delete(chatId); // Передаем chatId вместо chat.id()
        log.debug("chat{chatId={}} was deleted", chatId);
    }
}
