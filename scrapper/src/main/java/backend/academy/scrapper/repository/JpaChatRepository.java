package backend.academy.scrapper.repository;

import backend.academy.scrapper.entity.Chat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<Chat, Long> {
    Optional<Chat> findByChatId(Long chatId);

    boolean existsByChatId(Long chatId);
}
