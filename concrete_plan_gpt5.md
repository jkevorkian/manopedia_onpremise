# Concrete On-Premise Android Standalone Plan (GPT-5)

## Overview

We will convert the app to run fully on-device by embedding the backend logic and local data store inside the Flutter app. We introduce a Repository abstraction for persistence now backed by an in-memory store to get a working offline app fast. Later, swap the internals to SQLite without touching the UI. The UI remains intact, minimizing changes.

## Key Decisions

- Introduce `DataRepository` as a middle layer for persistence (`lib/persistence/data_repository.dart`).
- Keep the current Flutter UI and routing; update the networking dispatcher to call the repository.
- Seed initial data and a default offline user on first run.

## Implementation Steps

1) Database and Seeding
- Implement `DataRepository` with an in-memory dataset:
  - Users (default: `a@local` / `a`)
  - Exercises (small seed set)
  - Favorites and simple review queues
- This interface will be preserved when moving to SQLite.

2) Local Services via Repository
- Exercise flow: move completed new → review; remove on success; counters.
- Statistics: simple percentage and level derived from completed/new.
- Profile: notifications and password change operations.
- Achievements: simple counters; can be extended later.

3) Replace Networking with Local Dispatcher
- Keep the local dispatcher at `utils/networking/networking.dart`, but delegate to `DataRepository` instead of HTTP/Realm.
- Maintain the same function signatures in `*_networking.dart` files so UI code remains unchanged.
- Manage session via `SecureStorage` storing a local `token` and `userId`.

4) UX and Startup
- Keep existing native channel init in `RootPage`, but add a fallback path to proceed if the native init is unavailable.
- Document the offline credentials and behavior in `frontend-mobile/README.md`.

## Result

- The app no longer depends on Docker or a remote Spring Boot server.
- All API calls are served by a local repository (in-memory now, SQLite-ready).
- A default user exists for immediate use; all core features (login, lessons, review, dictionary, favorites, notifications settings, achievements summary, progress) work offline.

## TODOs for SQLite Migration

- Define sqflite schema and DAOs mirroring `DataRepository` structures (users, exercises, favorites, progress).
- Swap method bodies in `DataRepository` to use sqflite; keep signatures.
- Add a factory/flag to select in-memory vs SQLite implementation.
- Implement migrations and data seeding on first run.
- Add unit tests for both implementations to ensure parity.


