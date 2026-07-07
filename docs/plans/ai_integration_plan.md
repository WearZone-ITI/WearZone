# WearZone — AI Shopping Assistant (AiChat) Implementation Plan

## Overview

Build a full-screen conversational AI Shopping Assistant that orchestrates **live Shopify Storefront data** to answer natural-language user questions. The AI will use **Google Gemini 1.5 Pro** via the official Google AI Client SDK, and perform **function calling** against the existing Shopify REST endpoints that the data layer already knows how to call. Conversation history will be persisted in **Room**.

**The chatbot can answer:**
- "Find me a black jacket under $80" → calls `getProducts(query=black jacket, maxPrice=80)`
- "What's trending right now?" → calls `getProducts(sortKey=BEST_SELLING)`
- "Show me Nike products" → calls `getProductsByVendor(vendor=Nike)`
- "More like this product" → calls `getProductDetail(id=...)` + `productRecommendations`
- "What did I order before?" → calls `GetCustomerOrders` (auth-gated)
- "Tell me about [product name]" → calls `searchProducts(query=...)`
- "Browse [category name] items" → calls `getProducts(collectionId=...)`

---

## Key Design Decisions

> [!IMPORTANT]
> **AI Backend Choice: Google Gemini via Official SDK**
> We use `com.google.ai.client.generativeai:generativeai` with the `gemini-1.5-pro` model for premium reasoning. The API key is securely provided via `BuildConfig.GEMINI_API_KEY` (backed by `local.properties`).

> [!IMPORTANT]
> **Tool Calling Strategy: ViewModel-Orchestrated (no BFF needed)**
> The LLM sends back a structured `FunctionCall` object with tool name + arguments. The `AiChatRepository` parses this and calls the appropriate **existing use case** (`SearchProductsUseCase`, `GetProductsUseCase`, `GetProductsByVendorUseCase`, `GetProductDetailUseCase`) — all already wired in `UseCaseModule.kt`. The result is fed back to Gemini for a final natural-language answer. This avoids any backend/BFF requirement.

> [!IMPORTANT]
> **Security: Storefront token only**
> The AI feature calls the same `IProductRepository` / `IProductRemoteDataSource` that the rest of the app uses. It **never** calls Admin REST endpoints directly. All Admin REST Postman entries are off-limits per `AGENTS.md §0`.

> [!IMPORTANT]
> **Chat History Persistence (Room):**
> Instead of keeping messages strictly in memory, we persist them locally so they survive app restarts. We add `ChatMessageEntity` to Room and `ChatDao`. `AiChatRepositoryImpl` treats Room as the Single Source of Truth. The `ChatViewModel` collects from the database flow.

---

## Proposed Changes

### Component 1 — `data/local` module (Room Persistence)

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/local/ai/chat/ChatMessageEntity.kt`
```kotlin
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val role: String,
    val content: String,
    val timestamp: Long,
    val suggestedProductIdsJson: String // Serialized list of product IDs
)
```

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/local/ai/chat/ChatDao.kt`
```kotlin
@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistoryFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}
```

#### [MODIFY] `WearZoneDatabase.kt`
Add `ChatMessageEntity` to `@Database` and `abstract fun chatDao(): ChatDao`.

---

### Component 2 — `domain` module

#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/ai/chat/model/ChatMessage.kt`
```kotlin
data class ChatMessage(
    val id: String,
    val role: ChatRole,
    val content: String,
    val suggestedProducts: ImmutableList<Product> = persistentListOf(),
    val timestamp: Long = System.currentTimeMillis(),
)
enum class ChatRole { USER, ASSISTANT, SYSTEM, MODEL }
```

#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/ai/chat/model/ChatTool.kt`
```kotlin
enum class ChatTool {
    SEARCH_PRODUCTS,
    GET_BEST_SELLERS,
    GET_PRODUCTS_BY_VENDOR,
    GET_PRODUCT_DETAIL,
    GET_ORDER_HISTORY,
}
```

#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/ai/chat/repository/IAiChatRepository.kt`
```kotlin
interface IAiChatRepository {
    fun getChatHistoryFlow(): Flow<List<ChatMessage>>
    suspend fun sendMessage(userMessage: String, customerAccessToken: String?): DataResult<Unit>
    suspend fun clearHistory()
}
```

#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/ai/chat/usecase/SendChatMessageUseCase.kt`
#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/ai/chat/usecase/ClearChatHistoryUseCase.kt`
#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/ai/chat/usecase/GetChatHistoryUseCase.kt`

---

### Component 3 — `data` module (Remote & Repository)

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/remote/datasource/IAiChatRemoteDataSource.kt`
```kotlin
interface IAiChatRemoteDataSource {
    suspend fun sendMessage(
        userMessage: String,
        history: List<ChatMessage>,
        customerAccessToken: String?,
    ): ChatMessage
}
```

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/remote/datasource/AiChatRemoteDataSourceImpl.kt`
This is the core orchestration engine. Initializes `GenerativeModel` (with `gemini-1.5-pro` and `BuildConfig.GEMINI_API_KEY`) with the 5 available tools as function declarations. Handles function calling loops.

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/repository/AiChatRepositoryImpl.kt`
Implements `IAiChatRepository`. Exposes `ChatDao.getChatHistoryFlow()`. `sendMessage` inserts user message into Room, calls `AiChatRemoteDataSource`, and then inserts the model response.

---

### Component 4 — `presentation` module

All files in `presentation/src/main/kotlin/com/example/wearzone/presentation/ai/chat/`

#### [NEW] `ChatUiState.kt`
```kotlin
data class ChatUiState(
    val messages: ImmutableList<ChatMessage> = persistentListOf(),
    val isLoading: Boolean = false,
    val inputText: String = "",
)
```

#### [NEW] `ChatUiIntent.kt`
#### [NEW] `ChatUiEffect.kt`
#### [NEW] `ChatViewModel.kt`
- `@HiltViewModel`
- Collects `getChatHistoryUseCase()` Flow and updates `ChatUiState.messages`
- `handleIntent` for `OnSendClicked` calls `SendChatMessageUseCase`.

#### [NEW] `ChatScreen.kt` & Components
Full-screen chat UI with VogueVibe styling.
Includes `UserMessageBubble`, `AssistantMessageBubble`, `ProductSuggestionsRow`, `TypingIndicator`, `ChatProductCard`.

---

### Component 5 — `app` module (DI + Navigation)

#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/di/DataSourceModule.kt`
#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/di/RepositoryModule.kt`
#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/di/UseCaseModule.kt`
#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/di/DatabaseModule.kt` (Add ChatDao)
#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/navigation/Route.kt`
#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/navigation/NavGraph.kt`
#### [MODIFY] `presentation/.../home/HomeScreen.kt` (Add FAB)

---

## AI Tool → Shopify Endpoint Mapping

| AI Tool (Gemini Function) | Existing Use Case Called | Shopify Call Under the Hood |
|---|---|---|
| `search_products` | `SearchProductsUseCase` | Admin REST `GET /products.json?title=...` |
| `get_best_sellers` | `GetProductsUseCase` | Admin REST `GET /products.json` (client-sorted) |
| `get_products_by_vendor` | `GetProductsByVendorUseCase` | Admin REST `GET /products.json?vendor=...` |
| `get_product_detail` | `GetProductDetailUseCase` | Admin REST `GET /products/{id}.json` |
| `get_order_history` | `GetOrderHistoryUseCase` | Admin REST `GET /orders.json?customer_id=...` |
