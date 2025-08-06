# Manopedia Local Deployment

This document provides instructions on how to deploy the Manopedia application locally using Docker and Flutter.

## Prerequisites

- Docker
- Flutter SDK
- An Android device or emulator

## Backend and Database Setup

1.  **Navigate to the project root directory.**
2.  **Build and start the backend and database containers:**

    ```bash
    docker-compose up -d --build
    ```

    This command will build the Docker images for the backend and database and start the containers in detached mode.

## Frontend Setup

1.  **Download the AI models:**

    Navigate to the `frontend-mobile/android/app/src/main/assets/` directory.

    **For Linux/macOS or Git Bash on Windows:**
    Use the following `curl` commands to download the MediaPipe models:

    ```bash
    curl -L -o hand_landmarker.task https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task
    ```
    ```bash
    curl -L -o pose_landmarker_lite.task https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task
    ```

    **For Windows PowerShell:**
    Use the following `Invoke-WebRequest` commands:
    ```powershell
    Invoke-WebRequest -Uri https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task -OutFile hand_landmarker.task
    ```
    ```powershell
    Invoke-WebRequest -Uri https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/float16/1/pose_landmarker_lite.task -OutFile pose_landmarker_lite.task
    ```

2.  **Navigate to the `frontend-mobile` directory:**

    ```bash
    cd frontend-mobile
    ```

3.  **Get the Flutter dependencies:**

    ```bash
    flutter pub get
    ```

4.  **List available devices:**

    To see a list of all connected devices and emulators that can run the application, use the following command:

    ```bash
    flutter devices
    ```

5.  **Run the Flutter application on your connected device:**

    ```bash
    flutter run
    ```

    If you have multiple devices connected, you can specify the device ID from the list in the previous step:

    ```bash
    flutter run -d <device_id>
    ```

## Troubleshooting

-   **Backend connection issues:** If the frontend has trouble connecting to the backend, ensure that the `API_URL` in `frontend-mobile/.env.dev` is set to `http://localhost:8080`.
-   **Docker errors:** If you encounter any Docker-related errors, make sure that Docker is running and that you have the necessary permissions to run Docker commands.
