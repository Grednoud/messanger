# Сетевой чат / Network Chat

Учебное многомодульное Java-приложение: клиент-серверный чат с JavaFX интерфейсом.

An educational multi-module Java application: client-server chat with JavaFX UI.

![Java](https://img.shields.io/badge/Java-21-orange)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin_DSL-green)

## Возможности / Features

- **Клиент-серверная архитектура / Client-server architecture:**
  - TCP-сокеты для обмена сообщениями
  - JSON-протокол для удобства отладки
  
- **Аутентификация / Authentication:**
  - Простая проверка логин/пароль
  - Список онлайн-пользователей

- **Современный интерфейс / Modern UI:**
  - Тёмная тема (Catppuccin-inspired)
  - Диалог подключения с настройками сервера
  - Список пользователей и история сообщений

## Структура проекта / Project Structure

```
messenger/
├── message-protocol/       # Протокол сообщений (общий для клиента и сервера)
│   └── src/main/java/
│       └── ru/codesteps/protocol/
│           ├── Message.java    # Sealed interface с типами сообщений
│           └── Protocol.java   # Сериализация/десериализация JSON
│
├── chat-server/            # Сервер чата
│   └── src/main/java/
│       └── ru/codesteps/server/
│           ├── ChatServerApp.java      # Точка входа
│           ├── ChatServer.java         # Управление подключениями
│           ├── ClientHandler.java      # Обработка клиента
│           ├── AuthService.java        # Интерфейс аутентификации
│           └── InMemoryAuthService.java
│
├── chat-client/            # JavaFX клиент
│   └── src/main/java/
│       └── ru/codesteps/client/
│           ├── ChatClientApp.java      # Точка входа JavaFX
│           ├── NetworkClient.java      # Сетевое взаимодействие
│           └── ui/
│               ├── MainView.java       # Основной интерфейс
│               └── LoginDialog.java    # Диалог подключения
│
├── build.gradle.kts        # Корневой Gradle-скрипт
├── settings.gradle.kts     # Настройки многомодульного проекта
└── README.md
```

## Требования / Requirements

- **JDK 21** или новее / JDK 21 or newer
- Gradle (wrapper включён) / Gradle (wrapper included)

## Сборка и запуск / Build & Run

```bash
# Клонировать репозиторий / Clone the repository
git clone <repository-url>
cd messenger

# Собрать проект / Build the project
./gradlew build

# Запустить тесты / Run tests
./gradlew test
```

### Запуск сервера / Running the Server

```bash
# По умолчанию порт 11111 / Default port 11111
./gradlew :chat-server:run

# Или с указанием порта / Or specify port
./gradlew :chat-server:run --args="12345"
```

Сервер создаёт тестовых пользователей:
- `Alex` / `123`
- `Bob` / `234`
- `Clod` / `345`

### Запуск клиента / Running the Client

```bash
./gradlew :chat-client:run
```

В диалоге подключения укажите:
- Сервер: `localhost` (или IP сервера)
- Порт: `11111`
- Логин и пароль одного из тестовых пользователей

На Windows используйте `gradlew.bat` вместо `./gradlew`.

## Архитектура / Architecture

### Протокол сообщений / Message Protocol

Все сообщения передаются как JSON-строки в формате:
```json
{"type": "MESSAGE_TYPE", "data": {...}}
```

Типы сообщений:
- `AUTH` — запрос аутентификации
- `AUTH_RESULT` — результат аутентификации
- `CHAT` — текстовое сообщение
- `USER_CONNECTED` — пользователь подключился
- `USER_DISCONNECTED` — пользователь отключился
- `USER_LIST` — список онлайн-пользователей
- `DISCONNECT` — запрос отключения

### Ключевые принципы / Key Principles

1. **Модульность** — протокол вынесен в отдельный модуль для переиспользования
2. **Современный Java** — sealed interfaces, records, virtual threads
3. **Читаемость** — код написан для обучения, избегает сложных паттернов
4. **Тестируемость** — unit-тесты для протокола и сервиса аутентификации

## Тестирование / Testing

```bash
# Запустить все тесты
./gradlew test

# Тесты с детальным выводом
./gradlew test --info
```

Тесты покрывают:
- Сериализацию/десериализацию всех типов сообщений
- Обработку ошибок парсинга
- Логику аутентификации

## История / History

Этот проект — модернизация учебного Java-приложения с целью демонстрации современного подхода к разработке:
- Java 8 → Java 21
- Maven → Gradle (Kotlin DSL)
- JavaFX 12 → JavaFX 21
- PostgreSQL → In-memory (для простоты запуска)
- DataInputStream/DataOutputStream → BufferedReader/PrintWriter + JSON

## Лицензия / License

Educational project / Учебный проект
