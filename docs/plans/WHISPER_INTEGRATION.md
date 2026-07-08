# WearZone: AI Smart Chat Voice-to-Text Integration

## 📌 Overview
To reduce user friction and elevate the shopping experience, WearZone features an advanced AI Smart Chat. Typing complex fashion queries (e.g., *"Find me a red summer dress under 500 EGP"*) on a mobile keyboard can be tedious. To solve this, we integrated **Groq's Whisper-large-v3** model, allowing users to hold a microphone button, speak naturally in English or Arabic, and have their voice instantly transcribed into the chat.

## 🛠️ Tech Stack & Architecture
* **API:** Groq Cloud (`whisper-large-v3`)
* **Hardware:** Android Microphone (`MediaRecorder`, `.m4a` AAC encoding)
* **Network:** Retrofit2 with `MultipartBody` requests
* **UI:** Jetpack Compose with custom pointer gesture detection
* **Architecture:** Clean Architecture + MVVM + MVI State Management

---

## 🏗️ Implementation Phases

### Phase 1: Hardware & Permissions (Utility Layer)
* Declared `<uses-permission android:name="android.permission.RECORD_AUDIO" />` in the Android Manifest.
* Created `VoiceRecorderHelper.kt` to manage the `MediaRecorder` lifecycle.
* Handled Android 12 (API 31+) deprecations.
* Audio is captured via the device microphone, encoded as AAC, and saved as a temporary `audio_message.m4a` file in the app's secure cache directory.

### Phase 2: Network Layer (Retrofit)
* Appended the `/audio/transcriptions` endpoint to `GroqApiService`.
* Implemented a `Multipart` request. Unlike standard JSON REST APIs, audio processing requires `MultipartBody.Part` for the binary file and a `RequestBody` explicitly formatted as `text/plain` for the model definition.

### Phase 3: Domain & Data Layers
* **Repository (`AiChatRemoteDataSourceImpl` & `AiChatRepositoryImpl`):**
  Converts the local `.m4a` `File` into OkHttp's `RequestBody` and executes the network call securely using the injected API Key. Returns a `Result<String>`.
* **Use Case (`TranscribeVoiceUseCase`):**
  Provides a clean boundary between the UI and the data layer, injected via Dagger/Hilt.

### Phase 4: Presentation & UI (Jetpack Compose)
* **Hold-to-Talk Logic:** Used `Modifier.pointerInput(Unit)` and `detectTapGestures` to implement a natural "Press and Hold" recording gesture.
* **State Management (`ChatViewModel`):** Handles the transcription loading state. Intelligently appends the transcribed text to the `OutlinedTextField` with proper spacing, preventing text overlap.
* **Dynamic UI:** The microphone icon transitions to an error color (`Red`) while actively recording to provide immediate visual feedback to the user.

---

## 🐛 Challenges & Solutions (Engineering Highlights)

During development, we encountered and resolved several complex edge cases:

### 1. The "Hold-to-Talk" Permission Lifecycle Bug
**Problem:** Triggering a permission request dialog *during* an active `onPress` gesture causes the Compose gesture detector to register a "release" event (because the system dialog steals window focus). This resulted in ghost recordings and stuck UI states.
**Solution:** Implemented a pre-flight check using `ContextCompat.checkSelfPermission`. If the permission is missing, the `onPress` event *only* launches the permission request. The user must grant it and then press the button a second time to actually begin the `MediaRecorder` lifecycle.

### 2. Retrofit Multipart 401 Unauthorized Error
**Problem:** The Groq API returned `401 Unauthorized` despite a valid API key.
**Solution:** Identified that Cloudflare API gateways occasionally drop headers if the multipart boundary is malformed. Refactored the `model` parameter from a generic string to an explicit `RequestBody` using `"text/plain".toMediaTypeOrNull()`. This ensured OkHttp formatted the boundaries perfectly, allowing the Authorization header to pass through securely.

### 3. Smart Text Appending
**Problem:** Transcribed text mashed into existing typed text (e.g., typing "I want" and saying "red shoes" resulted in "I wantred shoes").
**Solution:** Added state-aware formatting in the ViewModel that evaluates if `inputText.isBlank()`. If false, it injects a space `"${state.inputText} $transcribedText"` before appending.

---

## 🚀 Future Improvements
* **Audio Streaming:** Transition from file-based REST uploads to WebSocket streaming for real-time, word-by-word transcription as the user speaks.
* **Auto-Send Toggle:** Provide a user setting to bypass the text-field review and immediately send the transcribed message to the LLM upon releasing the microphone.