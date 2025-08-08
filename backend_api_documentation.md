# Backend API Detailed Documentation

This document provides a detailed breakdown of the backend API, including the logic behind each endpoint.

## 1. Authentication and Registration (`/user-service`)

This service handles user creation, login, and token validation.

- **`POST /user-service/login`**:
    - **Description:** Authenticates a user and returns a JWT token.
    - **Request Body:** `LoginRequest` object containing `email` and `password`.
    - **Logic:**
        - It first tries to find the user by email in the `users` collection.
        - If the user is not found and the email is "a", it creates a new test user with the credentials "a"/"a".
        - It then hashes the provided password and compares it to the hashed password stored in the database.
        - If the passwords match, it generates a JWT token with the user's email as the subject and returns it.
        - It also updates the user's last login time and fetches new exercises if the user hasn't logged in that day.

- **`POST /user-service/validar`**:
    - **Description:** Validates a JWT token.
    - **Request Headers:** `Authorization` header with the JWT token.
    - **Logic:**
        - It validates the token's signature and expiration using the `jwt.secret` and `jwt.expiration` properties from the `application.properties` file.
        - If the token is valid, it returns a `TokenOkResponse` with `ok: true`.

- **`POST /user-service/registro/validacion`**:
    - **Description:** Checks if a user already exists with the given email.
    - **Request Body:** `ValidacionRegistroRequest` object containing `email`.
    - **Logic:**
        - It checks if a user with the given email already exists in the `users` collection.

- **`POST /user-service/registro`**:
    - **Description:** Initiates the user registration process.
    - **Request Body:** `RegistroRequest` object containing `email`, `usuario`, `nombre`, and `password`.
    - **Logic:**
        - It creates an `Onboarding` record with a hashed PIN and the user's information.
        - The email sending functionality is currently commented out.

- **`POST /user-service/registro/pin`**:
    - **Description:** Finalizes the user registration process using the PIN.
    - **Request Body:** `RegistroPinRequest` object containing `email` and `pin`.
    - **Logic:**
        - It validates the PIN against the one stored in the `onboarding` collection.
        - If the PIN is correct, it creates a new `Usuario` record, assigns the initial exercises, and creates the user's achievements.

- **`POST /user-service/cambio_password`**:
    - **Description:** Initiates the password change process for a logged-out user.
    - **Request Body:** `CambioPasswordRequest` object containing `email`.
    - **Logic:**
        - It generates a PIN, creates a `ValidacionMail` record, and sends the PIN to the user's email.

- **`POST /user-service/cambio_password/pin`**:
    - **Description:** Validates the PIN for a password change.
    - **Request Body:** `CambioPasswordValidar` object containing `email` and `pin`.
    - **Logic:**
        - It validates the PIN against the one stored in the `validacion_mail` collection.

- **`POST /user-service/cambio_password/nueva`**:
    - **Description:** Sets the new password after a successful PIN validation.
    - **Request Body:** `CambioPasswordPinRequest` object containing `email`, `pin`, and `password`.
    - **Logic:**
        - It validates the PIN and then updates the user's password in the `users` collection.

## 2. User Profile (`/user-service/perfil`)

This service manages user profile information.

- **`GET /user-service/perfil`**:
    - **Description:** Retrieves the user's profile information.
    - **Logic:**
        - It fetches the user's `nombre` (name) and `email` from the `users` collection.

- **`GET /user-service/perfil/notificaciones`**:
    - **Description:** Retrieves the user's notification settings.
    - **Logic:**
        - It fetches the user's notification preferences from the `users` collection.

- **`PUT /user-service/perfil/notificaciones`**:
    - **Description:** Updates the user's notification settings.
    - **Request Body:** `NotificacionesPutRequest` object with the new notification settings.
    - **Logic:**
        - It updates the user's notification preferences in the `users` collection.

- **`POST /user-service/perfil/password`**:
    - **Description:** Changes the user's password.
    - **Request Body:** `CambioPasswordRequest` object with the old and new passwords.
    - **Logic:**
        - It validates the old password and then updates it with the new one in the `users` collection.

- **`POST /user-service/perfil/borrar`**:
    - **Description:** Deletes the user's account.
    - **Request Body:** `BorrarUsuarioRequest` object with the user's password for confirmation.
    - **Logic:**
        - It validates the password and then deletes the user's account from the `users` collection.

- **`POST /user-service/perfil/foto`**:
    - **Description:** Adds or updates the user's profile picture.
    - **Request Body:** `GuardarFotoRequest` object with the base64-encoded image.
    - **Logic:**
        - It saves the profile picture to the `users` collection and updates the user's achievements.

- **`GET /user-service/perfil/foto`**:
    - **Description:** Retrieves the user's profile picture.
    - **Logic:**
        - It fetches the base64-encoded profile picture from the `users` collection.

## 3. Exercises and Lessons (`/ejercicio-service`)

This service provides the core learning content.

- **`GET /ejercicio-service/diario`**:
    - **Description:** Fetches the next daily exercise for the user.
    - **Logic:**
        - It retrieves the next available exercise from the user's `ejerciciosNuevos` list.

- **`GET /ejercicio-service/repaso`**:
    - **Description:** Fetches a review exercise for the user.
    - **Logic:**
        - It retrieves an exercise from the user's `ejerciciosRepasoPendiente` list.

- **`POST /ejercicio-service/diario`**:
    - **Description:** Marks a daily exercise as completed.
    - **Request Body:** `EjercicioRequest` object with the exercise `identificador`.
    - **Logic:**
        - It removes the exercise from the user's `ejerciciosNuevos` list and creates a new `EjercicioRealizado` record.

- **`POST /ejercicio-service/repaso`**:
    - **Description:** Marks a review exercise as completed.
    - **Request Body:** `EjercicioRequest` object with the exercise `identificador` and a `success` flag.
    - **Logic:**
        - If `success` is true, it removes the exercise from the user's `ejerciciosRepasoPendiente` list and updates the exercise's `stageEjercicio` and `fechaComienzoRepaso`.
        - If `success` is false, it decrements the exercise's `stageEjercicio`.

- **`GET /ejercicio-service/diario/cantidad`**:
    - **Description:** Gets the number of remaining daily exercises.
    - **Logic:**
        - It returns the size of the user's `ejerciciosNuevos` list.

## 4. Dictionary (`/diccionario-service`)

This service provides the dictionary functionality.

- **`GET /diccionario-service`**:
    - **Description:** Fetches a list of words from the dictionary.
    - **Query Parameters:** `pagina` (page number), `size` (page size), `busqueda` (search term), `favoritos` (boolean), `categorias` (list of categories).
    - **Logic:**
        - It retrieves a paginated list of exercises from the `ejercicio` collection, with optional filtering by search term, favorites, and categories.

- **`GET /diccionario-service/{id}`**:
    - **Description:** Fetches a specific word from the dictionary.
    - **Path Parameter:** `id` of the exercise.
    - **Logic:**
        - It retrieves the details of a specific exercise from the `ejercicio` collection.

- **`POST /diccionario-service/favorito/{identificador}`**:
    - **Description:** Adds a word to the user's favorites.
    - **Path Parameter:** `identificador` of the exercise.
    - **Logic:**
        - It adds the exercise to the user's `user_favorites` collection.

- **`DELETE /diccionario-service/favorito/{identificador}`**:
    - **Description:** Removes a word from the user's favorites.
    - **Path Parameter:** `identificador` of the exercise.
    - **Logic:**
        - It removes the exercise from the user's `user_favorites` collection.

## 5. Statistics and Progress (`/estadisticas-service`)

This service provides user statistics and progress information.

- **`GET /estadisticas-service/progreso`**:
    - **Description:** Retrieves the user's progress statistics.
    - **Logic:**
        - It calculates the user's progress based on the last completed exercise's `srsLevel` and `srsLevelOrder`.

## 6. Achievements (`/logros-service`)

This service manages user achievements.

- **`GET /logros-service/logros`**:
    - **Description:** Retrieves the user's achievements.
    - **Logic:**
        - It fetches the user's achievements from the `achievements_users` collection and checks for new achievements based on the user's completed exercises.
