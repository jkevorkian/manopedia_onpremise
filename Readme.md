# Real-time Translation Application (Manopedia On-Premise Minimal)

This project aims to implement the real-time translation component of the Manopedia application, focusing on on-device processing of the LSTM network and sliding window approach. This document outlines the functional architecture of the application.

## Functional Diagram: Real-time Translation Application

### I. Core Modules & Their Responsibilities:

1.  **Input Module (e.g., `CameraHandler`, `AudioHandler`):**
    *   **Responsibility:** Captures raw input data from the device's sensors.
    *   **Inputs:** Device camera stream (video frames), microphone audio stream (audio samples).
    *   **Outputs:** Raw video frames (e.g., `Bitmap`, `ImageBuffer`), raw audio samples (e.g., `byte[]`, `short[]`).
    *   **Key Components/Logic:**
        *   Manages camera/microphone access and permissions.
        *   Configures input stream parameters (resolution, frame rate, sample rate).
        *   Provides a continuous stream of raw input data.

2.  **Preprocessing Module (e.g., `DataPreprocessor`):**
    *   **Responsibility:** Transforms raw input into a format suitable for the LSTM model, including landmark extraction (for video) and the sliding window mechanism.
    *   **Inputs:** Raw video frames, raw audio samples.
    *   **Outputs:** Batched, normalized feature vectors (e.g., `float[][]` or `ByteBuffer`) ready for LSTM inference.
    *   **Key Components/Logic:**
        *   **`LandmarkExtractor` (for video):** Detects and extracts key points (e.g., hand landmarks using a pre-trained model like MediaPipe, or a custom solution).
        *   **`FeatureNormalizer`:** Normalizes extracted landmark coordinates or audio features to a consistent scale.
        *   **`SlidingWindowBuffer`:**
            *   Maintains a circular buffer of recent feature vectors.
            *   As new features arrive, it adds them to the buffer and removes the oldest ones.
            *   When the buffer contains a full "window" of data (e.g., 30 frames for a 1-second window at 30 FPS), it forms a complete input sequence for the LSTM.
        *   **`InputFormatter`:** Converts the processed data from the sliding window into the exact tensor shape and data type expected by the TFLite model.

3.  **LSTM Inference Module (e.g., `TFLiteModelHandler`):**
    *   **Responsibility:** Loads the TensorFlow Lite model (`handsigns_model.tflite`) and performs real-time inference on the preprocessed data.
    *   **Inputs:** Preprocessed feature vectors (input tensor).
    *   **Outputs:** Raw model output (e.g., probability distribution over classes, or an encoded sequence of predictions).
    *   **Key Components/Logic:**
        *   **`ModelLoader`:** Loads the `.tflite` model file into memory.
        *   **`TFLiteInterpreter`:** Manages the TensorFlow Lite interpreter instance.
        *   Handles input/output buffer allocation and management for efficient inference.
        *   Executes the model inference when a new input sequence is available.

4.  **Post-processing / Translation Module (e.g., `TranslationEngine`):**
    *   **Responsibility:** Interprets the raw model output and converts it into human-readable translated text.
    *   **Inputs:** Raw model output (e.g., probability scores for each possible sign/word).
    *   **Outputs:** Translated text string.
    *   **Key Components/Logic:**
        *   **`OutputDecoder`:** Maps the model's numerical output (e.g., highest probability index) to the corresponding sign or word from a predefined vocabulary.
        *   **`SequenceAggregator`:** If the model provides per-frame or per-window predictions, this component aggregates them over time to form a stable, coherent translation. This might involve:
            *   Simple voting (most frequent prediction in a short time window).
            *   Thresholding (only output if confidence is above a certain level).
            *   More advanced techniques like Connectionist Temporal Classification (CTC) decoding or beam search if the model architecture supports it.
        *   **`Contextualizer` (Optional):** If multiple signs form a phrase, this component might use a simple language model or rule-based system to combine them into grammatically correct sentences.

5.  **User Interface (UI) Module (e.g., `TranslationScreen`, `MainActivity`):**
    *   **Responsibility:** Presents the application to the user, displays the input (e.g., camera feed), and shows the translated output. Also handles user interactions.
    *   **Inputs:** Translated text, status messages (e.g., "Initializing...", "No sign detected").
    *   **Outputs:** User interactions (e.g., "Start/Stop" button presses, settings changes, input source selection).
    *   **Key Components/Logic:**
        *   **`CameraPreview` / `VideoDisplay`:** Renders the live camera feed.
        *   **`TranslationTextView` / `OutputDisplay`:** Displays the real-time translated text.
        *   Interactive elements: Buttons, sliders, settings menus.
        *   Feedback mechanisms: Loading indicators, error messages.

### II. Logic Pathways / Data Flow:

```
+-----------------+       +---------------------+       +---------------------+       +-------------------------+       +-----------------+
|                 |       |                     |       |                     |       |                         |       |                 |
|  Input Module   |------>| Preprocessing Module|------>| LSTM Inference Module |------>| Post-processing /       |------>|   UI Module     |
| (Camera/Mic)    |       | (DataPreprocessor)  |       | (TFLiteModelHandler)|       | Translation Module      |       | (TranslationEngine)     |
|                 |       |                     |       |                     |       |                         |       |                 |
+-----------------+       +---------------------+       +---------------------+       +-------------------------+       +-----------------+
        ^                                                                                                |
        |                                                                                                |
        | (User Start/Stop, Settings)                                                                    | (Feedback/Status Updates)
        +------------------------------------------------------------------------------------------------+
```

**Detailed Data Flow Steps:**

1.  **User Initiates:** User interacts with the UI (e.g., taps "Start Translation").
2.  **Input Capture:** The **UI Module** signals the **Input Module** to start capturing.
3.  **Raw Data Stream:** The **Input Module** continuously captures raw video frames or audio samples and pushes them to the **Preprocessing Module**.
4.  **Data Preparation:** The **Preprocessing Module** receives raw data, performs landmark extraction (if video), normalizes features, and manages the sliding window. Once a complete window of data is formed, it creates an input tensor and sends it to the **LSTM Inference Module**.
5.  **Model Inference:** The **LSTM Inference Module** receives the input tensor, feeds it into the loaded TFLite model, and executes the inference. The raw model output is then passed to the **Post-processing / Translation Module**.
6.  **Translation Logic:** The **Post-processing / Translation Module** decodes the raw model output, aggregates predictions over time, and generates the final translated text string. This text is then sent to the **UI Module**.
7.  **Display Output:** The **UI Module** receives the translated text and updates the display in real-time, showing it to the user. It also continues to display the live camera feed (if applicable) and any status messages.
8.  **Continuous Loop:** Steps 3-7 repeat continuously as long as translation is active, creating a real-time feedback loop.

### III. Key Objects/Classes (Conceptual, language-agnostic):

*   `ApplicationController`: The main orchestrator, managing the lifecycle and interactions between all other modules.
*   `InputSource`: An abstract interface for capturing raw data (e.g., `CameraInputSource`, `MicrophoneInputSource`).
*   `FrameBuffer` / `AudioBuffer`: Manages the sliding window data.
*   `LandmarkDetector`: Handles the detection and extraction of landmarks from images.
*   `FeatureProcessor`: Applies normalization and other transformations to raw features.
*   `TFLiteModel`: Encapsulates the TFLite interpreter and model loading logic.
*   `PredictionDecoder`: Converts model output probabilities into discrete predictions.
*   `TranslationAggregator`: Combines individual predictions into a coherent translation.
*   `UserInterfaceManager`: Manages all UI elements and user interactions.