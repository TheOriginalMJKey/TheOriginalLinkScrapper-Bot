# 🌐 LinkTracker — приложение для отслеживания обновлений контента

## 📌 Описание проекта

**LinkTracker** — это система уведомлений о новых изменениях на веб-страницах. Пользователь может подписаться на интересующие его ссылки (например, вопросы на StackOverflow или репозитории на GitHub), и получать уведомления о новых ответах, комментариях или обновлениях через Telegram.

Это помогает пользователям не переключаться между множеством сайтов, а централизованно следить за важными им ресурсами.

---

## 🧩 Сценарий использования

1. **Регистрация**: пользователь запускает бота в Telegram и регистрируется.
2. **Добавление ссылок**: пользователь использует команду `/track <ссылка>` для подписки на обновления.
3. **Удаление ссылок**: команда `/untrack <ссылка>` прекращает отслеживание.
4. **Просмотр подписок**: команда `/list` показывает все текущие ссылки.
5. **Получение уведомлений**: при обнаружении изменений бот присылает уведомление с кратким текстом нового контента.

---

## 🛠 Архитектура системы

```
+------------------+       +------------------+
|                  | HTTP  |                  |
|     Bot App      |<----->|   Scrapper App   |
| (Telegram bot)   |       | (monitoring URLs)|
+--------+---------+       +--------+---------+
         |                          |
         | Kafka                    | Kafka
         v                          v
   +---------------------------------------------+
   |              PostgreSQL / Message Broker    |
   +---------------------------------------------+
```

### 🔹 Bot Application
- Обрабатывает команды пользователя через Telegram
- Регистрация, добавление/удаление ссылок
- Получает уведомления от `scrapper` через HTTP или Kafka
- Хранит данные в PostgreSQL

### 🔹 Scrapper Application
- Регулярно проверяет обновления по всем отслеживаемым ссылкам
- Отправляет уведомления через HTTP или Kafka
- Поддерживает API GitHub и StackOverflow

---

## 💡 Дополнительные функции

- **Фильтрация уведомлений**: возможность игнорировать определённых авторов
- **Тэгирование ссылок**: группировка по темам (например, "Работа", "Хобби")
- **Батчинг уведомлений**: отправка дайджеста раз в день по расписанию
- **Сохранение истории обновлений**: хранение предыдущих изменений

---

## 🧰 Технические детали

### ✅ Используемые технологии:
- **Java 17+**
- **Spring Boot** (или аналоги, в зависимости от выбора)
- **PostgreSQL**
- **Redis**
- **Apache Kafka** (опционально)
- **Telegram Bot API**
- **Liquibase** для миграций БД
- **Testcontainers** для тестирования БД
- **Docker Compose** для локальной разработки


## 🧪 Тестирование

- Все модули покрыты юнит-тестами
- Для внешних API используются моки
- Интеграционное тестирование через Testcontainers
- SQL-миграции проверяются автоматически
- Разделение бизнес-логики и доступа к данным

---

## 📈 Расширяемость

- Возможность добавления новых источников (Reddit, YouTube и т.д.)
- Поддержка других каналов уведомлений (Email, Push, SMS)
- Админ-панель для управления пользователями и подписками
- Визуализация активности через Grafana / Prometheus

---

## 🚀 Как запустить

1. Убедитесь, что установлены:
   - JDK 17+
   - Maven / Gradle
   - Docker и Docker Compose

2. Запустите окружение:

```bash
docker-compose up -d
```

3. Запустите сервисы:

```bash
./mvnw spring-boot:run -pl bot
./mvnw spring-boot:run -pl scrapper
```

4. Начните диалог с Telegram-ботом

---

## 🙌 Благодарность

Спасибо за использование! Проект еще в разработке, совсем скоро появится ассинхронная обработка и кэширование!
