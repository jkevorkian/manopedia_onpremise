# On-Premise Android App Implementation Plan

This document outlines the plan to transform the existing client-server application into a fully on-premise Android application that does not depend on an internet connection or a separate server.

## 1. Goal

The primary goal is to create a single Android application that embeds the backend logic and a local database, allowing the app to run entirely offline.

## 2. Core Principles

- **Single Codebase:** The backend logic will be re-implemented in Dart within the Flutter project to maintain a single codebase.
- **Local Database:** MongoDB will be replaced with a mobile-friendly, embedded database.
- **No Network Calls:** All HTTP/REST API calls will be replaced with direct function calls to the embedded backend logic.

## 3. Implementation Steps

### Step 1: Database Migration

- **Choice of Database:** We will use **SQLite** via the `sqflite` package. This is a mature, reliable, and well-supported solution for Flutter.
- **Schema Definition:** The existing MongoDB collections will be mapped to a relational schema in SQLite. This will involve creating tables for `users`, `ejercicio`, `ejercicio_realizado`, `user_favorites`, `achievements_users`, `historial_ingresos`, and `categorias`.
- **Data Seeding:** The initial exercise data will be seeded into the SQLite database when the app is first launched.

### Step 2: Backend Logic Re-implementation

- **Language:** All backend logic will be re-implemented in **Dart**.
- **Structure:** A new directory, `lib/local_backend`, will be created to house the re-implemented backend logic. This will include services for user management, exercises, dictionary, statistics, and achievements.
- **Authentication:** User authentication will be handled locally by querying the SQLite database.
- **Business Logic:** The business logic from the Spring Boot services will be ported to Dart, including:
    - User registration and login
    - Exercise fetching and completion
    - Dictionary searching and filtering
    - Statistics and progress calculation
    - Achievement unlocking

### Step 3: API Layer Replacement

- **Direct Function Calls:** All existing HTTP/REST API calls in the Flutter frontend will be replaced with direct function calls to the new Dart-based backend services.
- **Data Models:** The existing Dart data models will be used to pass data between the UI and the local backend.

### Step 4: File Structure

The following file structure will be implemented:

```
frontend-mobile/
  lib/
    local_backend/
      database/
        database_helper.dart
        schema.dart
      services/
        user_service.dart
        exercise_service.dart
        dictionary_service.dart
        statistics_service.dart
        achievements_service.dart
      models/
        ...
    ...
```

### Step 5: Testing

- **Unit Tests:** Unit tests will be written for all the re-implemented backend logic in Dart.
- **Integration Tests:** Integration tests will be written to test the interaction between the Flutter UI and the new local backend module.
- **End-to-End Tests:** End-to-end tests will be written to verify the entire application flow.

## 4. Challenges and Considerations

- **Development Effort:** This is a significant undertaking that will require a considerable amount of development time.
- **App Size:** The app size will increase due to the embedded database and backend logic.
- **Security:** All data will be stored on the user's device, so it is crucial to implement proper security measures to protect sensitive data.

## 5. Timeline

This project is estimated to take **4-6 weeks** to complete, including development and testing.
