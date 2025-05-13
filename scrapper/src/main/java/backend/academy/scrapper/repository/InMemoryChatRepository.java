package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import backend.academy.scrapper.entity.Link;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryChatRepository implements ChatRepository {
    private final Map<Long, Chat> chatStorage = new HashMap<>(); // Ключ - chatId

    @Override
    public void add(Chat chat) {
        if (chat.chatId() == null) {
            throw new IllegalArgumentException("Chat ID cannot be null");
        }
        chatStorage.put(chat.chatId(), chat);
    }

    @Override
    public List<Chat> findAll() {
        return new ArrayList<>(chatStorage.values());
    }

    @Override
    public Optional<Chat> findById(Long id) {
        return chatStorage.values().stream()
                .filter(chat -> chat.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Chat> findByChatId(Long chatId) {
        return Optional.ofNullable(chatStorage.get(chatId));
    }

    @Override
    public void remove(Long chatId) {
        chatStorage.remove(chatId);
    }

    @Override
    public void addLinkToChat(Chat chat, Link link) {
        chat.getLinks().add(link);
        link.getChats().add(chat);
    }

    @Override
    public void removeLinkFromChat(Chat chat, Link link) {
        chat.getLinks().remove(link);
        link.getChats().remove(chat);
    }

    @Override
    public boolean exists(Long chatId) {
        return chatStorage.containsKey(chatId);
    }

    // Methods from default implementations in the interface that we're overriding
    @Override
    public Chat save(Chat chat) {
        add(chat);
        return chat;
    }

    @Override
    public void delete(Long chatId) {
        remove(chatId);
    }

    @Override
    public boolean existsByChatId(Long chatId) {
        return exists(chatId);
    }

    @Override
    public Optional<Chat> findWithLinksByChatId(Long chatId) {
        return findByChatId(chatId);
    }
}
