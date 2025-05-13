package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import java.util.List;
import java.util.Optional;

public interface ChatRepository {
    void add(Chat chat);

    List<Chat> findAll();

    Optional<Chat> findById(Long id);

    Optional<Chat> findByChatId(Long chatId);

    void remove(Long chatId);

    void addLinkToChat(Chat chat, Link link);

    void removeLinkFromChat(Chat chat, Link link);

    boolean exists(Long chatId);

    // Methods to satisfy InMemory implementation
    default Chat save(Chat chat) {
        add(chat);
        return chat;
    }

    default void delete(Long chatId) {
        remove(chatId);
    }

    default boolean existsByChatId(Long chatId) {
        return exists(chatId);
    }

    default Optional<Chat> findWithLinksByChatId(Long chatId) {
        return findByChatId(chatId);
    }
}
