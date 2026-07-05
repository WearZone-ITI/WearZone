# AiChat → GroqCloud Migration Implementation Plan

## Background

The WearZone AiChat feature currently calls **Google's Gemini REST API** (via a Retrofit `GeminiApiService`) with Room DB persistence for chat history. This migration replaces the entire backend with **GroqCloud's OpenAI-compatible REST API**, removes Room persistence, and moves to a fully in-memory `StateFlow`-based architecture inside `ChatViewModel`.

---

## User Review Required

> [!IMPORTANT]
> **Groq API Key** — The key `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` will be written to `local.properties` and exposed via `BuildConfig.GROQ_API_KEY`. It is a dev key scoped to Groq and is safe to bundle.

> [!WARNING]
> **Room DB changes** — `ChatMessageEntity`, `ChatDao`, and the `chatDao()` accessor will be **deleted**. The `WearZoneDatabase` schema version will bump from **5 → 6** (with `fallbackToDestructiveMigration()` already in place). This wipes chat history on device update — which is intentional per the ephemeral-state requirement.

> [!CAUTION]
> **`converter-gson` dependency** — The project currently uses `kotlinx.serialization` for all other networking. The Groq DTOs will use `Gson`/`@SerializedName` (as you specified "converter-gson"). No `Gson` annotations will touch any domain or existing data model — isolation is guaranteed.

---

## Open Questions

None — requirements are fully specified.

---

## Proposed Changes

### 1. Build & Configuration

#### [MODIFY] [app/build.gradle.kts](file:///d:/projects/WearZone/app/build.gradle.kts)

- Remove `GEMINI_API_KEY` `buildConfigField`.
- Add `GROQ_API_KEY` `buildConfigField` reading from `local.properties`.
- Verify `retrofit` and `retrofit.converter.gson` are present (they already are at lines 138–139).
- Remove `com.google.genai` SDK if present (confirmed: it is NOT listed — no action needed).

#### [MODIFY] [local.properties](file:///d:/projects/WearZone/local.properties)

- Add `GROQ_API_KEY=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`

---

### 2. Data Layer — DTOs

#### [DELETE] [GeminiDto.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/ai/chat/dto/GeminiDto.kt)

All Gemini-specific DTOs removed.

#### [NEW] `data/.../remote/ai/chat/dto/GroqDto.kt`

Standard OpenAI-compatible DTOs using `@SerializedName` Gson annotations:

```kotlin
// Request
data class GroqChatRequest(
    @SerializedName("model") val model: String = "llama-3.3-70b-versatile",
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("tools") val tools: List<GroqTool>? = null,
    @SerializedName("tool_choice") val toolChoice: String? = null
)

data class GroqMessage(
    @SerializedName("role") val role: String,          // "system" | "user" | "assistant" | "tool"
    @SerializedName("content") val content: String?,
    @SerializedName("tool_calls") val toolCalls: List<GroqToolCall>? = null,
    @SerializedName("tool_call_id") val toolCallId: String? = null,
    @SerializedName("name") val name: String? = null   // for "tool" role
)

data class GroqToolCall(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,          // always "function"
    @SerializedName("function") val function: GroqFunctionCall
)

data class GroqFunctionCall(
    @SerializedName("name") val name: String,
    @SerializedName("arguments") val arguments: String  // raw JSON string
)

// Tool definitions
data class GroqTool(
    @SerializedName("type") val type: String = "function",
    @SerializedName("function") val function: GroqFunctionDef
)

data class GroqFunctionDef(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("parameters") val parameters: GroqFunctionParams
)

data class GroqFunctionParams(
    @SerializedName("type") val type: String = "object",
    @SerializedName("properties") val properties: Map<String, GroqParamProperty>,
    @SerializedName("required") val required: List<String>
)

data class GroqParamProperty(
    @SerializedName("type") val type: String,
    @SerializedName("description") val description: String
)

// Response
data class GroqChatResponse(
    @SerializedName("choices") val choices: List<GroqChoice>? = null,
    @SerializedName("error") val error: GroqError? = null
)

data class GroqChoice(
    @SerializedName("message") val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class GroqError(
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("code") val code: String? = null
)
```

---

### 3. Data Layer — API Service

#### [DELETE] [GeminiApiService.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/ai/chat/api/GeminiApiService.kt)

#### [NEW] `data/.../remote/ai/chat/api/GroqApiService.kt`

```kotlin
interface GroqApiService {
    @POST("chat/completions")
    suspend fun chatCompletions(
        @Body request: GroqChatRequest
    ): retrofit2.Response<GroqChatResponse>
}
```

Using `retrofit2.Response<T>` (not raw `T`) so we can inspect HTTP status codes for human-friendly error mapping.

---

### 4. Data Layer — Remote Data Source

#### [MODIFY] [IAiChatRemoteDataSource.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/ai/chat/IAiChatRemoteDataSource.kt)

Signature stays exactly the same:
```kotlin
interface IAiChatRemoteDataSource {
    suspend fun sendMessage(message: String, history: List<ChatMessage>): String
}
```

#### [MODIFY] [AiChatRemoteDataSourceImpl.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/ai/chat/AiChatRemoteDataSourceImpl.kt)

Complete replacement (Gemini → Groq):

**Key changes:**
- Remove `@GeminiApiKey` / `GeminiApiService` → inject `@GroqApiKey String` + `GroqApiService`
- `init` block logs: `Log.d("GROQ_CHAT_DEBUG", "Groq API Key length: ${apiKey.length}")`
- Build two tool definitions (`searchProducts`, `getProductDetail`) using `GroqTool` DTOs
- `SYSTEM_PROMPT` constant: the strict WearZone AI Stylist persona
- `sendMessage()`:
  1. Build history list: trim to last 10 non-system messages (keeping system at index 0)
  2. Log payload JSON string before network call
  3. Call `groqApiService.chatCompletions(request)` → `Response<GroqChatResponse>`
  4. Log response code + headers + raw body string
  5. If `response.isSuccessful` → parse tool calls or plain text
  6. Else → map HTTP code to friendly message (429, 401, 403, 500, timeout, offline)
- Tool call dispatch (`searchProducts`, `getProductDetail`) follows same pattern as before
- All exceptions → `Log.e("GROQ_CHAT_DEBUG", "...", e)` + friendly fallback string

**Token trimming rule:**
```kotlin
// keep system prompt (index 0) + last 10 conversation turns
val trimmedHistory = if (historyMessages.size > 10) {
    Log.d("GROQ_CHAT_DEBUG", "Trimming history from ${historyMessages.size} to 10 entries")
    historyMessages.takeLast(10)
} else historyMessages
val payload = listOf(systemMessage) + trimmedHistory + listOf(userMessage)
```

**Error mapping (inside data source, not VM):**
```kotlin
return when (response.code()) {
    429 -> "Whoops, you're browsing style advice faster than our servers can process! Please wait a few seconds and try again."
    401, 403 -> "Stylist connection error. There's a credential mismatch behind the scenes."
    else -> "Unable to reach the fashion assistant right now. Please check your internet connection."
}
```
These strings flow back to the repository → VM → `_uiState.update { it.copy(error = ...) }` — they are **never** appended to the `_messages` history flow that feeds the API payload.

---

### 5. Data Layer — Room Removal (Chat-specific)

#### [DELETE] [ChatDao.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/local/ai/chat/ChatDao.kt)

#### [DELETE] [ChatMessageEntity.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/local/ai/chat/ChatMessageEntity.kt)

#### [MODIFY] [WearZoneDatabase.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/db/WearZoneDatabase.kt)

- Remove `ChatMessageEntity` from `entities = [...]`
- Remove `abstract fun chatDao(): ChatDao`
- Bump version: `5 → 6`

---

### 6. Data Layer — Repository Replacement

#### [MODIFY] [AiChatRepositoryImpl.kt](file:///d:/projects/WearZone/data/src/main/kotlin/com/example/wearzone/data/repository/AiChatRepositoryImpl.kt)

Complete rewrite — remove all `ChatDao` usage. New implementation:

```kotlin
class AiChatRepositoryImpl @Inject constructor(
    private val remoteDataSource: IAiChatRemoteDataSource
) : IAiChatRepository {

    // In-memory history — ephemeral, lives until process death
    private val _history = MutableStateFlow<List<ChatMessage>>(emptyList())

    override fun getChatHistory(): Flow<List<ChatMessage>> = _history.asStateFlow()

    override suspend fun clearHistory() {
        Log.d("GROQ_CHAT_DEBUG", "Clearing in-memory chat history")
        _history.value = emptyList()
    }

    override suspend fun sendMessage(message: String): DataResult<Unit> {
        return try {
            val userMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = ChatRole.USER,
                content = message,
                timestamp = System.currentTimeMillis()
            )
            val pendingId = UUID.randomUUID().toString()
            val pendingMsg = ChatMessage(
                id = pendingId, role = ChatRole.MODEL,
                content = "...", timestamp = System.currentTimeMillis(), isPending = true
            )
            _history.update { it + userMsg + pendingMsg }

            val currentHistory = _history.value.filter { !it.isPending }
            val responseText = remoteDataSource.sendMessage(message, currentHistory)

            _history.update { history ->
                history.map { if (it.id == pendingId) it.copy(content = responseText, isPending = false) else it }
            }
            DataResult.Success(Unit)
        } catch (e: Exception) {
            Log.e("GROQ_CHAT_DEBUG", "Repository error sending message", e)
            _history.update { it.filter { msg -> !msg.isPending } }
            DataResult.Error(DomainError.Unknown(e))
        }
    }
}
```

---

### 7. Domain Layer — Unchanged

The domain contracts (`IAiChatRepository`, `ChatMessage`, `ChatRole`, `SendChatMessageUseCase`, `GetChatHistoryUseCase`, `ClearChatHistoryUseCase`) remain **100% unchanged**. Zero domain modifications.

---

### 8. DI Layer

#### [MODIFY] [NetworkModule.kt](file:///d:/projects/WearZone/app/src/main/kotlin/com/example/wearzone/di/NetworkModule.kt)

- Remove `provideGeminiApiService()` function
- Add `provideGroqApiService()`:
  ```kotlin
  @Provides
  fun provideGroqApiService(): GroqApiService {
      val loggingInterceptor = HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
      }
      val okHttpClient = OkHttpClient.Builder()
          .addInterceptor(loggingInterceptor)
          .addInterceptor { chain ->
              val req = chain.request().newBuilder()
                  .addHeader("Authorization", "Bearer ${BuildConfig.GROQ_API_KEY}")
                  .addHeader("Content-Type", "application/json")
                  .build()
              chain.proceed(req)
          }
          .connectTimeout(30, TimeUnit.SECONDS)
          .readTimeout(60, TimeUnit.SECONDS)
          .build()
      return Retrofit.Builder()
          .baseUrl("https://api.groq.com/openai/v1/")
          .client(okHttpClient)
          .addConverterFactory(GsonConverterFactory.create())
          .build()
          .create(GroqApiService::class.java)
  }
  ```

#### [MODIFY] [DataSourceModule.kt](file:///d:/projects/WearZone/app/src/main/kotlin/com/example/wearzone/di/DataSourceModule.kt)

- Remove `@GeminiApiKey` qualifier annotation declaration + provider
- Add `@GroqApiKey` qualifier annotation + provider:
  ```kotlin
  @Qualifier @Retention(AnnotationRetention.BINARY)
  annotation class GroqApiKey

  @Provides @GroqApiKey
  fun provideGroqApiKey(): String = BuildConfig.GROQ_API_KEY
  ```

#### [MODIFY] [DatabaseModule.kt](file:///d:/projects/WearZone/app/src/main/kotlin/com/example/wearzone/di/DatabaseModule.kt)

- Remove `provideChatDao()` function entirely.

#### [MODIFY] [RepositoryModule.kt](file:///d:/projects/WearZone/app/src/main/kotlin/com/example/wearzone/di/RepositoryModule.kt)

- The `bindAiChatRepository` binding remains unchanged (interface binding still maps `AiChatRepositoryImpl → IAiChatRepository`).

---

### 9. Presentation Layer — ChatViewModel

#### [MODIFY] [ChatViewModel.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/ai/chat/ChatViewModel.kt)

Key changes:
- **Remove** `clearChatHistoryUseCase` dependency for now, keep the clear intent wired to `sendClearHistory` via repository's in-memory clear.
- The VM currently `observeChatHistory()` via `getChatHistoryUseCase().collectLatest` — this works unchanged since the repository now exposes a `StateFlow` from `_history`.
- **Error display guardrail**: errors from `DataResult.Error` land in `_uiState.update { it.copy(error = friendlyMessage) }`, which drives a visible `Text` in the UI — they are **never** routed back into `sendChatMessageUseCase()` as chat content.
- The `isSending = true` already disables input — no changes needed to locking logic.

---

### 10. Presentation Layer — ChatScreen

#### [MODIFY] [ChatScreen.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/ai/chat/ChatScreen.kt)

**New composable: `ProductCardInChat`**

Parse AI response text for a product reference (detect pattern like `Product ID: <digits>` or `ID: <digits>`). If detected, render a Material 3 `Card` below the message text with:
- `AsyncImage` placeholder (Coil 3)
- Product title extracted from surrounding text
- Formatted price if available
- Clickable: `onClick = { navController.navigate("product_detail/$productId") }`

The `AiMessageBubble` composable will call `ProductCardInChat` when IDs are detected.

**Error Banner**

When `uiState.error != null`, show a dismissible `Banner` / `Snackbar` at the bottom with the friendly message. On dismiss: `onIntent(ChatUiIntent.OnDismissError)`.

**ChatScreen signature change** — needs `navController: NavController` parameter to enable card click routing.

#### [MODIFY] [ChatUiState.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/ai/chat/ChatUiState.kt)

No structural changes needed — `error: String?` field already exists.

---

## File Change Summary Table

| File | Action | Module |
|------|--------|--------|
| `local.properties` | ADD `GROQ_API_KEY` | root |
| `app/build.gradle.kts` | MODIFY `buildConfigField` (GEMINI→GROQ) | app |
| `GeminiDto.kt` | **DELETE** | data |
| `GroqDto.kt` | **NEW** | data |
| `GeminiApiService.kt` | **DELETE** | data |
| `GroqApiService.kt` | **NEW** | data |
| `AiChatRemoteDataSourceImpl.kt` | **REWRITE** | data |
| `ChatDao.kt` | **DELETE** | data |
| `ChatMessageEntity.kt` | **DELETE** | data |
| `WearZoneDatabase.kt` | MODIFY (remove chat, bump version) | data |
| `AiChatRepositoryImpl.kt` | **REWRITE** (remove Room) | data |
| `NetworkModule.kt` | MODIFY (Gemini→Groq provider) | app/di |
| `DataSourceModule.kt` | MODIFY (qualifier swap) | app/di |
| `DatabaseModule.kt` | MODIFY (remove ChatDao provider) | app/di |
| `ChatViewModel.kt` | MINOR MODIFY (error guardrail comments) | presentation |
| `ChatScreen.kt` | MODIFY (ProductCard + error banner + navController) | presentation |

---

## Verification Plan

### Automated
```bash
./gradlew clean assembleDebug
```
Must produce `BUILD SUCCESSFUL` with zero errors.

### Manual (Logcat filter: `GROQ_CHAT_DEBUG`)
1. Launch app → navigate to AiChat.
2. Confirm `Groq API Key length: 56` log on init.
3. Type "Show me shirts" → Confirm outgoing JSON payload logged.
4. Confirm response code 200 + body logged.
5. Confirm `searchProducts` tool call is dispatched and logged.
6. Confirm product cards render with clickable navigation.
7. Kill app → reopen → confirm chat history is wiped (ephemeral).
8. Simulate rate-limit error → confirm friendly `429` message appears in UI, **not** in next API call's message history.
