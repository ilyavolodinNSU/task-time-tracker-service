# Task Time Tracker API

Сервис учета рабочего времени сотрудников. Позволяет управлять задачами, фиксировать затраченное время и анализировать активность сотрудников за указанный период. Реализован как REST-приложение на Spring Boot с ролевым доступом, валидацией данных, централизованной обработкой ошибок и контейнеризированной инфраструктурой.

## Стек технологий
| Компонент | Технология |
|-----------|------------|
| Язык | Java 21 |
| Фреймворк | Spring Boot, Spring WebMVC, Spring Security (OAuth2 Resource Server) |
| Работа с БД | MyBatis 4.0.1, Flyway (миграции), PostgreSQL 18 |
| Аутентификация | Keycloak 26.5 (OIDC, JWT Bearer) |
| Документация | SpringDoc OpenAPI 3.0.2 |
| Тестирование | JUnit 5, Mockito, AssertJ, TestContainers, JaCoCo |
| Сборка и деплой | Apache Maven, Docker, Docker Compose v2, Multi-stage Dockerfile |

## Реализованная функциональность
В соответствии с техническим заданием реализованы следующие сущности и эндпоинты:

| Метод | Путь | Описание | Требуемая роль |
|-------|------|----------|----------------|
| `POST` | `/api/tasks` | Создание задачи | `MANAGER` |
| `GET` | `/api/tasks/{id}` | Получение задачи по ID | `EMPLOYEE`, `MANAGER` |
| `PATCH` | `/api/tasks/{id}/status` | Изменение статуса задачи | `MANAGER` |
| `POST` | `/api/time-records` | Фиксация затраченного времени | `MANAGER` |
| `GET` | `/api/time-records` | Получение записей времени сотрудника за период | `EMPLOYEE`, `MANAGER` |

**Дополнительно реализовано:**
- Декларативная документация API через SpringDoc OpenAPI (`/swagger-ui.html`)
- Валидация входящих DTO через Jakarta Bean Validation
- Централизованная обработка исключений через `@RestControllerAdvice`
- Интеграционные тесты DAO-слоя с использованием TestContainers
- Bearer Authentication (JWT) с валидацией issuer и mapping ролей из `realm_access`

## Архитектура и инженерные решения
- **Многослойная структура**: Контроллеры -> Сервисы -> Репозитории -> MyBatis Мапперы. Бизнес-логика инкапсулирована в сервисном слое, контроллеры отвечают только за маршрутизацию и сериализацию.
- **Иммутабельность DTO**: Использование `java records` исключает побочные эффекты и упрощает передачу данных между слоями.
- **Валидация**: Проверка синтаксиса (`@NotBlank`, `@NotNull`) выполняется на уровне контроллера. Бизнес-правила (корректность временных интервалов, запрет изменения статуса завершенных задач) валидируются в сервисах.
- **Обработка ошибок**: `GlobalExceptionHandler` перехватывает все типы исключений и возвращает унифицированный JSON-ответ с временной меткой, HTTP-статусом, описанием ошибки и путем запроса. Отдельно обрабатываются `AccessDeniedException` (`403`), `ResourceNotFoundException` (`404`), `ValidationConflictException` (`409`) и непредвиденные ошибки (`500`).
- **Миграции БД**: Flyway автоматически применяет `V1__init.sql` при старте, гарантируя идентичность схемы во всех средах.
- **Контейнеризация**: Многоэтапный Dockerfile собирает образ на базе `eclipse-temurin:21-jre-alpine`. Приложение запускается от не-root пользователя, использует настройки JVM для контейнеров (`MaxRAMPercentage`) и интегрируется с Docker healthchecks.

## Безопасность и тестовые пользователи
Сервис требует аутентификации через JWT Bearer Token. Роли извлекаются из JWT-клейма `realm_access.roles` и преобразуются в формат Spring Security (`ROLE_...`). Доступ к методам контролируется аннотацией `@PreAuthorize`.

Для тестирования в рилме `task-time-tracker` предустановлены два пользователя:

| Логин | Пароль | Роли | Уровень доступа |
|-------|--------|------|-----------------|
| `manager` | `manager123` | `manager`, `employee` | Полный доступ: создание/обновление задач и записей времени, чтение всех данных |
| `employee` | `employee123` | `employee` | Ограниченный доступ: чтение задач и записей времени. Операции записи/изменения возвращают `403 Forbidden` |

**Получение токена (пример):**
```bash
curl -X POST http://localhost:8081/realms/task-time-tracker/protocol/openid-connect/token \
  -d "client_id=task-time-tracker-api&username=manager&password=manager123&grant_type=password" \
  -H "Content-Type: application/x-www-form-urlencoded"
```
Токен передается в заголовке всех защищённых запросов: `Authorization: Bearer <access_token>`

## Требования к окружению
- Docker & Docker Compose v2.20+
- Java 21+ (для локальной сборки)
- Maven 3.8+ (в проекте используется `mvnw`, дополнительная установка не требуется)

## Сборка и запуск (Docker Compose)
Инфраструктура разворачивается одной командой. Compose управляет зависимостями, healthcheck-ами и очередностью запуска (БД -> Keycloak -> Приложение).

1. Перейдите в корень проекта.
2. Запустите стек:
   ```bash
   docker compose up -d --build
   ```
3. Дождитесь полной инициализации (30–60 секунд). Проверьте статус:
   ```bash
   docker compose ps
   curl http://localhost:8083/actuator/health
   # Ожидаемый ответ: {"status":"UP"}
   ```

**Порты сервисов:**
- Приложение API: `8083`
- Swagger UI: `http://localhost:8083/swagger-ui.html`
- Keycloak Admin Console: `http://localhost:8081` (логин: `admin`, пароль: `admin`)
- PostgreSQL: `5432`

## Локальная разработка (без Docker)
Для запуска вне контейнеров требуется локальная PostgreSQL.

1. Создайте базу данных: `CREATE DATABASE tasktimetracker_service;`
2. Создайте профиль `application-local.yml` с локальными путями к БД и Keycloak (`localhost`).
3. Соберите и запустите приложение:
   ```bash
   ./mvnw clean package -DskipTests
   java -jar target/task-time-tracker-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
   ```

## Тестирование и покрытие кода
Проект содержит полный набор тестов. Юнит-тесты покрывают бизнес-логику и контроллеры (~99% строкового покрытия сервисного слоя). Интеграционные тесты валидируют взаимодействие с БД.

```bash
# Запуск всех тестов
./mvnw test

# Только интеграционные тесты DAO-слоя (TestContainers + Flyway)
./mvnw test -Dtest="*RepositoryIntegrationTest"

# Генерация отчета JaCoCo
./mvnw test jacoco:report
# HTML-отчет: target/site/jacoco/index.html
```
Интеграционные тесты автоматически поднимают изолированный контейнер PostgreSQL, применяют миграции и откатывают транзакции после каждого тестового метода, обеспечивая полную изоляцию данных.

## Документация API и Postman
В корень репозитория добавлена коллекция запросов: `task-time-tracker.postman_collection.json`.

**Как использовать:**
1. Импортируйте файл в Postman (`Import` -> `File`).
2. Запустите папку `0. Аутентификация` для автоматического получения и сохранения токенов в переменные окружения (`managerToken`, `employeeToken`).
3. Запускайте папки последовательно. Коллекция содержит assertions для проверки статус-кодов, валидации полей, ролевых ограничений (`403 Forbidden`) и бизнес-ошибок.
4. Для автоматического прогона используйте вкладку `Run` (Collection Runner).

Интерактивная документация доступна по адресу `/swagger-ui.html`. Для тестирования запросов через UI используйте кнопку `Authorize` и вставьте валидный Bearer-токен.

## Конфигурация и переменные окружения
Параметры вынесены в профили Spring. Основные переменные для `docker`-профиля:
- `SPRING_DATASOURCE_URL`: строка подключения к PostgreSQL
- `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI`: URI Issuer Keycloak (должен совпадать с полем `iss` в токене)
- `KC_HOSTNAME_*`: настройки маршрутизации Keycloak (влияют на формирование `iss` в JWT)

## Примечания
- В режиме `start-dev` Keycloak использует встроенную H2-базу для хранения конфигурации. При перезапуске контейнера данные рилма повторно импортируются из `./keycloak/realms`.
- Для production-развертывания рекомендуется перевести Keycloak в production-режим, настроить TLS, вынести секреты во внешний vault/secret manager и ограничить доступ к эндпоинтам управления.