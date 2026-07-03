# WearZone — AI Feature Analysis & Decision Guide

> **Prepared for:** WearZone Team · JETS Mobile Lab — Android Track  
> **Based on:** `Shopify_Project_Specs.pdf`, REST & GraphQL Postman Collections, `AGENTS.md` Architecture Contract, GitHub repo at `WearZone-ITI/WearZone`  
> **Date:** July 2026

---

## 0. How to Use This Document

Read each feature section fully, check the comparison matrix, then scroll to **Section 7 — Final Recommendation**. The goal is not to pick the "best" AI feature in the abstract, but the one that fits **your exact stack, your available Shopify endpoints, your MVI architecture, and a realistic team timeline**.

---

## 1. Project Context Snapshot

Before evaluating features, here is what the project actually gives you to work with:

### 1.1 Tech Stack (Non-Negotiable)

| Layer | Technology |
|---|---|
| Language | Kotlin — 100% |
| UI | Jetpack Compose (zero XML) |
| Architecture | Clean Architecture + MVI |
| DI | Hilt |
| Async | Kotlin Coroutines + Flow |
| Network | Retrofit 2 + OkHttp or Apollo Kotlin (GraphQL) |
| Auth | Firebase Authentication |
| Local Storage | Room + DataStore (Preferences + Proto) |

### 1.2 Shopify Endpoints Available for AI Features

These are the endpoints from your Postman collections that are **most relevant to AI implementation**:

| Endpoint | Collection | What It Returns |
|---|---|---|
| `productRecommendations(productId)` | GraphQL Storefront | Products Shopify recommends as related |
| `search(query, types: [PRODUCT])` | GraphQL Storefront | Full-text product search results |
| `predictiveSearch(query)` | GraphQL Storefront | Autocomplete suggestions for search |
| `products(sortKey: BEST_SELLING)` | GraphQL Storefront | Store's trending products |
| `products(sortKey: PRICE)` | GraphQL Storefront | Price-sorted product list |
| `GetCustomerOrders` | GraphQL Storefront | Logged-in user's full order history |
| `GetCustomer` profile | GraphQL Storefront | User's name, email, addresses, orders |
| `GET /products.json?vendor=X` | REST Admin | Filter by brand |
| `GET /products.json?product_type=X` | REST Admin | Filter by product type |
| `GET /products/{id}/metafields.json` | REST Admin | Extended product data (reviews, specs) |
| `Filter products by collection` | REST Admin | Products in a category |

### 1.3 Available Product Data Per Item

Each product from the API contains:

- `id`, `title`, `description`, `descriptionHtml`
- `vendor` (brand), `productType`, `tags[]`
- `availableForSale`, `priceRange`
- `images[]` (urls with altText)
- `variants[]` (size, color, stock, price)
- `options[]` (Size, Color, etc.)
- Metafields: reviews, ratings (via Admin REST)

### 1.4 Architecture Layer Map for New AI Feature

Any AI feature added must respect these layer boundaries from `AGENTS.md`:

```
presentation/ai/<feature>/
├── <Feature>Screen.kt          ← Compose UI
├── <Feature>ViewModel.kt       ← MVI: StateFlow + Channel
├── <Feature>UiState.kt         ← sealed interface
├── <Feature>UiIntent.kt        ← sealed interface
├── <Feature>UiEffect.kt        ← sealed interface
└── components/

domain/ai/
├── model/                      ← pure Kotlin data classes
├── repository/                 ← I<Feature>Repository interface
└── usecase/                    ← <Feature>UseCase (single invoke())

data/remote/
├── api/                        ← Retrofit/Apollo service
├── datasource/                 ← I<Feature>RemoteDataSource + Impl
└── dto/                        ← <Feature>Dto
```

---

## 2. AI Feature 1 — AI Shopping Assistant (Smart Chat)

### 2.1 What It Is

A conversational chat interface where the user types natural language requests and the AI responds with product suggestions, styling advice, and direct answers about the store. Examples of what users can ask:

- "Find me a black jacket under $80"
- "What's trending this season?"
- "I need an outfit for a beach party"
- "Compare the Nike hoodie and the Adidas one"
- "Do you have anything similar to what I bought last time?"

### 2.2 How It Leverages Your Endpoints

The AI backend acts as an intelligent orchestrator that reads the user's message and calls Shopify endpoints as **tools**:

| User Query | Shopify Call Made Automatically |
|---|---|
| "Find me red sneakers" | `search(query: "red sneakers")` |
| "What's popular right now?" | `products(sortKey: BEST_SELLING)` |
| "Show me Nike products" | `products(query: "vendor:Nike")` |
| "More like this jacket" | `productRecommendations(productId: $currentId)` |
| "What did I buy before?" | `GetCustomerOrders` (if authenticated) |
| "Show me shoes under $50" | `search(query: "shoes") + client-side filter` |

The LLM receives a system prompt that describes these tools, and it decides which API to call based on the user's message. This is called **function calling** (supported by Claude, OpenAI, Gemini).

### 2.3 Technical Implementation Approach

#### Domain Layer
```kotlin
// domain/ai/chat/model/ChatMessage.kt
data class ChatMessage(
    val id: String,
    val role: ChatRole,          // USER, ASSISTANT
    val content: String,
    val suggestedProducts: ImmutableList<Product> = persistentListOf(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ChatRole { USER, ASSISTANT }

// domain/ai/chat/usecase/SendChatMessageUseCase.kt
class SendChatMessageUseCase @Inject constructor(
    private val aiChatRepository: IAiChatRepository,
    private val searchProductsUseCase: SearchProductsUseCase,      // reuse existing
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) {
    suspend operator fun invoke(
        message: String,
        history: List<ChatMessage>,
        currentProductId: String? = null
    ): Result<ChatMessage>
}
```

#### Data Layer
The data source calls an LLM API (e.g., Anthropic Claude via `/v1/messages`). The LLM is given a system prompt describing the store and the available Shopify tools. When the LLM decides to call a tool, the data source executes the Shopify API call and feeds the result back into the conversation.

```kotlin
// Simplified flow in AiChatRemoteDataSourceImpl.kt
1. POST to LLM API with:
   - systemPrompt (store context + tool definitions)
   - messages history
   - tools: [search_products, get_recommendations, get_best_sellers]

2. If LLM response contains a tool_use block:
   - Execute the corresponding Shopify API call
   - Feed tool_result back to LLM
   - Get final text response

3. Return ChatMessage(role=ASSISTANT, content=aiText, suggestedProducts=foundProducts)
```

#### Presentation Layer
```
ChatScreen — bottom sheet or dedicated screen
├── MessageList (LazyColumn, key = message.id)
├── ProductSuggestionsRow (horizontal scroll of ProductCards)
├── ChatInputField + SendButton
└── LoadingIndicator (animated dots while AI is thinking)
```

#### UiState
```kotlin
data class ChatUiState(
    val messages: ImmutableList<ChatMessage> = persistentListOf(),
    val isLoading: Boolean = false,
    val inputText: String = ""
)
```

### 2.4 Pros

- **Covers 100% of your users** — works for both guests (general questions) and authenticated users (personalized based on order history)
- **Reuses all your existing use cases** — `SearchProductsUseCase`, `GetProductDetailUseCase`, `GetProductRecommendationsUseCase` are all called from inside the AI feature, meaning zero wasted work
- **Native fit with Shopify GraphQL** — `search`, `predictiveSearch`, and `productRecommendations` are exactly the endpoints you already have mapped in the GraphQL collection
- **High UI engagement** — a chat interface is visually impressive and showcases the project in demos
- **Personalization grows over time** — for logged-in users, `GetCustomerOrders` provides context to make suggestions smarter
- **Single integration point** — you only need one external AI API (Claude, OpenAI, Gemini), and you call it from a single `AiChatRemoteDataSource`
- **Clean Architecture fits perfectly** — the chat feature is entirely self-contained in its own domain/data/presentation folders without touching any existing code
- **Accessible to all team members** — UI, domain logic, and AI integration are clearly separated tasks that different people can work on in parallel
- **Works without order history** — unlike Shopping Insights, this is valuable from the first second a user opens the app

### 2.5 Cons

- **Requires an external LLM API key** — you must sign up for Anthropic, OpenAI, or Google AI and manage API costs. For a student project, free tiers (Gemini 1.5 Flash, Claude Haiku) are sufficient but need setup
- **Latency is noticeable** — LLM responses take 1–3 seconds. You must implement a loading state (animated dots/shimmer) to avoid users thinking the app is frozen
- **Response quality varies** — if the user asks about something not in your Shopify catalog, the AI may hallucinate product names. You must validate all product IDs the AI returns against your actual Shopify API
- **Conversation history management** — you need to maintain message history in the ViewModel/DataStore to provide context. Long conversations cost more API tokens
- **Backend concern** — ideally, the LLM API key lives on a backend/BFF server (not in the APK) for the same security reason cited in `AGENTS.md` for the Shopify Admin token. For a student project, a simple BFF endpoint or Firebase Function wrapping the LLM call is enough
- **Internet dependency** — the chat feature is fully non-functional offline (no cached AI responses)

---

## 3. AI Feature 2 — AI Product Comparison

### 3.1 What It Is

A feature that lets users select two or three products and generates an AI-written side-by-side comparison highlighting differences in material, price, style, and value. It gives a recommendation like: "For everyday casual wear, Product A is the better value. For athletic performance, Product B is the stronger choice."

### 3.2 How It Leverages Your Endpoints

| Step | Shopify Call |
|---|---|
| Fetch selected products | `GetProduct($id)` × 2 or 3 (existing use case) |
| Get extended specs | `GET /products/{id}/metafields.json` (REST Admin) |
| Get reviews | `GET /admin/api/products/{id}/metafields.json` → ratings |
| Feed to AI | Product JSON → LLM → structured comparison text |

### 3.3 Technical Implementation Approach

#### Domain Layer
```kotlin
// domain/product/usecase/CompareProductsUseCase.kt
class CompareProductsUseCase @Inject constructor(
    private val productRepository: IProductRepository
) {
    // Reuses GetProductDetailUseCase twice
    suspend operator fun invoke(
        productIds: List<String>
    ): Result<ProductComparison>
}

data class ProductComparison(
    val products: ImmutableList<ProductDetail>,
    val aiSummary: String,                    // e.g. "Product A is better for..."
    val dimensions: ImmutableList<ComparisonDimension>
)

data class ComparisonDimension(
    val label: String,                        // "Price", "Material", "Style"
    val values: ImmutableList<String>         // one per product
)
```

#### UX Flow
1. User long-presses a `ProductCard` → enters selection mode (like Instagram multi-select)
2. User taps up to 2 more products → "Compare" button appears
3. Navigate to `CompareScreen`
4. App fetches product details + calls LLM for AI summary
5. Display: product images side-by-side + attribute table + AI paragraph at the bottom

#### Presentation
```
CompareScreen
├── ProductHeaderRow (images + titles, sticky at top)
├── ComparisonTable (LazyColumn of attribute rows)
│   ├── PriceRow
│   ├── BrandRow
│   ├── MaterialRow (from tags/metafields)
│   └── AvailableSizesRow
└── AiSummaryCard (AI-generated paragraph with recommendation)
```

### 3.4 Pros

- **Simplest AI implementation** — the core logic is: `fetchProducts() → formatAsJson() → callLLM() → displayResult()`. No tool-calling, no streaming, no function calling loops
- **No extra Shopify endpoints needed** — `GetProduct($id)` is already implemented in your collection and existing `GetProductDetailUseCase` can be reused
- **Very low latency** — you do one LLM call with structured data and get a structured response. Total time: 1–2 seconds
- **High user value in fashion** — comparing two jackets or two sneakers is a real decision users face. This directly reduces purchase hesitation
- **Easy to test** — you can mock the LLM response with a fixed comparison string and test the full UI and ViewModel independently
- **Fits well in existing ProductDetailScreen** — "Add to Comparison" button can be placed right next to "Add to Cart", requiring minimal UI changes

### 3.5 Cons

- **Quality entirely depends on product description quality** — if the Shopify catalog has short or incomplete descriptions, the AI comparison will be shallow. Fashion products sometimes have very generic descriptions ("Premium quality jacket")
- **No structured spec data for most apparel** — unlike electronics (processor speed, RAM, screen size), clothing doesn't naturally have quantifiable comparison dimensions. The AI is guessing from marketing copy
- **UX friction for product selection** — users need to know they can compare products. If not surfaced clearly, this feature gets ignored
- **Limited to 2–3 products** — sending more products' data to an LLM inflates tokens rapidly
- **Metafields dependency is unreliable** — the REST Admin endpoint for metafields may or may not have structured data for your specific Shopify store, and accessing Admin API from the Android app violates the security contract defined in `AGENTS.md`
- **Less impressive in a demo** — compared to a live chat or visual image search, a comparison table is a quiet feature

---

## 4. AI Feature 3 — AI Image Search

### 4.1 What It Is

The user takes a photo or uploads an image from their gallery (a street style photo, a screenshot from Instagram, a photo of an item they own) and the app finds visually similar products in the WearZone Shopify catalog. Also known as "shop the look."

### 4.2 How It Leverages Your Endpoints

| Step | What Happens |
|---|---|
| User uploads image | Android CameraX or Gallery picker |
| AI Vision analysis | Image → LLM with vision (Claude claude-3-5-haiku, GPT-4o-mini, Gemini Flash) → text description |
| Text extraction | "Red women's floral maxi dress with puff sleeves" |
| Shopify search | `search(query: "floral maxi dress")` → product results |
| Display results | Standard `ProductCard` grid |

There is **no native visual search endpoint in Shopify's API**. All visual search must be converted to text first.

### 4.3 Technical Implementation Approach

#### Domain Layer
```kotlin
// domain/ai/imagesearch/usecase/PerformImageSearchUseCase.kt
class PerformImageSearchUseCase @Inject constructor(
    private val visionRepository: IVisionRepository,
    private val searchProductsUseCase: SearchProductsUseCase   // reuse
) {
    suspend operator fun invoke(imageBase64: String): Result<ImageSearchResult>
}

data class ImageSearchResult(
    val extractedDescription: String,     // "Red floral maxi dress..."
    val extractedKeywords: List<String>,  // ["red", "floral", "dress", "maxi"]
    val products: ImmutableList<Product>
)
```

#### Two-Step Pipeline
```
Step 1: Image → Vision LLM
POST /v1/messages (Claude or GPT-4V)
Body: {
  "model": "claude-3-haiku",
  "messages": [{
    "role": "user",
    "content": [
      { "type": "image", "source": { "type": "base64", "media_type": "image/jpeg", "data": "<base64>" }},
      { "type": "text", "text": "Describe the clothing item(s) in this image. 
                                  Focus on: item type, color, pattern, style, material. 
                                  Output 3-5 search keywords, comma-separated." }
    ]
  }]
}
Response: "red floral dress, maxi length, puff sleeves, summer"

Step 2: Keywords → Shopify Search
GraphQL: search(query: "red floral dress maxi", types: [PRODUCT])
```

#### Android-Specific Requirements
- `CameraX` library for in-app camera capture
- `ActivityResultContracts.GetContent()` for gallery picker
- Image compression to ~800px max dimension before base64 encoding (reduces API cost)
- Camera permission in `AndroidManifest.xml`

### 4.4 Pros

- **Highest wow factor** — demonstrating "take a photo → find the product" is the most visually impressive feature in a demo or presentation. It looks like a major tech company feature
- **Genuinely useful for fashion** — "shop the look" is a real pattern. Users find outfits they like on social media and want to buy them. This solves that exact problem
- **On-brand for WearZone** — a fashion/apparel app is the perfect domain for this feature
- **Reuses existing search infrastructure** — `SearchProductsUseCase` and `SearchScreen` need zero changes; you're just calling them from a new entry point
- **Simple two-step pipeline** — the implementation logic itself is straightforward: image → keywords → search. There are no complex agentic loops
- **LLM with vision is now widely available** — Claude claude-3-haiku, Gemini 1.5 Flash, GPT-4o-mini all support image input at low cost

### 4.5 Cons

- **Results quality is bounded by text search accuracy** — because Shopify has no visual search, you are doing `image → text → text search`. If the AI describes the image as "blue jacket" but the product is listed as "navy coat", there will be no match. The feature can feel broken even when the implementation is correct
- **Requires Android camera integration** — `CameraX` is not trivial for a team that might not have worked with it. Camera preview, permissions handling, lifecycle, and compression add significant work on top of the AI integration
- **Needs image compression logic** — raw photos from a modern phone are 3–8MB. You must compress to base64 before sending, and this needs careful testing to avoid OOM errors
- **Most expensive per-call** — vision LLM calls with image input cost significantly more tokens than text-only calls. A free-tier quota runs out faster
- **Privacy considerations** — storing or transmitting user photos requires clear privacy disclosure, even in a student project
- **No true visual similarity** — the result is keyword-matched products, not visually similar ones. True visual search requires pre-computing embeddings for every product image and running vector similarity search — far beyond the scope of this project
- **Highest implementation complexity** — of all five features, this has the most moving parts: CameraX, permissions, image compression, base64 encoding, vision API, text extraction, Shopify search, and result display
- **Fragile in testing** — if the LLM describes an image differently than expected, the search returns nothing, making end-to-end testing very difficult to automate

---

## 5. AI Feature 4 — AI Outfit Generator

### 5.1 What It Is

From any product detail page, the user taps "Generate Outfit" and the AI curates a complete outfit using other products from the WearZone store — e.g., if viewing a white linen shirt, the AI suggests matching trousers, shoes, and a belt from the available catalog, along with a style tip.

### 5.2 How It Leverages Your Endpoints

| Step | Shopify Call |
|---|---|
| Get anchor product | `GetProduct($id)` — current product detail |
| Get Shopify's own suggestions | `productRecommendations(productId: $id)` |
| Get products by complementary type | `products(query: "product_type:Pants")` |
| Get trending items | `products(sortKey: BEST_SELLING)` |
| Send all to LLM | Product catalog → LLM → select 3–5 products + write style tip |

The `productRecommendations` endpoint is a first-class Shopify feature that uses Shopify's own recommendation AI. Your job is to layer your own LLM on top to curate the results into a cohesive outfit narrative.

### 5.3 Technical Implementation Approach

#### Domain Layer
```kotlin
// domain/ai/outfit/usecase/GenerateOutfitUseCase.kt
class GenerateOutfitUseCase @Inject constructor(
    private val outfitRepository: IOutfitRepository,
    private val getProductDetailUseCase: GetProductDetailUseCase,
    private val getRecommendationsUseCase: GetProductRecommendationsUseCase
) {
    suspend operator fun invoke(anchorProductId: String): Result<GeneratedOutfit>
}

data class GeneratedOutfit(
    val anchorProduct: Product,
    val outfitItems: ImmutableList<Product>,   // 2–4 curated items
    val styleTip: String,                      // AI-generated styling description
    val occasion: String                       // "Beach Party", "Office Casual", etc.
)
```

#### The LLM Prompt Strategy
```
System: "You are WearZone's personal stylist. You will be given a 'hero product' 
         and a list of available items from our store catalog. Your job is to 
         curate a complete outfit. Output ONLY valid JSON with keys: 
         selected_product_ids (array of 2-4 IDs from the catalog), 
         style_tip (2 sentences), occasion (1-3 words)."

User: "Hero product: { title: 'White Linen Shirt', type: 'Tops', tags: ['casual','summer'] }
       Available catalog: [
         { id: 'gid://1', title: 'Navy Chino Pants', type: 'Bottoms', price: '$45' },
         { id: 'gid://2', title: 'White Sneakers', type: 'Shoes', price: '$60' },
         ...
       ]
       Build an outfit."
```

The LLM returns JSON with product IDs → app fetches those products and displays them as outfit cards.

#### Presentation
```
OutfitGeneratorScreen (launched from ProductDetailScreen)
├── OutfitHeroCard (the anchor product the user was viewing)
├── GeneratedItemsGrid (2x2 grid of AI-selected products)
│   └── Each card has: image, name, price, "Add to Cart" / "Add to Wishlist"
├── StyleTipCard (AI text: "This relaxed summer look works for...")
├── OccasionBadge ("Perfect for: Beach Casual")
└── RegenerateButton ("Try a different combination")
```

The "Regenerate" button changes the temperature/seed in the LLM call, producing a different outfit from the same catalog.

### 5.4 Pros

- **Directly leverages `productRecommendations`** — this is a Shopify-native AI endpoint already in your GraphQL collection that most apps never use. Using it here is architecturally elegant and shows API knowledge
- **Highly on-brand** — an outfit generator is quintessentially a fashion feature. WearZone (wearable zone) is the perfect context for this
- **Drives engagement and cross-selling** — users who generate an outfit are likely to add multiple items, increasing order value. This is a genuine business value
- **"Regenerate" feature creates replay value** — unlike a comparison or static chat response, users will tap Regenerate multiple times, exploring the catalog more deeply
- **Can tie into Wishlist** — "Save Outfit to Wishlist" is a natural extension that saves all outfit items together, leveraging the `ToggleWishlistUseCase` that's already designed in `AGENTS.md`
- **Structured LLM output (JSON)** — because the AI returns product IDs from a known catalog, there is no hallucination risk for product data. You validate every returned ID against your Shopify API before display
- **Independent of user auth state** — guests can generate outfits from any product page, making the feature widely accessible

### 5.5 Cons

- **Critically dependent on catalog diversity** — if WearZone's Shopify store only sells one product type (e.g., only T-shirts), there is nothing to build an outfit with. You must verify that the store has tops, bottoms, shoes, and accessories before committing to this feature
- **Context window limits catalog size** — you cannot send all 200+ products to the LLM. You need to intelligently pre-filter the catalog (e.g., send 20 products max, filtered by complementary product type) before the LLM call. This adds filtering logic to the implementation
- **`productRecommendations` is not always accurate for outfit pairing** — Shopify's recommendation engine is based on purchase co-occurrence, not style compatibility. A user who bought a shirt and trousers together helps, but the model isn't a fashion stylist. The LLM needs to override bad recommendations
- **Token cost grows with catalog size** — the more products you send as context, the higher the cost per call. Careful pre-filtering is essential
- **Style subjectivity** — fashion is subjective. Some users will disagree with the AI's outfit choices, potentially hurting trust in the feature if the output isn't good

---

## 6. AI Feature 5 — AI Shopping Insights

### 6.1 What It Is

A personalized analytics section in the Account screen (or a dedicated Insights tab) that uses AI to generate natural language summaries of the user's shopping behavior. Examples:

- "You've spent $240 this month, mostly on Nike products"
- "You tend to buy new items when there's a sale — 3 of your last 5 orders used a discount code"
- "You've bought 4 T-shirts this year. You might be running low on the one from March"
- "Based on your order history, here are 3 products you're likely to love this season"

### 6.2 How It Leverages Your Endpoints

| Data Source | Shopify Call |
|---|---|
| User's orders | `GetCustomerOrders` (already mapped in GraphQL collection) |
| Customer profile | `GetCustomer` (name, email, address count) |
| Product context | From order `lineItems` (title, price, vendor) |
| Trending for comparison | `products(sortKey: BEST_SELLING)` |

All data comes from endpoints you already have. The AI input is just the serialized order history.

### 6.3 Technical Implementation Approach

#### Domain Layer
```kotlin
// domain/ai/insights/usecase/GetShoppingInsightsUseCase.kt
class GetShoppingInsightsUseCase @Inject constructor(
    private val insightsRepository: IInsightsRepository,
    private val getOrderHistoryUseCase: GetOrderHistoryUseCase   // reuse
) {
    suspend operator fun invoke(customerId: String): Result<ShoppingInsights>
}

data class ShoppingInsights(
    val summary: String,                           // "You've been mostly buying..."
    val topBrands: ImmutableList<BrandStat>,
    val monthlySpend: ImmutableList<MonthStat>,
    val recommendations: ImmutableList<Product>,   // AI-suggested items
    val funFact: String                            // "You've bought 6 items — a new record!"
)
```

#### The LLM Prompt Strategy
```
System: "You are a personal shopping assistant. Analyze this user's order history and 
         generate friendly, actionable insights. Keep the tone warm and conversational.
         Output valid JSON only with keys: summary, top_brands, fun_fact, 
         suggested_search_queries."

User: "Order history: [
  { date: 'Jan 2026', items: [{ title: 'Nike Air Max', price: 90, vendor: 'Nike' }] },
  { date: 'Feb 2026', items: [{ title: 'Adidas Hoodie', price: 55, vendor: 'Adidas' }] },
  ...
]"
```

#### Presentation
```
InsightsSection (inside AccountScreen or new InsightsScreen)
├── InsightsSummaryCard (AI-generated paragraph)
├── TopBrandsChart (simple bar chart or pill badges)
├── MonthlySpendGraph (optional: line chart with recharts equivalent)
├── FunFactCard ("🎉 You've explored 4 different brands this year!")
└── PersonalizedRecommendationsRow (horizontal scroll of ProductCards)
```

### 6.4 Pros

- **All required data is already available** — `GetCustomerOrders` and `GetCustomer` are both in your GraphQL collection and return rich data (order dates, items, prices, vendors, discount codes used)
- **Zero new Shopify endpoints needed** — this is 100% powered by APIs you have already mapped and tested in Postman
- **No external image processing** — text-only LLM calls are cheaper, faster, and simpler than vision calls
- **Fits naturally in existing Account screen** — the `AccountScreen` in your `AGENTS.md` already shows order history preview. Insights are a natural extension of that
- **Differentiates for authenticated users** — gives logged-in users a reason to create an account and keep buying, which is genuine business value
- **Easy to test** — you can create a fixed mock order history and assert specific insights are generated. The domain logic is entirely deterministic

### 6.5 Cons

- **Zero value for new users and guests** — this feature is completely blank for anyone without purchase history. Since most users of a new app start as guests or have 0 orders, the feature is invisible to the majority of your users during a demo
- **Limited Shopify data for meaningful analysis** — `GetCustomerOrders` gives you items bought, prices, and dates. It does not give you: products viewed, time spent on pages, items added-and-removed from cart, or wishlisted items. Real behavioral analytics platforms (Mixpanel, Firebase Analytics) would be needed for richer insights, but those are not in scope
- **AI summaries can feel generic without enough data** — with only 1–3 orders, the AI has little to work with and produces shallow insights like "You've bought 2 items." The feature needs at least 5–10 orders to feel genuinely useful
- **Least visual feature** — the output is mostly text cards and simple stats. In a demo, it looks less impressive than chat, comparison tables, or outfit grids
- **No actionable loop for the user** — after reading insights, what does the user do? There's no clear next action that drives engagement with the rest of the app. Compare this to Shopping Assistant, which immediately sends you to product pages
- **Data freshness concern** — if order history is cached and the user placed a new order, insights might be stale until cache invalidation. You need to carefully manage when insights are regenerated

---

## 7. Head-to-Head Comparison Matrix

| Criterion | Smart Chat | Product Comparison | Image Search | Outfit Generator | Shopping Insights |
|---|:---:|:---:|:---:|:---:|:---:|
| **Works for Guest Users** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| **Works for 0-Order Users** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| **Uses Existing Endpoints** | ✅ All | ✅ Most | 🟡 Partially | ✅ All | ✅ All |
| **Uses `productRecommendations`** | ✅ Yes | ❌ No | ❌ No | ✅ Yes | ❌ No |
| **New Permissions Required** | ❌ None | ❌ None | ⚠️ Camera | ❌ None | ❌ None |
| **External Vision AI Needed** | ❌ No | ❌ No | ✅ Required | ❌ No | ❌ No |
| **Implementation Complexity** | Medium | Low | **Very High** | Medium | Low |
| **Result Quality Risk** | Low | Medium | **High** | Medium | Low |
| **Demo Wow Factor** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ |
| **Fits WearZone Fashion Domain** | ✅ Strong | ✅ Good | ✅ Strong | ✅ Perfect | 🟡 Partial |
| **MVI Architecture Integration** | Clean | Clean | Complex | Clean | Clean |
| **Requires Catalog Diversity** | ❌ No | ❌ No | ❌ No | ⚠️ Yes | ❌ No |
| **Works Without LLM for MVP** | ❌ No | ❌ No | ❌ No | 🟡 Partially | ❌ No |
| **Estimated Implementation Days** | 4–6 days | 2–3 days | 7–10 days | 4–6 days | 2–3 days |

---

## 8. Final Recommendation

### 🥇 Top Pick: AI Shopping Assistant (Smart Chat)

**Choose the AI Shopping Assistant.** Here is the full reasoning:

**It is the only feature that ties together every part of your application.** When the user asks "show me red sneakers under $60", the AI calls your `search` endpoint. When they ask "what's popular?", it calls `products(sortKey: BEST_SELLING)`. When they say "more like this", it calls `productRecommendations`. When they ask "what did I buy before?", it calls `GetCustomerOrders`. Every GraphQL query in your Postman collection gets used through one feature. This is rare and architecturally beautiful.

**It works for everyone from day one.** A guest who has never logged in can immediately ask "show me jackets under $100" and get results. Authenticated users get personalized responses. Shopping Insights is the only feature that requires prior orders to be useful at all — the rest of your evaluators fall somewhere in between.

**It fits WearZone's fashion domain better than any alternative.** Fashion shopping is inherently conversational. People ask friends "what should I wear to a wedding?" They don't filter by `product_type: Formal`. An AI that understands natural language maps perfectly to how people actually shop for clothes.

**The implementation is clean and fits your MVI architecture exactly.** The chat feature lives in its own `presentation/ai/chat/`, `domain/ai/chat/`, and `data/remote/ai/` folders. It does not touch any existing screens, ViewModels, or UseCases — it only calls them. This means the rest of your team can keep working on Cart, Checkout, or Account while one or two members implement the AI chat.

**It has the highest demo value relative to its complexity.** A live chat conversation in a mobile demo is immediately understandable and impressive to evaluators. You type "find me something for a beach trip" and products appear. That moment lands better than a comparison table or an analytics dashboard.

### 🥈 Strong Second Pick: AI Outfit Generator

If your Shopify store catalog has at least 3 product types (tops, bottoms, shoes), the Outfit Generator is your best alternative. It directly uses `productRecommendations` — a Shopify AI endpoint that most student projects ignore — and it creates a genuinely engaging "complete the look" user journey. It's also easier to demo than insights and simpler to implement than image search.

Only choose this if you can verify the catalog has sufficient diversity. An outfit generator with only T-shirts in the catalog is not an outfit generator.

### ❌ What Not to Choose and Why

**Do not choose AI Image Search** unless you have a team member with strong experience in Android CameraX, image processing, and you have at least 10 days of runway. The gap between "works" and "works well" in image search is enormous. The text-based search backend means results will often miss the mark even when the AI vision description is accurate. You will spend more time debugging the gap between visual similarity and text matching than building actual AI features.

**Do not choose AI Shopping Insights** as your primary feature for a class project. The demo will show a blank screen for any evaluator who hasn't placed orders in your test store. The feature requires seeded test data to demonstrate, and even then it lacks the interactivity that makes AI demos compelling.

---

## 9. Implementation Roadmap for AI Shopping Assistant

Once your team decides, here is the step-by-step approach:

### Sprint 1 — Foundation (Days 1–2)
- Create `domain/ai/chat/model/ChatMessage.kt`, `ChatRole.kt`
- Create `IAiChatRepository` interface in domain
- Create `SendChatMessageUseCase` in domain
- Unit test: `SendChatMessageUseCase` with `FakeAiChatRepository`

### Sprint 2 — Data Layer (Days 2–3)
- Create `AiApiService` (Retrofit interface to LLM API, e.g., Anthropic `/v1/messages`)
- Create `AiChatRemoteDataSourceImpl` with tool-calling logic
- Define the tool schema for: `search_products`, `get_recommendations`, `get_best_sellers`
- Inject `IoDispatcher` here (same pattern as every other `RepositoryImpl`)
- Integration test: send a real LLM call with a mock tool call response

### Sprint 3 — Presentation Layer (Days 3–5)
- Create `ChatUiState`, `ChatUiIntent`, `ChatUiEffect`
- Create `ChatViewModel` with `StateFlow<ChatUiState>` and `Channel<ChatUiEffect>`
- Build `ChatScreen` with `LazyColumn` for messages + input field
- Build `MessageBubble` component (USER: right-aligned, ASSISTANT: left-aligned)
- Build `ProductSuggestionsRow` (horizontal scroll of mini `ProductCard` components)
- Add FAB to `HomeScreen` and/or `ProductListScreen` that navigates to `ChatScreen`

### Sprint 4 — Polish & Edge Cases (Days 5–6)
- Typing indicator (animated dots while AI is responding)
- Error state (network failure, AI API error)
- Empty state (first open: "Hi! Ask me anything about our collection")
- Persist chat history in `DataStore` so conversation survives app restart
- Handle "product not found" gracefully when AI suggests a product ID that doesn't exist in Shopify
- Add `ChatRoute` to `Route.kt` and wire in `NavGraph.kt`

---

## 10. Quick Reference Card

| If you want... | Choose |
|---|---|
| Maximum user coverage (guest + auth) | Smart Chat |
| Highest demo wow factor | Smart Chat or Image Search |
| Simplest implementation | Product Comparison |
| Most on-brand for fashion | Outfit Generator |
| Best use of `productRecommendations` endpoint | Smart Chat or Outfit Generator |
| Personalized experience for returning customers | Shopping Insights |
| Avoid camera/image complexity | Smart Chat, Comparison, or Insights |
| Minimum new Shopify endpoints | Shopping Insights |

---

*Document compiled from: `Shopify_Project_Specs.pdf` · `MAD46_Shopify_Storefront_GraphQL_postman_collection.json` · `Shopify_Admin_REST_-_Mobile_E-Commerce_App_postman_collection.json` · `WearZone-ITI/WearZone` AGENTS.md (1920 lines, June 2026)*
