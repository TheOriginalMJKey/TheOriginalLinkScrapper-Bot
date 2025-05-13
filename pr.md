# Scrapper Application

## Requirements Checklist

### Functional Requirements

- [x] StackOverflow updates with:
  - [x] Question text
  - [x] Username
  - [x] Creation time
  - [x] Preview of answer/comment (200 chars)
- [x] GitHub updates with:
  - [x] PR/Issue title
  - [x] Username
  - [x] Creation time
  - [x] Description preview (200 chars)

### Non-functional Requirements

- [x] Batch processing of links (no full memory load)
- [x] Separated scheduler and notification services
- [x] Defined notification service interface (scrapper -> bot)
- [x] HTTP implementation for notifications
- [x] PostgreSQL database usage
- [x] Docker Compose for local development
- [x] Testcontainers for testing
- [x] SQL migrations with Liquibase
- [x] Manual migration execution
- [x] Dual DB access implementation (SQL/ORM)
- [x] Configuration-based DB access selection
- [x] Testcontainers in tests
- [x] DB operation tests (insert/delete/update)
- [x] Different implementation tests (SQL/ORM)
- [x] Message preview tests by update type
- [x] Indexes on WHERE fields
- [x] Pagination for data processing
- [x] Configurable batch size (50-500)
- [x] Configurable update check interval
- [x] SQL-level filtering for updates
- [x] Business logic separation from data access
- [x] Implementation-agnostic interfaces 