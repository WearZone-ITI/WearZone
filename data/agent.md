# Data Module

## Overview

The `data` module is responsible for handling all data operations in the application.

It implements repository contracts defined in the `domain` module and manages communication with:

* Remote APIs
* Local Database (Room)
* Local Preferences (DataStore)
* Data Sources
* Repository Implementations

This module follows **Clean Architecture** principles and remains isolated from UI concerns.

---

## Responsibilities

### Remote Data Handling

* Execute API requests
* Handle authentication
* Parse responses
* Map DTOs to domain models
* Handle network failures

### Local Data Handling

* Cache data using Room
* Store lightweight settings with DataStore
* Expose reactive streams with Flow

### Repository Implementation

* Implement repository interfaces from domain
* Coordinate remote and local sources
* Handle dispatcher switching internally

---

## Module Dependency

```text
data
 └── depends on → domain
```

Rules:

* Import only `domain`
* Must not import `presentation`
* Dependency injection configuration lives outside this module

---

## Package Structure

```text
data/
├── db/
│   └── WearZoneDatabase.kt

├── local/
│   ├── dao/
│   ├── entity/
│   └── datasource/

├── remote/
│   ├── api/
│   ├── dto/
│   ├── interceptor/
│   └── datasource/

└── repository/
```

---

## Tech Stack

| Layer                 | Technology            |
| --------------------- | --------------------- |
| Networking            | Retrofit + OkHttp     |
| Serialization         | kotlinx.serialization |
| Local Database        | Room                  |
| Preferences           | DataStore             |
| Async                 | Kotlin Coroutines     |
| Streams               | Kotlin Flow           |
| Annotation Processing | KSP                   |

---

## Architecture Flow

```text
Presentation
      ↓
UseCase
      ↓
Repository Interface (Domain)
      ↓
Repository Implementation (Data)
      ↓
Remote / Local Data Source
      ↓
Database / API
```

---

## Data Layer Rules

### Repository

* Implements domain contracts only
* Handles dispatcher switching internally
* Returns `Result<T>`

### DTO

* Exists only in remote layer
* Maps to domain models

### Room

* Entities live under `local/entity`
* DAO methods use `suspend` or `Flow`

### DataSource

* Abstract access to remote/local providers

---

## Coroutines Rules

```kotlin
withContext(ioDispatcher) {
    runCatchingCancellable {
        // data operation
    }
}
```

Rules:

* Dispatchers injected at implementation level only
* ViewModels remain dispatcher-agnostic
* Use Flow for streams
* Use Result<T> for outcomes

---

## Notes

* No business logic inside data.
* No UI references.
* Domain models never depend on DTOs.
* Keep repositories isolated and testable.
