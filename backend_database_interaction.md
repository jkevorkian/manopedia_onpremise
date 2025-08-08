# Backend-Database Interaction Documentation

This document outlines the interactions between the backend service and the MongoDB database container.

## Database Collections

The backend interacts with the following MongoDB collections:

- **`users`**: Stores user information, including credentials, profile details, and notification settings.
- **`onboarding`**: Temporarily stores user information during the registration process.
- **`validacion_mail`**: Stores PINs for email validation during password changes.
- **`ejercicio`**: Stores the exercises, which are the core learning content.
- **`ejercicio_realizado`**: Stores the exercises that users have completed.
- **`user_favorites`**: Stores the exercises that users have marked as favorites.
- **`achievements_users`**: Stores the achievements that users have unlocked.
- **`historial_ingresos`**: Stores the login history of users.
- **`categorias`**: Stores the categories for the exercises.

## Repository Layer

The backend uses the Spring Data MongoDB framework to interact with the database. The following repositories are defined:

- **`UsuariosRepository`**: Performs CRUD operations on the `users` collection.
- **`OnboardingRepository`**: Performs CRUD operations on the `onboarding` collection.
- **`ValidacionMailRepository`**: Performs CRUD operations on the `validacion_mail` collection.
- **`EjercicioRepository`**: Performs CRUD operations on the `ejercicio` collection.
- **`EjercicioRealizadoRepository`**: Performs CRUD operations on the `ejercicio_realizado` collection.
- **`FavoritosUsuarioRepository`**: Performs CRUD operations on the `user_favorites` collection.
- **`LogrosRepository`**: Performs CRUD operations on the `achievements_users` collection.
- **`HistorialIngresosRepository`**: Performs CRUD operations on the `historial_ingresos` collection.
- **`CategoriaRepository`**: Performs CRUD operations on the `categorias` collection.

## Data Flow

### User Registration

1.  When a user registers, a new document is created in the `onboarding` collection with a hashed PIN.
2.  After the user validates their email with the PIN, a new document is created in the `users` collection, and the corresponding document in the `onboarding` collection is no longer needed.

### User Login

1.  When a user logs in, the backend queries the `users` collection to validate the user's credentials.
2.  Upon successful login, a new document is created in the `historial_ingresos` collection to record the login event.

### Exercises

1.  When a new user is created, the backend fetches the initial set of exercises from the `ejercicio` collection and adds them to the user's document in the `users` collection.
2.  When a user completes an exercise, a new document is created in the `ejercicio_realizado` collection, and the exercise is removed from the user's list of new exercises.
3.  When a user requests a review exercise, the backend queries the `ejercicio_realizado` collection to find an exercise that is due for review.

### Dictionary

1.  The dictionary is built from the `ejercicio` collection. When the user requests the dictionary, the backend queries the `ejercicio` collection and returns a list of all the exercises.
2.  When a user marks an exercise as a favorite, a new document is created in the `user_favorites` collection.

### Achievements

1.  When a user completes certain actions (e.g., completes 10 exercises), the backend checks if the user has unlocked any achievements.
2.  If an achievement is unlocked, a new document is created in the `achievements_users` collection.
