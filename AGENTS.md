# WEARZONE — Android · AI Agent Rules & Architecture Contract

> **MANDATORY:** Read this file **completely** before writing a single line of code.
> Every rule below is non-negotiable and applies to every AI agent working on this project.
> Violations will be rejected in code review without exception.

---

## 0. Project Identity

| Field           | Value                                                             |
|-----------------|-------------------------------------------------------------------|
| **App Name**    | WearZone                                                          |
| **Platform**    | Android (Kotlin + Jetpack Compose)                                |
| **Domain**      | E-Commerce — Shopify-backed fashion/apparel browsing & checkout   |
| **Users**       | Guest · Authenticated Customer                                    |
| **Backend**     | Shopify API + Firebase Auth                                       |
| **Reference**   | `Shopify_Project_Specs.pdf` ← READ THIS FIRST                    |
| **Design Ref**  | https://stitch.withgoogle.com/projects/2465420536387730507        |

---

## ⚠️ CRITICAL SECURITY NOTICE — Shopify API Strategy

The project spec lists the Shopify Admin REST API as the primary integration and GraphQL as a "Great Bonus."
**This distinction must be understood before writing a single network call.**

### The Security Problem with Admin REST API in a Mobile App

The Admin API is designed exclusively for **server-to-server** communication.
Bundling an Admin API Key inside an Android APK gives anyone who decompiles
the APK **full read/write access to the entire Shopify store backend** — products,
orders, customer data, financials. This is not a theoretical risk; APK decompilation
takes under 60 seconds with freely available tools.

### The Right Decision: Two Paths

| Path | API | Library | Security | Performance | When to use |
|------|-----|---------|----------|-------------|-------------|
| **A — Senior / Production** | Shopify Storefront GraphQL API | Apollo Kotlin | ✅ Safe for public APKs | ✅ Fetch only what you need | If team agrees to use bonus points |
| **B — Academic / Grading** | Shopify Admin REST API | Retrofit 2 | ❌ Academic use only | ⚠️ Over-fetches | If grading rubric requires REST |

**Default for this project: Path B (Retrofit + Admin REST)** for grading compliance.
However, sections §1.4 and §7 include parallel GraphQL notes so any agent
can switch to Path A without restructuring the codebase.

> If Path A is chosen, replace `Retrofit` + `OkHttp` in the tech stack with
> `Apollo Kotlin` (latest). The domain and presentation layers change **zero lines**.

---

## 1. Confirmed Technology Stack

> ⚠️ Do NOT suggest or use any technology not listed below without explicit approval.

### 1.1 Core Language & UI

| Concern          | Technology                          | Notes                              |
|------------------|-------------------------------------|------------------------------------|
| **Language**     | Kotlin (latest stable)              | 100% Kotlin — zero Java files      |
| **UI Toolkit**   | Jetpack Compose (BOM latest stable) | Zero XML layouts anywhere          |
| **Min SDK**      | 26 (Android 8.0)                    |                                    |
| **Target SDK**   | Latest stable                       |                                    |

### 1.2 Architecture & DI

| Concern                  | Technology                                              | Notes                                         |
|--------------------------|---------------------------------------------------------|-----------------------------------------------|
| **Architecture**         | Clean Architecture + MVI                                | Strict layer separation — see §3              |
| **Dependency Injection** | Hilt                                                    | Only DI framework allowed — no Koin           |
| **ViewModel**            | `androidx.lifecycle:lifecycle-viewmodel-compose`        | One ViewModel per screen                      |
| **State Management**     | `StateFlow<UiState>` + `Channel<UiEffect>`              | No LiveData — see §3 for full MVI contract    |

### 1.3 Async & Reactive

| Concern               | Technology                                         | Notes                                       |
|-----------------------|----------------------------------------------------|---------------------------------------------|
| **Async**             | Kotlin Coroutines                                  | `viewModelScope`, `suspend fun`             |
| **Reactive Streams**  | Kotlin Flow (cold) · StateFlow · Channel (hot)     | No RxJava                                   |
| **Dispatchers**       | Injected into **data layer only** via Hilt         | ViewModels are 100% dispatcher-agnostic — see §6 |
| **Exception Handling**| `runCatching` + `Result<T>` + `CoroutineExceptionHandler` | See §6                               |

### 1.4 Network

| Concern                 | Technology                                     | Notes                                               |
|-------------------------|------------------------------------------------|-----------------------------------------------------|
| **HTTP Client (Path B)**| Retrofit 2 (latest) + OkHttp 4                | ⚠️ Admin API — academic use only                    |
| **HTTP Client (Path A)**| Apollo Kotlin (latest)                         | ✅ Storefront GraphQL — production-grade alternative |
| **Serialization**       | `kotlinx.serialization`                        | No Gson, no Moshi                                   |
| **Retrofit Converter**  | `retrofit2-kotlinx-serialization-converter`    | Path B only                                         |
| **Interceptors**        | OkHttp `Interceptor` (Auth, Logging, Error)    | Path B only — see §7                                |

### 1.5 Local Storage

| Concern               | Technology                       | Notes                                           |
|-----------------------|----------------------------------|-------------------------------------------------|
| **Database**          | Room (latest)                    | All DAOs: `suspend fun` or `Flow<>`             |
| **Preferences**       | DataStore Preferences            | Simple key-value: currency, locale              |
| **Typed Preferences** | Proto DataStore                  | Structured data: `UserPreferences.proto`        |

### 1.6 Navigation

| Concern          | Technology                                   | Notes                                                    |
|------------------|----------------------------------------------|----------------------------------------------------------|
| **Navigation**   | Jetpack Navigation Compose 2.8.0+            | **Type-safe routing only** — string routes are BANNED    |
| **Deep Links**   | Declared in `NavGraph` + `AndroidManifest`   |                                                          |

### 1.7 Collections in UI State

| Concern                   | Technology                       | Notes                                               |
|---------------------------|----------------------------------|-----------------------------------------------------|
| **Collections in UiState**| `kotlinx.collections.immutable`  | `ImmutableList`, `ImmutableMap` — see §3.2          |

### 1.8 Images

| Concern          | Technology       | Notes                         |
|------------------|------------------|-------------------------------|
| **Image Loading**| Coil 3 (Compose) | `AsyncImage` everywhere       |

### 1.9 Build Tooling

| Concern                       | Technology | Notes                                    |
|-------------------------------|------------|------------------------------------------|
| **Annotation Processing**     | KSP        | **KAPT is BANNED** — KSP is 2× faster   |

### 1.10 Testing

| Concern          | Technology                            | Notes                              |
|------------------|---------------------------------------|------------------------------------|
| **Unit Testing** | JUnit 5 + Kotlin Coroutines Test      | `runTest`, `TestCoroutineScheduler`|
| **Mocking**      | MockK                                 | No Mockito                         |
| **Flow Testing** | Turbine                               | StateFlow + Channel assertions     |
| **UI Testing**   | Compose Testing (`composeTestRule`)   |                                    |
| **Test Doubles** | Fakes preferred over Mocks for repos  | See §11                            |

### ❌ Explicitly Forbidden — Zero Exceptions

| Technology            | Reason                                      | Use Instead                    |
|-----------------------|---------------------------------------------|--------------------------------|
| `kapt`                | Slow — deprecated for Kotlin projects       | KSP                            |
| String-based nav routes | Type-unsafe, runtime crashes             | Type-safe nav (§5)             |
| `List<T>` in UiState  | Compose treats it as unstable → over-recomposition | `ImmutableList<T>`   |
| `MutableSharedFlow` for effects | Drops events when no subscriber | `Channel(BUFFERED)`    |
| `LiveData`            | Replaced by StateFlow                       | `StateFlow`                    |
| `RxJava` / `RxKotlin` | Replaced by Kotlin Flow                   | Kotlin Flow                    |
| `Koin`                | Only Hilt allowed                           | Hilt                           |
| XML layouts           | Compose-only project                        | Jetpack Compose                |
| `Gson` / `Moshi`      | Replaced by kotlinx.serialization           | `kotlinx.serialization`        |
| `GlobalScope`         | No lifecycle awareness — leaks              | `viewModelScope`               |
| `CoroutineDispatcher` injected into ViewModel | VM must be threading-agnostic | Inject into repos only |
| `mutableStateOf` for VM-level state | Not observable outside Compose | `StateFlow` in VM   |
| `findViewById`        | No XML                                      | Compose                        |
| Any global `object` with mutable state | Thread-unsafe singleton | Inject via Hilt           |

---

## 2. Module Structure — STRICTLY ENFORCED

Four Gradle modules. Each has its own `build.gradle.kts`.

```
root/
├── app/           ← Android: @HiltAndroidApp, MainActivity, NavHost, Hilt wiring modules
├── data/          ← Android: RepoImpl, Room entities/DAOs, Retrofit services, DTOs
├── domain/        ← Pure Kotlin: UseCases, domain Models, Repository interfaces
└── presentation/  ← Android: Compose screens, ViewModels, UiState/UiIntent/UiEffect, Components
```

### 2.1 Module Dependency Graph

```
       ┌─────────────────────────────────────┐
       │               app                   │  ← only module seeing both data & presentation
       └───────────┬─────────────┬───────────┘
                   ▼             ▼
           presentation        data
                   │             │
                   └──────┬──────┘
                          ▼
                        domain           ← knows nothing; pure Kotlin
```

**Hard Rules:**
- `domain` → imports nothing outside stdlib
- `data` → imports `domain` only
- `presentation` → imports `domain` only
- `app` → imports `presentation` + `data` (for Hilt @Module binding only)

### 2.2 Complete Folder Structure

```
data/src/main/kotlin/com.wearzone.data/
├── local/
│   ├── db/
│   │   └── WearZoneDatabase.kt          ← @Database
│   ├── dao/
│   │   ├── CartDao.kt
│   │   ├── WishlistDao.kt
│   │   └── ProductCacheDao.kt
│   ├── entity/                          ← Room @Entity classes (suffix: Entity)
│   │   ├── CartItemEntity.kt
│   │   ├── WishlistItemEntity.kt
│   │   └── ProductCacheEntity.kt
│   └── datasource/
│       ├── ICartLocalDataSource.kt
│       ├── CartLocalDataSourceImpl.kt
│       ├── IProductLocalDataSource.kt
│       └── ProductLocalDataSourceImpl.kt
├── remote/
│   ├── api/
│   │   ├── ProductApiService.kt         ← Retrofit @Service
│   │   ├── OrderApiService.kt
│   │   └── CurrencyApiService.kt
│   ├── dto/                             ← @Serializable DTOs (suffix: Dto)
│   │   ├── ProductDto.kt                ← contains .toDomain() mapper
│   │   ├── ProductDetailDto.kt
│   │   ├── OrderDto.kt
│   │   └── AddressDto.kt
│   ├── interceptor/
│   │   ├── AuthInterceptor.kt
│   │   ├── ErrorInterceptor.kt
│   │   └── LoggingInterceptor.kt
│   └── datasource/
│       ├── IProductRemoteDataSource.kt
│       └── ProductRemoteDataSourceImpl.kt
├── repository/
│   ├── ProductRepositoryImpl.kt
│   ├── CartRepositoryImpl.kt
│   ├── WishlistRepositoryImpl.kt
│   ├── CheckoutRepositoryImpl.kt
│   └── AccountRepositoryImpl.kt
└── di/
    ├── NetworkModule.kt
    ├── DatabaseModule.kt
    ├── DataSourceModule.kt
    └── DispatcherModule.kt              ← @IoDispatcher, @DefaultDispatcher live here

domain/src/main/kotlin/com.wearzone.domain/
├── auth/
│   ├── model/                           ← Pure Kotlin (suffix: none — just the concept name)
│   │   ├── User.kt
│   │   └── AuthToken.kt
│   ├── repository/
│   │   └── IAuthRepository.kt
│   └── usecase/
│       ├── LoginUseCase.kt
│       ├── RegisterUseCase.kt
│       └── LogoutUseCase.kt
├── product/
│   ├── model/
│   │   ├── Product.kt
│   │   ├── ProductDetail.kt
│   │   ├── Review.kt
│   │   ├── Brand.kt
│   │   └── Category.kt
│   ├── repository/
│   │   └── IProductRepository.kt
│   └── usecase/
│       ├── GetProductsUseCase.kt
│       ├── GetProductDetailUseCase.kt
│       └── SearchProductsUseCase.kt
├── cart/
│   ├── model/
│   │   ├── Cart.kt
│   │   └── CartItem.kt
│   ├── repository/
│   │   └── ICartRepository.kt
│   └── usecase/
│       ├── GetCartUseCase.kt
│       ├── AddToCartUseCase.kt
│       ├── RemoveFromCartUseCase.kt
│       └── UpdateCartQuantityUseCase.kt
├── wishlist/
│   ├── model/
│   │   └── WishlistItem.kt
│   ├── repository/
│   │   └── IWishlistRepository.kt
│   └── usecase/
│       ├── GetWishlistUseCase.kt
│       ├── ToggleWishlistUseCase.kt
│       └── RemoveFromWishlistUseCase.kt
├── checkout/
│   ├── model/
│   │   ├── Order.kt
│   │   ├── Coupon.kt
│   │   ├── PaymentMethod.kt
│   │   └── Address.kt
│   ├── repository/
│   │   ├── ICheckoutRepository.kt
│   │   └── IAddressRepository.kt
│   └── usecase/
│       ├── PlaceOrderUseCase.kt
│       ├── ApplyCouponUseCase.kt
│       └── ValidateAddressUseCase.kt
├── account/
│   ├── model/
│   │   ├── UserProfile.kt
│   │   ├── OrderHistory.kt
│   │   └── CurrencyRate.kt
│   ├── repository/
│   │   ├── IAccountRepository.kt
│   │   └── ICurrencyRepository.kt
│   └── usecase/
│       ├── GetProfileUseCase.kt
│       ├── GetOrderHistoryUseCase.kt
│       └── GetExchangeRatesUseCase.kt
└── common/
    ├── result/
    │   └── DataResult.kt               ← sealed class DataResult<T>
    └── error/
        └── DomainError.kt              ← sealed class DomainError

presentation/src/main/kotlin/com.wearzone.presentation/
├── auth/
│   ├── login/
│   │   ├── LoginScreen.kt
│   │   ├── LoginViewModel.kt
│   │   ├── LoginUiState.kt             ← sealed interface
│   │   ├── LoginUiIntent.kt            ← sealed interface
│   │   ├── LoginUiEffect.kt            ← sealed interface
│   │   └── components/
│   │       ├── LoginForm.kt
│   │       └── SocialLoginButtons.kt
│   └── register/
│       ├── RegisterScreen.kt
│       ├── RegisterViewModel.kt
│       ├── RegisterUiState.kt
│       ├── RegisterUiIntent.kt
│       ├── RegisterUiEffect.kt
│       └── components/
├── home/
│   ├── HomeScreen.kt
│   ├── HomeViewModel.kt
│   ├── HomeUiState.kt
│   ├── HomeUiIntent.kt
│   ├── HomeUiEffect.kt
│   └── components/
│       ├── CategoryRow.kt
│       ├── BrandRow.kt
│       └── FeaturedProductsGrid.kt
├── product/
│   ├── list/
│   │   ├── ProductListScreen.kt
│   │   ├── ProductListViewModel.kt
│   │   ├── ProductListUiState.kt
│   │   ├── ProductListUiIntent.kt
│   │   ├── ProductListUiEffect.kt
│   │   └── components/
│   │       ├── ProductCard.kt
│   │       ├── FilterBottomSheet.kt
│   │       └── SortBottomSheet.kt
│   └── detail/
│       ├── ProductDetailScreen.kt
│       ├── ProductDetailViewModel.kt
│       ├── ProductDetailUiState.kt
│       ├── ProductDetailUiIntent.kt
│       ├── ProductDetailUiEffect.kt
│       └── components/
│           ├── ImageCarousel.kt
│           ├── SizeSelector.kt
│           └── ReviewCard.kt
├── search/
│   ├── SearchScreen.kt
│   ├── SearchViewModel.kt
│   ├── SearchUiState.kt
│   ├── SearchUiIntent.kt
│   ├── SearchUiEffect.kt
│   └── components/
├── cart/
│   ├── CartScreen.kt
│   ├── CartViewModel.kt
│   ├── CartUiState.kt
│   ├── CartUiIntent.kt
│   ├── CartUiEffect.kt
│   └── components/
│       ├── CartItemRow.kt
│       └── PriceSummary.kt
├── wishlist/
│   ├── WishlistScreen.kt
│   ├── WishlistViewModel.kt
│   ├── WishlistUiState.kt
│   ├── WishlistUiIntent.kt
│   ├── WishlistUiEffect.kt
│   └── components/
├── checkout/
│   ├── CheckoutScreen.kt
│   ├── CheckoutViewModel.kt
│   ├── CheckoutUiState.kt
│   ├── CheckoutUiIntent.kt
│   ├── CheckoutUiEffect.kt
│   └── components/
│       ├── AddressCard.kt
│       ├── PaymentSelector.kt
│       ├── CouponField.kt
│       └── OrderSummaryCard.kt
├── account/
│   ├── AccountScreen.kt
│   ├── AccountViewModel.kt
│   ├── AccountUiState.kt
│   ├── AccountUiIntent.kt
│   ├── AccountUiEffect.kt
│   └── components/
│       ├── OrderHistoryCard.kt
│       └── WishlistPreview.kt
├── address/
│   ├── AddressScreen.kt
│   ├── AddressViewModel.kt
│   ├── AddressUiState.kt
│   ├── AddressUiIntent.kt
│   ├── AddressUiEffect.kt
│   └── components/
└── common/
    ├── components/                     ← Shared composables
    ├── navigation/
    │   ├── AppNavHost.kt
    │   └── AppRoutes.kt               ← @Serializable route objects (type-safe)
    └── theme/
        ├── AppTheme.kt
        ├── AppColors.kt
        ├── AppTypography.kt
        └── AppShapes.kt

app/src/main/kotlin/com.wearzone/
├── WearZoneApplication.kt             ← @HiltAndroidApp
├── MainActivity.kt                    ← @AndroidEntryPoint, single Activity
└── di/
    ├── NetworkModule.kt               ← Retrofit, OkHttp, Interceptors
    ├── DatabaseModule.kt              ← RoomDatabase, DAOs
    ├── RepositoryModule.kt            ← @Binds IRepo → RepoImpl
    ├── DataSourceModule.kt            ← @Binds IDataSource → DataSourceImpl
    ├── DataStoreModule.kt             ← DataStore, ProtoDataStore
    └── DispatcherModule.kt            ← @IoDispatcher, @DefaultDispatcher qualifiers
```

---

## 3. MVI Architecture — STRICTLY ENFORCED

### 3.1 The MVI Contract

```
User Action (Intent)
       │
       ▼
ViewModel.handleIntent(intent: UiIntent)
       │
       ├──► private fun calls UseCase (suspend)
       │           │
       │           ▼
       │       Repository (handles IO dispatch internally)
       │           │
       │     Result<T> returned
       │           │
       ├──► _uiState.value = NewState      (StateFlow — for UI rendering)
       └──► _uiEffect.send(Effect)         (Channel  — for one-shot events)
                   │
                   ▼
       Composable collects in LaunchedEffect → navigation / snackbar
```

### 3.2 Three Contract Files Per Screen (MANDATORY)

Every screen folder **must** contain exactly these three contract files:

#### `FeatureUiState.kt` — What the UI renders

```kotlin
// presentation/product/list/ProductListUiState.kt
sealed interface ProductListUiState {
    data object Loading : ProductListUiState
    data object Empty   : ProductListUiState

    // ✅ ImmutableList REQUIRED — standard List<T> is BANNED in UiState
    data class Success(
        val products: ImmutableList<ProductUiModel>,
        val isLoadingMore: Boolean = false,
    ) : ProductListUiState

    data class Error(val message: String) : ProductListUiState
}

// UiModel — what the presentation layer maps domain models to
data class ProductUiModel(
    val id: String,
    val title: String,
    val vendor: String,
    val formattedPrice: String,   // pre-formatted for display
    val imageUrl: String,
    val isInWishlist: Boolean,
)
```

#### `FeatureUiIntent.kt` — What the user does

```kotlin
// presentation/product/list/ProductListUiIntent.kt
sealed interface ProductListUiIntent {
    data class OnProductClicked(val productId: String)  : ProductListUiIntent
    data class OnFilterApplied(val filter: ProductFilter) : ProductListUiIntent
    data class OnSortSelected(val sort: SortOption)     : ProductListUiIntent
    data class OnWishlistToggled(val productId: String) : ProductListUiIntent
    data object OnRetry                                  : ProductListUiIntent
    data object OnLoadMore                               : ProductListUiIntent
}
```

#### `FeatureUiEffect.kt` — One-shot side effects (via Channel)

```kotlin
// presentation/product/list/ProductListUiEffect.kt
sealed interface ProductListUiEffect {
    data class NavigateToDetail(val productId: String) : ProductListUiEffect
    data class ShowError(val message: String)          : ProductListUiEffect
    data object NavigateToLogin                         : ProductListUiEffect
}
```

### 3.3 ViewModel Structure — STRICTLY ENFORCED

```kotlin
// presentation/product/list/ProductListViewModel.kt
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val toggleWishlistUseCase: ToggleWishlistUseCase,
    // ✅ NO dispatcher injected here — ViewModel is threading-agnostic
) : ViewModel() {

    // ── State ─────────────────────────────────────────────────────────────
    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    // ── Effects ───────────────────────────────────────────────────────────
    // Channel guarantees delivery — no event is ever dropped
    private val _uiEffect = Channel<ProductListUiEffect>(Channel.BUFFERED)
    val uiEffect: Flow<ProductListUiEffect> = _uiEffect.receiveAsFlow()

    init { loadProducts() }

    // ── Intent Handler ────────────────────────────────────────────────────
    // ✅ handleIntent is a lean dispatcher — all logic in private functions
    fun handleIntent(intent: ProductListUiIntent) {
        when (intent) {
            is ProductListUiIntent.OnProductClicked   -> navigateToDetail(intent.productId)
            is ProductListUiIntent.OnFilterApplied    -> applyFilter(intent.filter)
            is ProductListUiIntent.OnSortSelected     -> applySort(intent.sort)
            is ProductListUiIntent.OnWishlistToggled  -> toggleWishlist(intent.productId)
            is ProductListUiIntent.OnRetry            -> loadProducts()
            is ProductListUiIntent.OnLoadMore         -> loadMoreProducts()
        }
    }

    // ── Private Functions — each does ONE thing ───────────────────────────
    private fun loadProducts() {
        viewModelScope.launch {          // ✅ No dispatcher — Main by default; repo handles IO
            _uiState.value = ProductListUiState.Loading
            getProductsUseCase()
                .onSuccess { products ->
                    _uiState.value = if (products.isEmpty())
                        ProductListUiState.Empty
                    else
                        ProductListUiState.Success(products.map { it.toUiModel() }.toImmutableList())
                }
                .onFailure { error ->
                    _uiState.value = ProductListUiState.Error(error.localizedMessage ?: "Unknown error")
                }
        }
    }

    private fun navigateToDetail(productId: String) {
        viewModelScope.launch {
            _uiEffect.send(ProductListUiEffect.NavigateToDetail(productId))
        }
    }

    private fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            toggleWishlistUseCase(productId).onFailure {
                _uiEffect.send(ProductListUiEffect.NavigateToLogin)
            }
        }
    }

    // ... applyFilter, applySort, loadMoreProducts — each a private function
}
```

### 3.4 Composable Screen Structure — STRICTLY ENFORCED

```kotlin
// presentation/product/list/ProductListScreen.kt

// ✅ Stateful root — only this level touches the ViewModel
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ✅ Effects collected in LaunchedEffect(Unit) — runs once, tied to composition
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProductListUiEffect.NavigateToDetail -> onNavigateToDetail(effect.productId)
                is ProductListUiEffect.NavigateToLogin  -> onNavigateToLogin()
                is ProductListUiEffect.ShowError        -> { /* show snackbar */ }
            }
        }
    }

    // ✅ State hoisting — pass state down, events up; content is stateless
    ProductListContent(
        uiState  = uiState,
        onIntent = viewModel::handleIntent,
    )
}

// ✅ Stateless content composable — pure rendering, no ViewModel reference
@Composable
private fun ProductListContent(
    uiState: ProductListUiState,
    onIntent: (ProductListUiIntent) -> Unit,
) {
    when (uiState) {
        is ProductListUiState.Loading -> AppLoadingOverlay()
        is ProductListUiState.Empty   -> EmptyStateWidget()
        is ProductListUiState.Error   -> AppErrorWidget(
            message = uiState.message,
            onRetry = { onIntent(ProductListUiIntent.OnRetry) },
        )
        is ProductListUiState.Success -> ProductGrid(
            products = uiState.products,
            onIntent = onIntent,
        )
    }
}
```

### 3.5 Layer Violation Rules — Zero Tolerance

| Rule                                           | Example of Violation                          |
|------------------------------------------------|-----------------------------------------------|
| Domain has ZERO Android imports                | `import android.*` in domain module           |
| Domain has ZERO Room/Retrofit imports          | `@Entity` on a domain Model                   |
| Presentation has ZERO data layer imports       | `ProductDto` referenced in ViewModel          |
| ViewModel never skips UseCase                  | Direct repo call from ViewModel               |
| UseCase has exactly ONE public `invoke()` operator | UseCase with multiple public methods     |
| DTOs never cross the repository boundary       | `ProductDto` passed to ViewModel              |
| Domain Models never enter DTOs                 | `Product` imported inside `ProductDto`        |
| `List<T>` never appears in a UiState           | `val products: List<ProductUiModel>`          |
| `MutableSharedFlow` never used for UI effects  | `_uiEffect = MutableSharedFlow<>()`           |
| `viewModelScope.launch(Dispatchers.IO)`        | Dispatcher argument in any ViewModel launch   |

### 3.6 SOLID Checklist — Before Writing Any Class

- **S** — Does this class have exactly one reason to change?
- **O** — Is the repository an interface (open for extension, closed for modification)?
- **L** — Can `FakeProductRepository` substitute `ProductRepositoryImpl` with zero caller changes?
- **I** — Is `IProductRepository` focused only on products (no cart or auth methods)?
- **D** — Does `ProductListViewModel` depend on `GetProductsUseCase`, not `ProductRepositoryImpl`?

---

## 4. Hilt Dependency Injection — STRICTLY ENFORCED

### 4.1 Application & Activity

```kotlin
// app/WearZoneApplication.kt
@HiltAndroidApp
class WearZoneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}

// app/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WearZoneTheme {
                AppNavHost()
            }
        }
    }
}
```

### 4.2 Hilt Module Patterns

```kotlin
// ✅ @Binds — for binding interface to implementation (abstract module)
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): IProductRepository

    @Binds @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): ICartRepository

    @Binds @Singleton
    abstract fun bindWishlistRepository(impl: WishlistRepositoryImpl): IWishlistRepository
}

// ✅ @Provides — for third-party classes or types you don't own (object module)
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        errorInterceptor: ErrorInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(errorInterceptor)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        })
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SHOPIFY_BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
```

### 4.3 Dispatcher Injection — Data Layer ONLY

```kotlin
// app/di/DispatcherModule.kt

// Step 1 — Declare qualifiers
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class DefaultDispatcher

// Step 2 — Provide them
@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}

// Step 3 — Inject ONLY into Repository and DataSource implementations
class ProductRepositoryImpl @Inject constructor(
    private val localDataSource: IProductLocalDataSource,
    private val remoteDataSource: IProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,   // ✅ here
) : IProductRepository {

    override suspend fun getProducts(): Result<List<Product>> =
        withContext(ioDispatcher) {            // ✅ dispatcher switch lives here
            runCatching {
                val dto = remoteDataSource.fetchProducts()
                dto.map { it.toDomain() }
            }
        }
}

// ✅ ViewModel — zero dispatcher knowledge
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
) : ViewModel() {
    private fun loadProducts() {
        viewModelScope.launch {             // ✅ main — repo handles IO internally
            /* ... */
        }
    }
}
```

### 4.4 Hilt Scopes

| Scope              | Use For                                              |
|--------------------|------------------------------------------------------|
| `@Singleton`       | Retrofit, OkHttpClient, RoomDatabase, DataStore      |
| `@ViewModelScoped` | Objects whose lifetime matches a single ViewModel    |
| `@ActivityScoped`  | Objects shared across the single activity's lifetime |

### 4.5 ViewModels

```kotlin
// ✅ Always @HiltViewModel + @Inject constructor
@HiltViewModel
class CartViewModel @Inject constructor(
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
) : ViewModel()

// ✅ In Composable — always hiltViewModel()
@Composable
fun CartScreen(viewModel: CartViewModel = hiltViewModel())

// ❌ NEVER instantiate ViewModel manually
val viewModel = CartViewModel(...)
```

### 4.6 KSP Configuration (MANDATORY)

```kotlin
// In every module's build.gradle.kts that uses annotation processing:
plugins {
    id("com.google.devtools.ksp")    // ✅ KSP
    // id("kotlin-kapt")             // ❌ KAPT is BANNED
}

dependencies {
    ksp("com.google.dagger:hilt-compiler:$hiltVersion")      // ✅ KSP
    ksp("androidx.room:room-compiler:$roomVersion")           // ✅ KSP
    // kapt("...") is BANNED everywhere
}
```

---

## 5. Navigation — Type-Safe Jetpack Navigation Compose 2.8.0+

### 5.1 Route Definitions — @Serializable Objects & Data Classes

```kotlin
// presentation/common/navigation/AppRoutes.kt

// ✅ Type-safe route objects — string routes are BANNED
@Serializable data object HomeRoute
@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object CartRoute
@Serializable data object WishlistRoute
@Serializable data object CheckoutRoute
@Serializable data object AccountRoute
@Serializable data object SearchRoute
@Serializable data object AddressRoute

// Routes with arguments use data class (type-safe — no string parsing)
@Serializable data class ProductListRoute(val brand: String? = null, val category: String? = null)
@Serializable data class ProductDetailRoute(val productId: String)

// ❌ BANNED — string routes
// "product_detail/{productId}"  ← NEVER use this pattern
```

### 5.2 NavHost Setup

```kotlin
// presentation/common/navigation/AppNavHost.kt
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = HomeRoute,
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {

        // ── Auth graph ───────────────────────────────────────────────────
        navigation<LoginRoute>(startDestination = LoginRoute) {
            composable<LoginRoute> {
                LoginScreen(
                    onLoginSuccess  = {
                        navController.navigate(HomeRoute) {
                            popUpTo<LoginRoute> { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(RegisterRoute) },
                )
            }
            composable<RegisterRoute> {
                RegisterScreen(onNavigateBack = { navController.popBackStack() })
            }
        }

        // ── Main graph ───────────────────────────────────────────────────
        composable<HomeRoute> {
            HomeScreen(
                onNavigateToProductList = { navController.navigate(ProductListRoute()) },
                onNavigateToSearch      = { navController.navigate(SearchRoute) },
            )
        }

        composable<ProductListRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProductListRoute>()   // ✅ type-safe arg extraction
            ProductListScreen(
                brand    = route.brand,
                category = route.category,
                onNavigateToDetail = { id -> navController.navigate(ProductDetailRoute(id)) },
            )
        }

        composable<ProductDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ProductDetailRoute>()  // ✅ type-safe
            ProductDetailScreen(
                productId       = route.productId,
                onNavigateBack  = { navController.popBackStack() },
                onNavigateToCart = { navController.navigate(CartRoute) },
            )
        }

        composable<CartRoute>     { CartScreen(navController) }
        composable<WishlistRoute> { WishlistScreen(navController) }
        composable<CheckoutRoute> { CheckoutScreen(navController) }
        composable<AccountRoute>  { AccountScreen(navController) }
        composable<SearchRoute>   { SearchScreen(navController) }
        composable<AddressRoute>  { AddressScreen(navController) }
    }
}
```

### 5.3 Navigation Rules

| Rule                                     | Detail                                                            |
|------------------------------------------|-------------------------------------------------------------------|
| String routes                            | BANNED — use `@Serializable` objects/data classes                 |
| Argument passing                         | IDs only via route data class — never pass domain objects         |
| Navigation called from ViewModel         | NEVER — emit `UiEffect` → collect in screen → call navController  |
| Back stack on auth completion            | `popUpTo<LoginRoute> { inclusive = true }`                        |
| Auth guard                               | Check auth state in NavHost before entering protected composables |
| NavController scope                      | Pass as lambda callbacks — never pass NavController into ViewModel|

---

## 6. Kotlin Coroutines & Flows — Rules

### 6.1 Dispatcher Responsibility Model

```
┌─────────────────┐    viewModelScope.launch { }   ┌─────────────────┐
│   ViewModel     │ ──────────── (Main) ──────────► │   UseCase       │
│  (no dispatcher)│                                 │  (no dispatcher)│
└─────────────────┘                                 └────────┬────────┘
                                                             │  suspend fun call
                                                    ┌────────▼────────┐
                                                    │   Repository    │
                                                    │  withContext(   │  ← @IoDispatcher
                                                    │  ioDispatcher)  │     injected here
                                                    └────────┬────────┘
                                                             │
                                              ┌──────────────▼──────────────┐
                                              │  Room DAO / Retrofit Service │
                                              │  (suspend — handles own IO)  │
                                              └──────────────────────────────┘
```

**Rule:** Dispatchers are injected exclusively at the `RepositoryImpl` and `DataSourceImpl` level.
ViewModels and UseCases are completely dispatcher-agnostic. Tests substitute
`UnconfinedTestDispatcher` via a `TestCoroutineRule` at the test boundary — no
production code changes needed.

### 6.2 Hot vs Cold Flows

| Type              | Use Case                                          | Behaviour                     |
|-------------------|---------------------------------------------------|-------------------------------|
| `Flow<T>` (cold)  | Repository exposing DB data or a single API call  | Starts on collection           |
| `StateFlow<T>`    | UiState in ViewModel                              | Always has value; replays last |
| `Channel<T>`      | One-shot UiEffects (navigation, snackbar)         | Guaranteed delivery via `receiveAsFlow()` |

```kotlin
// ✅ CORRECT — Room returns cold Flow; ViewModel converts to hot StateFlow
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun observeCart(): Flow<List<CartItemEntity>>    // cold
}

// In ViewModel — stateIn converts cold Flow to hot StateFlow
val cartUiState: StateFlow<CartUiState> = getCartUseCase()
    .map { result ->
        result.fold(
            onSuccess = { items -> CartUiState.Success(items.map { it.toUiModel() }.toImmutableList()) },
            onFailure = { CartUiState.Error(it.localizedMessage ?: "Error") },
        )
    }
    .stateIn(
        scope        = viewModelScope,
        started      = SharingStarted.WhileSubscribed(5_000),
        initialValue = CartUiState.Loading,
    )

// ✅ CORRECT — Channel for effects (never SharedFlow for one-shot events)
private val _uiEffect = Channel<CartUiEffect>(Channel.BUFFERED)
val uiEffect: Flow<CartUiEffect> = _uiEffect.receiveAsFlow()

// ✅ Sending an effect
private fun confirmRemoveItem(itemId: String) {
    viewModelScope.launch {
        _uiEffect.send(CartUiEffect.ShowConfirmDialog(itemId))
    }
}

// ❌ BANNED — SharedFlow for one-shot effects (events can be dropped)
private val _uiEffect = MutableSharedFlow<CartUiEffect>(replay = 0)
```

### 6.3 Preferred Flow Operators

```kotlin
.map { }               // transform
.filter { }            // filter
.catch { e -> }        // error handling in flow chain (do NOT let it propagate unhandled)
.onStart { }           // emit loading state before first item
.combine(other) { a, b -> }   // merge two streams (e.g. cartItems + discount)
.flatMapLatest { }     // cancel previous, switch to new (search: new query cancels old)
.debounce(300)         // throttle search input — always 300ms
.distinctUntilChanged()// skip duplicate emissions
.stateIn(...)          // cold Flow → hot StateFlow for ViewModel exposure
```

### 6.4 Exception Handling

```kotlin
// ✅ In suspend repository functions — use runCatching
override suspend fun getProducts(): Result<List<Product>> =
    withContext(ioDispatcher) {
        runCatching {
            remoteDataSource.fetchProducts().map { it.toDomain() }
        }
    }

// ✅ In Flow chains — use .catch
fun observeCart(): Flow<List<CartItem>> = cartDao
    .observeCart()
    .map { it.map { entity -> entity.toDomain() } }
    .catch { e ->
        Timber.e(e, "Cart observation error")
        emit(emptyList())
    }

// ✅ In ViewModel — handle Result from UseCase
viewModelScope.launch {
    getProductsUseCase()
        .onSuccess { products -> _uiState.value = ProductListUiState.Success(...) }
        .onFailure { error   -> _uiState.value = ProductListUiState.Error(error.localizedMessage ?: "Error") }
}

// ❌ BANNED — silent catch
try { ... } catch (e: Exception) { /* empty */ }
```

---

## 7. Network Layer — Shopify API

### 7.1 API Authentication (Path B — Admin REST)

> ⚠️ This sends credentials in every HTTP header. Acceptable for academic use only.

```kotlin
// data/remote/interceptor/AuthInterceptor.kt
class AuthInterceptor @Inject constructor(
    @Named("shopify_api_key") private val apiKey: String,
    @Named("shopify_password") private val password: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header("Authorization", Credentials.basic(apiKey, password))
            .build()
        return chain.proceed(request)
    }
}
```

### 7.2 URL Scheme (Path B)

```
https://{apikey}:{password}@{hostname}/admin/api/{version}/{resource}.json
```

Store in `local.properties` → exposed via `BuildConfig`:
```
SHOPIFY_API_KEY=...
SHOPIFY_PASSWORD=...
SHOPIFY_HOSTNAME=...
SHOPIFY_API_VERSION=2024-01
```

For Path A (Apollo/GraphQL), use the **Storefront API** public token — safe to bundle.

### 7.3 Retrofit Service Rules

```kotlin
// ✅ Suspend only — no Call<T>, no blocking
interface ProductApiService {
    @GET("products.json")
    suspend fun getProducts(
        @Query("vendor")       vendor: String?  = null,
        @Query("product_type") type: String?    = null,
        @Query("limit")        limit: Int       = 20,
        @Query("page_info")    pageInfo: String? = null,
    ): ProductsResponseDto

    @GET("products/{id}.json")
    suspend fun getProductById(@Path("id") id: Long): ProductResponseDto
}
```

### 7.4 Interceptor Stack (execution order)

```
1. AuthInterceptor    → adds Basic Auth header to every request
2. ErrorInterceptor   → maps HTTP codes to typed domain exceptions
3. LoggingInterceptor → DEBUG builds only (Level.BODY)
```

```kotlin
// data/remote/interceptor/ErrorInterceptor.kt
class ErrorInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        when (response.code) {
            401        -> throw UnauthorizedException()
            403        -> throw ForbiddenException()
            404        -> throw NotFoundException()
            422        -> throw ValidationException(response.body?.string())
            in 500..599 -> throw ServerException(response.code)
        }
        return response
    }
}
```

---

## 8. Local Storage Rules

### 8.1 Room Entity Rules

```kotlin
// ✅ @Entity classes live in data/local/entity/ ONLY — suffix: Entity
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val variantId: Long,
    val productId: Long,
    val title: String,
    val price: Double,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
)
// ❌ NEVER put @Entity in domain/model/ — domain is pure Kotlin
```

### 8.2 DAO Rules

```kotlin
// ✅ All DAO methods: suspend OR return Flow
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun observeCartItems(): Flow<List<CartItemEntity>>             // hot stream

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(item: CartItemEntity)

    @Delete
    suspend fun delete(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :qty WHERE variantId = :id")
    suspend fun updateQuantity(id: Long, qty: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()
}
```

### 8.3 DataStore — Preferences (simple key-value)

```kotlin
// Use for: selected currency, locale, last-seen filter
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val selectedCurrency: Flow<String> = dataStore.data.map { it[CURRENCY_KEY] ?: "USD" }

    suspend fun setSelectedCurrency(code: String) {
        dataStore.edit { it[CURRENCY_KEY] = code }
    }

    companion object {
        val CURRENCY_KEY = stringPreferencesKey("currency_code")
    }
}
```

### 8.4 Proto DataStore — structured data

```proto
// data/src/main/proto/user_preferences.proto
syntax = "proto3";
option java_package = "com.wearzone.data.local.proto";

message UserPreferences {
    string auth_token    = 1;
    string currency_code = 2;
    bool   is_guest      = 3;
    string locale        = 4;
}
```

Use Proto DataStore for: auth token, user identity state — data that must survive process death with type safety.

---

## 9. Offline-First / SSOT Architecture

Every feature that reads persistent data follows SSOT via the Repository:

```kotlin
// ✅ Repository SSOT pattern — emit local first, refresh from remote, Room re-emits automatically
class ProductRepositoryImpl @Inject constructor(
    private val localDs: IProductLocalDataSource,
    private val remoteDs: IProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IProductRepository {

    override fun observeProducts(brand: String?): Flow<List<Product>> =
        localDs.observeProducts(brand)
            .map { it.map { entity -> entity.toDomain() } }
            .onStart { refreshFromRemote(brand) }

    private suspend fun refreshFromRemote(brand: String?) {
        withContext(ioDispatcher) {
            runCatching { remoteDs.fetchProducts(brand) }
                .onSuccess { dto -> localDs.cacheProducts(dto.map { it.toEntity() }) }
                // Network errors are silent — local cache is the fallback
        }
    }
}
```

**Rules:**
- Room is the single source of truth — the UI always collects from Room's `Flow`, never directly from Retrofit
- Remote fetches update Room; Room's `Flow` re-emits automatically
- Network failure while offline: cached data is served silently
- For write operations (add to cart, place order): write local first, sync remote in background

---

## 10. Compose State Management & Recomposition

### 10.1 Immutable Collections in UiState — MANDATORY

```kotlin
// ✅ CORRECT — ImmutableList prevents unnecessary recomposition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class Success(
    val products: ImmutableList<ProductUiModel>,   // ✅
) : ProductListUiState

// When building the list:
_uiState.value = ProductListUiState.Success(
    products = products.map { it.toUiModel() }.toImmutableList()  // ✅
)

// ❌ BANNED in UiState
data class Success(val products: List<ProductUiModel>)   // ← Compose sees this as unstable
data class Success(val products: Set<ProductUiModel>)    // ← same problem
data class Success(val products: Map<*, *>)              // ← same problem
```

### 10.2 State Hoisting

```kotlin
// ✅ CORRECT — stateless leaf component; all state owned by parent
@Composable
fun QuantitySelector(
    quantity: Int,
    maxQuantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // pure rendering only
}

// ✅ CORRECT — parent passes values and lambdas
@Composable
fun CartItemRow(
    item: CartItemUiModel,
    onIntent: (CartUiIntent) -> Unit,
) {
    QuantitySelector(
        quantity    = item.quantity,
        maxQuantity = item.stockAvailable,
        onIncrease  = { onIntent(CartUiIntent.IncrementQuantity(item.variantId)) },
        onDecrease  = { onIntent(CartUiIntent.DecrementQuantity(item.variantId)) },
    )
}

// ❌ WRONG — local uncontrolled state in a component that should be controlled
@Composable
fun QuantitySelector(...) {
    var count by remember { mutableStateOf(0) }   // ← parent can't observe or reset this
}
```

### 10.3 Intelligent Recomposition

```kotlin
// ✅ Stable UiModels (data class provides equals/hashCode)
data class ProductUiModel(val id: String, val title: String, val price: String)

// ✅ Keys in LazyColumn prevent full recomposition on list change
LazyColumn {
    items(products, key = { it.id }) { product ->
        ProductCard(product = product, onIntent = onIntent)
    }
}

// ✅ derivedStateOf for values computable from existing state
val total by remember(cartItems) {
    derivedStateOf { cartItems.sumOf { it.price * it.quantity } }
}

// ✅ Annotate complex UiState classes explicitly when needed
@Immutable
data class CheckoutUiState(...)
```

### 10.4 Side Effects Rules

```kotlin
// LaunchedEffect(Unit)          — collect UiEffects from Channel (runs once)
LaunchedEffect(Unit) {
    viewModel.uiEffect.collect { effect -> /* navigate, show snackbar */ }
}

// LaunchedEffect(key)           — re-run when key changes
LaunchedEffect(productId) { viewModel.loadDetail(productId) }

// SideEffect                    — post-recomposition non-Compose side effects
SideEffect { analyticsTracker.setCurrentScreen("ProductList") }

// DisposableEffect              — setup/teardown paired resources
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event -> /* ... */ }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}

// ❌ WRONG — business logic inside SideEffect
// ❌ WRONG — LaunchedEffect without a key (always specify a meaningful key)
// ❌ WRONG — navigating inside BlocBuilder equivalent (use UiEffect Channel)
```

---

## 11. Testing Architecture

### 11.1 Test Strategy Per Layer

| Layer        | What to Test                                         | Tools                        | Target  |
|--------------|------------------------------------------------------|------------------------------|---------|
| Domain       | UseCase logic, model transformations                 | JUnit5 + MockK               | 100%    |
| Data         | Repository SSOT logic, mapper correctness, DAO ops   | JUnit5 + MockK + Turbine     | 90%     |
| Presentation | ViewModel state transitions, effect emissions        | JUnit5 + MockK + Turbine     | 95%     |
| UI           | Composable rendering with given UiState              | Compose Test                 | Key screens |

### 11.2 Test Dispatcher Rule

```kotlin
// shared test utility — replaces all @IoDispatcher usages
@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule : TestWatcher() {
    val testDispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
```

Since dispatchers are injected into repositories (not ViewModels), tests substitute them
via constructor injection of `UnconfinedTestDispatcher` — no production code changes needed.

### 11.3 Test Doubles Strategy

```
Fakes  ← preferred for IProductRepository, ICartRepository (deterministic, no magic)
Mocks  ← acceptable for UseCase in ViewModel tests (MockK)
Stubs  ← simple scenarios with fixed return values
```

```kotlin
// ✅ CORRECT — Fake repository (preferred over mock)
class FakeProductRepository : IProductRepository {
    var productsToReturn: List<Product> = emptyList()
    var errorToThrow: Throwable? = null

    override suspend fun getProducts(): Result<List<Product>> =
        errorToThrow?.let { Result.failure(it) } ?: Result.success(productsToReturn)

    override fun observeProducts(brand: String?): Flow<List<Product>> = flow {
        errorToThrow?.let { throw it }
        emit(productsToReturn)
    }
}

// ✅ CORRECT — ViewModel test with Turbine + FakeRepo
@ExtendWith(TestCoroutineExtension::class)
class ProductListViewModelTest {

    private val fakeRepo = FakeProductRepository()
    private val getProductsUseCase = GetProductsUseCase(fakeRepo)

    @Test
    fun `when products load successfully, state transitions to Success`() = runTest {
        fakeRepo.productsToReturn = listOf(TestFixtures.product)
        val viewModel = ProductListViewModel(getProductsUseCase)

        viewModel.uiState.test {
            assertEquals(ProductListUiState.Loading, awaitItem())
            val success = awaitItem() as ProductListUiState.Success
            assertEquals(1, success.products.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when products empty, state is Empty`() = runTest {
        fakeRepo.productsToReturn = emptyList()
        val viewModel = ProductListViewModel(getProductsUseCase)

        viewModel.uiState.test {
            skipItems(1) // Loading
            assertEquals(ProductListUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when network fails, state is Error`() = runTest {
        fakeRepo.errorToThrow = IOException("No network")
        val viewModel = ProductListViewModel(getProductsUseCase)

        viewModel.uiState.test {
            skipItems(1)
            assertIs<ProductListUiState.Error>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### 11.4 Channel Effect Testing

```kotlin
@Test
fun `clicking product emits NavigateToDetail effect`() = runTest {
    val viewModel = ProductListViewModel(getProductsUseCase)

    viewModel.uiEffect.test {
        viewModel.handleIntent(ProductListUiIntent.OnProductClicked("p_123"))
        val effect = awaitItem()
        assertIs<ProductListUiEffect.NavigateToDetail>(effect)
        assertEquals("p_123", (effect as ProductListUiEffect.NavigateToDetail).productId)
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 11.5 Test File Locations

```
data/src/test/
├── repository/           ← ProductRepositoryImplTest, CartRepositoryImplTest
├── datasource/           ← ProductRemoteDataSourceTest
└── mapper/               ← ProductDtoMapperTest, CartItemEntityMapperTest

domain/src/test/
├── product/usecase/      ← GetProductsUseCaseTest, SearchProductsUseCaseTest
├── cart/usecase/         ← AddToCartUseCaseTest, RemoveFromCartUseCaseTest
└── checkout/usecase/     ← PlaceOrderUseCaseTest, ApplyCouponUseCaseTest

presentation/src/test/
├── product/list/         ← ProductListViewModelTest
├── cart/                 ← CartViewModelTest
├── checkout/             ← CheckoutViewModelTest
└── auth/login/           ← LoginViewModelTest
```

---

## 12. Naming Conventions — STRICTLY ENFORCED

| Type                       | Convention          | Example                                    |
|----------------------------|---------------------|--------------------------------------------|
| Kotlin files               | `PascalCase`        | `ProductListViewModel.kt`                  |
| Composable functions       | `PascalCase`        | `ProductCard()`, `AppButton()`             |
| Packages                   | `lowercase`         | `com.wearzone.data.remote.dto`             |
| Variables / functions      | `camelCase`         | `loadProducts()`, `uiState`                |
| Constants                  | `UPPER_SNAKE_CASE`  | `AUTH_TOKEN_KEY`, `MAX_COD_AMOUNT`         |
| Room tables                | `snake_case`        | `cart_items`, `wishlist_items`             |
| Navigation routes          | Suffix `Route`      | `ProductDetailRoute`, `HomeRoute`          |
| DTO classes                | Suffix `Dto`        | `ProductDto`, `CartItemDto`                |
| Room entity classes        | Suffix `Entity`     | `CartItemEntity`, `WishlistItemEntity`     |
| Domain model classes       | No suffix           | `Product`, `CartItem`, `User`             |
| UI model classes           | Suffix `UiModel`    | `ProductUiModel`, `CartItemUiModel`        |
| UiState                    | Suffix `UiState`    | `ProductListUiState`, `CartUiState`        |
| UiIntent                   | Suffix `UiIntent`   | `ProductListUiIntent`, `CartUiIntent`      |
| UiEffect                   | Suffix `UiEffect`   | `ProductListUiEffect`, `CheckoutUiEffect`  |
| UseCase classes            | Suffix `UseCase`    | `GetProductsUseCase`, `AddToCartUseCase`   |
| Repository interfaces      | Prefix `I`          | `IProductRepository`, `ICartRepository`    |
| Repository implementations | Suffix `Impl`       | `ProductRepositoryImpl`                    |
| DataSource interfaces      | Prefix `I`          | `IProductRemoteDataSource`                 |
| DataSource implementations | Suffix `Impl`       | `ProductRemoteDataSourceImpl`              |
| Hilt modules               | Suffix `Module`     | `NetworkModule`, `RepositoryModule`        |
| Test files                 | Suffix `Test`       | `ProductListViewModelTest`                 |

---

## 13. Feature-Specific Business Rules

### 13.1 Authentication

- **Guest users** can: browse products, view categories, search, view product detail
- **Guest users** cannot: add to cart, add to wishlist, checkout — redirect to Login
- Firebase Auth is the identity provider (Email/Password + Social)
- Auth token stored in Proto DataStore — never `SharedPreferences`
- On login success: navigate to Home, pop entire auth graph from back stack
- Session persistence: read auth token from Proto DataStore in `NavHost` to determine `startDestination`

### 13.2 Product Catalog

- Products fetched from: `GET /products.json` (REST) or Storefront GraphQL `products` query
- Product detail: `GET /products/{id}.json`
- Reviews: fetched from Shopify product `metafields` or a dedicated endpoint
- **Offline-first**: products cached in Room, served from cache on network failure
- All images via Coil `AsyncImage` — placeholder shimmer + error fallback required

### 13.3 Search, Filter & Sort

- Search input is **debounced 300ms** using `.debounce(300).flatMapLatest { query -> searchUseCase(query) }`
- Active filters and sort selection held in ViewModel state — NOT in NavGraph arguments
- Filter options: Main Category, Sub-Category, Brand
- Sort options: `PRICE_ASC`, `PRICE_DESC`, `BEST_SELLER`, `SUB_CATEGORY`
- Filter/sort applied server-side via query params when online; client-side on cached Room data when offline

### 13.4 Cart Business Rules

- Cart is **local-first** — stored in Room, no Shopify Cart API required
- `maxQuantity` per item = `inventory_quantity` from Shopify product response; cached in `CartItemEntity`
- Increment blocked at UI level when `item.quantity >= item.maxQuantity`
- Checkout total recalculated reactively: `combine(cartItemsFlow, discountFlow) { items, discount -> ... }`
- Cart item count badge: exposed as `StateFlow<Int>` from CartViewModel, collected by bottom nav

### 13.5 Wishlist Rules

- Stored in Room — authenticated users only
- Redirect unauthenticated user to Login when "Add to Wishlist" is tapped
- Toggle: if item exists → remove; if not → add (single `ToggleWishlistUseCase`)

### 13.6 Checkout Rules

- Step 1: Address selection / entry
- Step 2: Apply coupon code (validate via API)
- Step 3: Select payment method
- Cash on Delivery blocked when `orderTotal > BuildConfig.MAX_COD_AMOUNT`
- Online payment: redirect to Shopify checkout URL or payment gateway WebView
- On order success: clear cart Room table, emit `CheckoutUiEffect.NavigateToOrderConfirmation`
- Order confirmation email sent by Shopify backend — not a client responsibility
- **Confirmation dialog required before submitting the final order**

### 13.7 Address Rules

- Required fields: recipient name, mobile number, street, city, country
- Country list fetched from API, cached in Room
- Street field uses Google Places / HERE Maps text autocomplete
- Coordinates validated before submission

### 13.8 Account Rules

- Personalized greeting, last 5 orders, last 4 wishlist items
- Currency selector: fetch rates from external API; persist selection in DataStore Preferences
- Confirmation dialog required before logout

### 13.9 Destructive Action Contract — HARD REQUIREMENT

Every destructive/negative action MUST emit a `ShowConfirmDialog` effect first.
Execution happens only after the user explicitly confirms.

Destructive actions list:
- Delete a cart item
- Clear the entire cart
- Remove from wishlist
- Logout
- Cancel an order

```kotlin
// ✅ CORRECT — ViewModel emits dialog effect; execution is a separate intent
private fun requestRemoveCartItem(itemId: String) {
    viewModelScope.launch {
        _uiEffect.send(
            CartUiEffect.ShowConfirmDialog(
                message   = "Remove this item from your bag?",
                onConfirm = CartUiIntent.ConfirmRemoveItem(itemId),
            )
        )
    }
}

// Composable handles the dialog and sends back the confirm intent
is CartUiEffect.ShowConfirmDialog -> {
    showDialog(
        message   = effect.message,
        onConfirm = { viewModel.handleIntent(effect.onConfirm) },
        onDismiss = { },
    )
}
```

---

## 14. UI & Design System Rules

### 14.1 Tokens — AppColors

```kotlin
// presentation/common/theme/AppColors.kt
object AppColors {
    val Primary        = Color(0xFF1A1A2E)   // Deep navy — WearZone brand
    val Accent         = Color(0xFFE94560)   // Coral red
    val Surface        = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF5F5F5)
    val Background     = Color(0xFFFAFAFA)
    val OnPrimary      = Color(0xFFFFFFFF)
    val TextPrimary    = Color(0xFF1C1C1E)
    val TextSecondary  = Color(0xFF8E8E93)
    val Success        = Color(0xFF34C759)
    val Error          = Color(0xFFFF3B30)
    val Divider        = Color(0xFFE5E5EA)
}
// ❌ NEVER hardcode Color(0xFF...) inside a Composable
```

### 14.2 Tokens — AppTypography

```kotlin
// ❌ NEVER hardcode TextStyle inline in a Composable
val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    titleMedium   = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyMedium    = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp),
)
```

### 14.3 Shared Component Catalogue — Use These, Never Reinvent

```
AppButton(text, onClick, modifier, isLoading, enabled, variant: Primary|Secondary|Destructive)
AppTextField(value, onValueChange, label, isError, errorMessage, modifier)
AppLoadingOverlay()
AppErrorWidget(message, onRetry)
AppSnackbar — via SnackbarHostState in Scaffold
ConfirmationDialog(title, message, confirmLabel, onConfirm, onDismiss) ← REQUIRED for destructive actions
EmptyStateWidget(title, subtitle, illustrationRes)
ProductCard(product, onProductClick, onWishlistToggle)
LoadingShimmer(modifier, shape)                                          ← skeleton screens
```

### 14.4 Accessibility Checklist

- Every `Image` and `AsyncImage` must have a non-empty `contentDescription`
- Every tappable element: minimum touch target `48.dp × 48.dp`
- Apply `semantics { role = Role.Button }` to custom interactive elements
- Color contrast ratio ≥ 4.5:1 for all text on background

---

## 15. Code Quality Rules

### 15.1 Prohibited Patterns

| Pattern                          | Use Instead                             |
|----------------------------------|-----------------------------------------|
| `TODO()` in production code      | Resolve before commit                   |
| `println()` / `Log.d()`          | `Timber.d()` / `Timber.e()`            |
| Empty `catch` blocks             | At minimum log with Timber              |
| `!!` without a comment           | Use `?: return`, `let`, or `requireNotNull` with message |
| Functions > 40 lines             | Extract private functions               |
| Files > 300 lines                | Extract components or helpers           |
| Hardcoded strings in Composables | `stringResource(R.string.xxx)`          |
| Hardcoded dimensions             | `dimensionResource` or theme tokens     |
| Hardcoded colors                 | `AppColors.*` only                      |

### 15.2 Logging

```kotlin
// Plant in Application class — DEBUG builds only
if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

// Usage
Timber.d("Cart loaded: ${items.size} items")
Timber.e(exception, "Failed to place order")
Timber.w("Coupon '$code' was invalid")
```

---

## 16. Scope & Safety Rules

- ❌ Do NOT modify files outside the scope of the requested task
- ❌ Do NOT rename existing files unless explicitly asked
- ❌ Do NOT refactor working code while implementing a new feature
- ❌ Do NOT add new Gradle dependencies without explicit approval
- ❌ Do NOT change module boundaries without a discussion
- ✅ Work on ONE feature or layer at a time
- ✅ Show the list of files to create/change BEFORE writing code
- ✅ Map every feature implementation to its section in `Shopify_Project_Specs.pdf`
- ✅ Every ViewModel must have a test file before the feature is marked done
- ✅ Every UseCase must reach 100% test coverage before a PR is raised

---

## 17. Documentation Rules

### 17.1 After Completing Any Task

1. Update `README.md` — mark feature status as ✅
2. Write a summary to `docs/ai/YYYY-MM-DD-task-name.md`
3. New architecture decision made → `docs/adr/ADR-XXX.md`
4. Confirm with the team before closing the task

### 17.2 Quick Reference

| I need to know...                    | Location                                          |
|--------------------------------------|---------------------------------------------------|
| Full feature requirements            | `Shopify_Project_Specs.pdf`                       |
| Module dependency rules              | §2.1                                              |
| Complete folder structure            | §2.2                                              |
| MVI contract (State/Intent/Effect)   | §3                                                |
| ViewModel structure & Channel effect | §3.3                                              |
| Hilt modules & scopes                | §4                                                |
| KSP configuration                    | §4.6                                              |
| Type-safe navigation setup           | §5                                                |
| Dispatcher model (why VM has none)   | §6.1                                              |
| Flow hot/cold decision               | §6.2                                              |
| Shopify API security warning         | Security Notice (top of file)                     |
| Retrofit interceptor stack           | §7.4                                              |
| Offline-first SSOT pattern           | §9                                                |
| ImmutableList rule                   | §10.1                                             |
| State hoisting rules                 | §10.2                                             |
| Side effects cheat sheet             | §10.4                                             |
| Testing strategy + Channel tests     | §11                                               |
| Destructive action contract          | §13.9                                             |
| Shared component catalogue           | §14.3                                             |

---

*Last updated: June 2026 · JETS Mobile Lab — Android Track · WearZone v1.0*