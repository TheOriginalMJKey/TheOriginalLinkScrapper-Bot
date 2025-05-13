package backend.academy.bot.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    @Scheduled(fixedRate = 300_000) // Каждые 5 минут
    public void checkUpdates() {
        System.out.println("Проверка обновлений...");
        // TODO: Реализовать проверку обновлений через Scrapper API
    }
}
