# SHOPIFY APP — Android · AI Agent Rules & Architecture Contract

> **MANDATORY:** Read this file **completely** before writing a single line of code.
> Every rule below is non-negotiable and applies to every AI agent working on this project.
> Violations will be rejected in code review without exception.

---

## 0. Project Identity

| Field           | Value                                                        |
|-----------------|--------------------------------------------------------------|
| **App Name**    | Shopify App (E-Commerce)                                     |
| **Platform**    | Android (Kotlin + Jetpack Compose)                           |
| **Domain**      | E-Commerce — Shopify-backed product browsing, cart, checkout |
| **Users**       | Guest · Authenticated Customer                               |
| **Backend**     | Shopify REST Admin API + Firebase Auth                       |
| **Reference**   | `Shopify_Project_Specs.pdf` ← READ THIS FIRST               |
| **Design Ref**  | https://pocket-shop-style.lovable.app                        |

---

## 1. Confirmed Technology Stack

> ⚠️ Do NOT suggest or use any technology not listed below without explicit approval.

### 1.1 Core Language & UI

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Language**             | Kotlin (latest stable)                 | 100% Kotlin — no Java files                        |
| **UI Toolkit**           | Jetpack Compose (BOM latest stable)    | No XML layouts anywhere                            |
| **Min SDK**              | 26 (Android 8.0)                       |                                                    |
| **Target SDK**           | Latest stable                          |                                                    |

### 1.2 Architecture & DI

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Architecture**         | Clean Architecture + MVI               | Strict layer separation, see §2                    |
| **Dependency Injection** | Hilt                                   | Single DI framework — no Koin, no manual DI        |
| **ViewModel**            | `androidx.lifecycle:lifecycle-viewmodel-compose` | One VM per screen                    |
| **State Management**     | `StateFlow` + `UiState` sealed class   | No LiveData, no mutableStateOf at VM level         |

### 1.3 Async & Reactive

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Async**                | Kotlin Coroutines                      | `viewModelScope`, `suspend fun`, structured concurrency |
| **Reactive Streams**     | Kotlin Flow (cold) + StateFlow/SharedFlow (hot) | No RxJava                              |
| **Dispatchers**          | `Dispatchers.IO` (network/DB), `Dispatchers.Default` (CPU), `Dispatchers.Main` (UI) | Injected via `CoroutineDispatcher` |
| **Exception Handling**   | `CoroutineExceptionHandler` + `runCatching` + `Result<T>` | See §6                      |

### 1.4 Network

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **HTTP Client**          | Retrofit 2 (latest)                    | With OkHttp 4                                      |
| **Serialization**        | Kotlin Serialization (`kotlinx.serialization`) | No Gson, no Moshi                        |
| **Retrofit Converter**   | `retrofit2-kotlinx-serialization-converter` |                                             |
| **Interceptors**         | OkHttp `Interceptor` (Auth, Logging, Error) | See §7                                    |

### 1.5 Local Storage

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Database**             | Room (latest)                          | All DAOs are `suspend fun` or `Flow<>`             |
| **Preferences**          | DataStore Preferences                  | For simple key-value (auth token, currency, locale)|
| **Typed Preferences**    | Proto DataStore                        | For structured data (UserPreferences proto)        |

### 1.6 Navigation

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Navigation**           | Jetpack Navigation Compose (latest)    | Single `NavHost`, typed routes, back stack handling|
| **Deep Links**           | Declared in `NavGraph` + `AndroidManifest.xml` |                                          |

### 1.7 Images & Media

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Image Loading**        | Coil 3 (Compose)                       | `AsyncImage` everywhere                            |

### 1.8 Testing

| Concern                  | Technology                             | Notes                                              |
|--------------------------|----------------------------------------|----------------------------------------------------|
| **Unit Testing**         | JUnit 5 + Kotlin Coroutines Test       | `runTest`, `TestCoroutineScheduler`                |
| **Mocking**              | MockK                                  | No Mockito                                         |
| **Flow Testing**         | `Turbine`                              | For Flow/StateFlow assertions                      |
| **UI Testing**           | Compose Testing (`composeTestRule`)    |                                                    |
| **Test Doubles**         | Fakes preferred over Mocks for repos   | See §11                                            |

### ❌ Explicitly Forbidden Technologies

- `LiveData` — **banned** (use `StateFlow`)
- `RxJava` / `RxKotlin` — **banned** (use Kotlin Flow)
- `Koin` — **banned** (use Hilt)
- XML layouts (`layout/*.xml`) — **banned** (use Compose)
- `Gson` — **banned** (use `kotlinx.serialization`)
- `Moshi` — **banned** (use `kotlinx.serialization`)
- `ViewModel.viewModelScope` launching raw threads — **banned** (use coroutines)
- Any global `object` with mutable state — **banned** (inject via Hilt)
- `findViewById` — **banned** (Compose-only project)

---

## 2. Module Structure — STRICTLY ENFORCED

The project is split into **four Gradle modules**. Each module has a dedicated `build.gradle.kts`.

```
root/
├── app/                    ← Android module: Hilt setup, NavGraph, NavHost, Application class
├── data/                   ← Android module: RepoImpl, Room, Retrofit, DTOs/Models
├── domain/                 ← Pure Kotlin module: UseCases, Entities, Repo interfaces
└── presentation/           ← Android module: Compose screens, ViewModels, Components
```

### 2.1 Module Dependency Graph

```
app  ──depends on──►  presentation
app  ──depends on──►  data           (for Hilt bindings only)
presentation  ──────►  domain
data          ──────►  domain
domain        ──────►  (nothing — pure Kotlin)
```

> **Rule:** `domain` must NEVER import `data` or `presentation`. `presentation` must NEVER import `data`.
> The only module that imports both `data` and `presentation` is `app` — solely for Hilt module wiring.

### 2.2 Feature Folder Structure (inside each module)

```
data/src/main/kotlin/com.yourpackage.data/
├── local/
│   ├── db/                        ← RoomDatabase class
│   ├── dao/                       ← DAOs per entity
│   ├── entity/                    ← Room @Entity classes
│   └── datasource/                ← LocalDataSource interfaces + impl
├── remote/
│   ├── api/                       ← Retrofit @Service interfaces
│   ├── dto/                       ← @Serializable DTO classes (fromDto mappers live here)
│   └── datasource/                ← RemoteDataSource interfaces + impl
├── repository/                    ← RepositoryImpl classes
└── di/                            ← Hilt @Module classes for data layer

domain/src/main/kotlin/com.yourpackage.domain/
├── auth/
│   ├── model/                     ← Pure Kotlin entities (User, AuthToken)
│   ├── repository/                ← IAuthRepository interface
│   └── usecase/                   ← LoginUseCase, RegisterUseCase, etc.
├── product/
│   ├── model/                     ← Product, ProductDetail, Review, Brand, Category
│   ├── repository/                ← IProductRepository
│   └── usecase/                   ← GetProductsUseCase, GetProductDetailUseCase, etc.
├── cart/
│   ├── model/                     ← CartItem, Cart
│   ├── repository/                ← ICartRepository
│   └── usecase/                   ← AddToCartUseCase, RemoveFromCartUseCase, UpdateCartUseCase
├── wishlist/
│   ├── model/                     ← WishlistItem
│   ├── repository/                ← IWishlistRepository
│   └── usecase/                   ← AddToWishlistUseCase, RemoveFromWishlistUseCase
├── checkout/
│   ├── model/                     ← Order, Coupon, PaymentMethod, Address
│   ├── repository/                ← ICheckoutRepository, IAddressRepository
│   └── usecase/                   ← PlaceOrderUseCase, ApplyCouponUseCase, ValidateAddressUseCase
├── account/
│   ├── model/                     ← UserProfile, OrderHistory, CurrencyRate
│   ├── repository/                ← IAccountRepository, ICurrencyRepository
│   └── usecase/                   ← GetProfileUseCase, GetExchangeRatesUseCase
└── common/
    ├── result/                    ← sealed class DataResult<T> { Success, Error, Loading }
    └── error/                     ← DomainError sealed class hierarchy

presentation/src/main/kotlin/com.yourpackage.presentation/
├── auth/
│   ├── login/
│   │   ├── LoginScreen.kt         ← @Composable root screen
│   │   ├── LoginViewModel.kt
│   │   └── components/            ← LoginForm, SocialLoginButtons, etc.
│   └── register/
│       ├── RegisterScreen.kt
│       ├── RegisterViewModel.kt
│       └── components/
├── home/
│   ├── HomeScreen.kt
│   ├── HomeViewModel.kt
│   └── components/               ← CategoryRow, BrandRow, FeaturedProducts
├── product/
│   ├── list/
│   │   ├── ProductListScreen.kt
│   │   ├── ProductListViewModel.kt
│   │   └── components/           ← ProductCard, FilterSheet, SortSheet
│   └── detail/
│       ├── ProductDetailScreen.kt
│       ├── ProductDetailViewModel.kt
│       └── components/           ← ImageCarousel, SizeSelector, ReviewCard
├── search/
│   ├── SearchScreen.kt
│   ├── SearchViewModel.kt
│   └── components/
├── cart/
│   ├── CartScreen.kt
│   ├── CartViewModel.kt
│   └── components/               ← CartItem, PriceSummary
├── wishlist/
│   ├── WishlistScreen.kt
│   ├── WishlistViewModel.kt
│   └── components/
├── checkout/
│   ├── CheckoutScreen.kt
│   ├── CheckoutViewModel.kt
│   └── components/               ← AddressCard, PaymentSelector, CouponField, OrderSummary
├── account/
│   ├── AccountScreen.kt
│   ├── AccountViewModel.kt
│   └── components/               ← OrderHistoryCard, WishlistPreview
├── address/
│   ├── AddressScreen.kt
│   ├── AddressViewModel.kt
│   └── components/
└── common/
    ├── components/               ← Shared Composables (AppButton, AppTextField, LoadingOverlay, etc.)
    ├── navigation/               ← Route sealed class, NavExtensions
    └── theme/                    ← AppTheme, AppColors, AppTypography, AppShapes

app/src/main/kotlin/com.yourpackage/
├── ShopifyApplication.kt         ← @HiltAndroidApp Application class
├── MainActivity.kt               ← Single Activity, @AndroidEntryPoint, setContent { AppNavHost() }
└── di/
    ├── NetworkModule.kt          ← @Module Retrofit, OkHttp, Interceptors
    ├── DatabaseModule.kt         ← @Module Room DB, DAOs
    ├── RepositoryModule.kt       ← @Binds IRepo → RepoImpl
    ├── DataSourceModule.kt       ← @Binds IDataSource → DataSourceImpl
    ├── DataStoreModule.kt        ← @Module DataStore, ProtoDataStore
    └── DispatcherModule.kt       ← @Module CoroutineDispatchers
```

---

## 3. Architecture Rules — NEVER VIOLATE

### 3.1 MVI Pattern — STRICTLY ENFORCED

Every screen follows the **Model → View → Intent** cycle:

```
UI Event (Intent)  ──►  ViewModel.handleIntent()  ──►  UseCase
                                                         │
UiState (StateFlow) ◄── ViewModel emits new state  ◄────┘
```

### 3.2 UiState Structure — Sealed Interface Per Screen

```kotlin
// ✅ CORRECT — sealed interface with specific state types
sealed interface ProductListUiState {
    data object Loading : ProductListUiState
    data class Success(
        val products: List<ProductUiModel>,
        val isLoadingMore: Boolean = false,
    ) : ProductListUiState
    data object Empty : ProductListUiState
    data class Error(val message: String) : ProductListUiState
}

// ✅ CORRECT — UI events as sealed interface
sealed interface ProductListUiEvent {
    data class OnProductClicked(val productId: String) : ProductListUiEvent
    data object OnRetry : ProductListUiEvent
    data class OnFilterApplied(val filter: ProductFilter) : ProductListUiEvent
    data class OnSortSelected(val sort: SortOption) : ProductListUiEvent
}

// ✅ CORRECT — one-shot effects as sealed interface (navigation, snackbar)
sealed interface ProductListUiEffect {
    data class NavigateToDetail(val productId: String) : ProductListUiEffect
    data class ShowError(val message: String) : ProductListUiEffect
}

// ❌ WRONG — do NOT use a single data class with all nullable fields
// ❌ WRONG — do NOT mix navigation logic inside UiState
```

### 3.3 ViewModel Structure — STRICTLY ENFORCED

```kotlin
@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val getFiltersUseCase: GetFiltersUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,  // injected
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductListUiState>(ProductListUiState.Loading)
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    // One-shot effects: SharedFlow with replay=0
    private val _uiEffect = MutableSharedFlow<ProductListUiEffect>(extraBufferCapacity = 1)
    val uiEffect: SharedFlow<ProductListUiEffect> = _uiEffect.asSharedFlow()

    init { loadProducts() }

    fun handleEvent(event: ProductListUiEvent) {
        when (event) {
            is ProductListUiEvent.OnProductClicked -> navigateToDetail(event.productId)
            is ProductListUiEvent.OnRetry          -> loadProducts()
            is ProductListUiEvent.OnFilterApplied  -> applyFilter(event.filter)
            is ProductListUiEvent.OnSortSelected   -> applySort(event.sort)
        }
    }

    private fun loadProducts() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = ProductListUiState.Loading
            getProductsUseCase()
                .onSuccess { products ->
                    _uiState.value = if (products.isEmpty())
                        ProductListUiState.Empty
                    else
                        ProductListUiState.Success(products.map { it.toUiModel() })
                }
                .onFailure { error ->
                    _uiState.value = ProductListUiState.Error(error.message ?: "Unknown error")
                }
        }
    }

    private fun navigateToDetail(productId: String) {
        viewModelScope.launch {
            _uiEffect.emit(ProductListUiEffect.NavigateToDetail(productId))
        }
    }
}
```

### 3.4 Composable Screen Structure — STRICTLY ENFORCED

```kotlin
// ✅ CORRECT — screen composable collects state and hoists events up
@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // One-shot effects — always in LaunchedEffect with lifecycle awareness
    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ProductListUiEffect.NavigateToDetail -> onNavigateToDetail(effect.productId)
                is ProductListUiEffect.ShowError        -> { /* show snackbar */ }
            }
        }
    }

    ProductListContent(
        uiState = uiState,
        onEvent = viewModel::handleEvent,
    )
}

// ✅ CORRECT — stateless content composable (state hoisting)
@Composable
private fun ProductListContent(
    uiState: ProductListUiState,
    onEvent: (ProductListUiEvent) -> Unit,
) {
    when (uiState) {
        is ProductListUiState.Loading -> AppLoadingOverlay()
        is ProductListUiState.Success -> ProductGrid(products = uiState.products, onEvent = onEvent)
        is ProductListUiState.Empty   -> EmptyStateWidget()
        is ProductListUiState.Error   -> AppErrorWidget(message = uiState.message, onRetry = { onEvent(ProductListUiEvent.OnRetry) })
    }
}

// ❌ WRONG — do NOT call ViewModel methods directly from nested composables
// ❌ WRONG — do NOT hoist state inside components — hoist to the screen level
// ❌ WRONG — do NOT use LaunchedEffect for state-driven UI — use 'when (uiState)' directly
```

### 3.5 Layer Violation Rules — ZERO TOLERANCE

| Rule                                      | Violation Example                             |
|-------------------------------------------|-----------------------------------------------|
| **Domain has NO Android imports**         | `import android.*` in domain module → REJECT  |
| **Domain has NO Retrofit/Room imports**   | `@Entity` inside domain model → REJECT        |
| **Presentation has NO data imports**      | `import data.repository.*` in ViewModel → REJECT |
| **ViewModel has NO UseCase-skipping**     | Direct repo call from VM → REJECT             |
| **UseCase does ONE thing**                | UseCase with 2+ public methods → REJECT       |
| **DTO never reaches Presentation**        | `ProductDto` in ViewModel → REJECT            |
| **Entity never enters Data DTOs**         | `Product` (domain) imported in `ProductDto` → REJECT |

### 3.6 SOLID Checklist — Before Writing Any Class

- **S (Single Responsibility):** Does this class do exactly ONE thing?
- **O (Open/Closed):** Is the repository an interface (open for extension, closed for change)?
- **L (Liskov):** Can `FakeProductRepository` replace `ProductRepositoryImpl` without breaking callers?
- **I (Interface Segregation):** Is `IProductRepository` only about products (no cart methods)?
- **D (Dependency Inversion):** Does `ProductListViewModel` depend on `GetProductsUseCase` (abstraction), not `ProductRepositoryImpl`?

### 3.7 DRY Checklist

- Mapper logic exists in ONE place only (DTO → Entity in data layer; Entity → UiModel in presentation layer)
- String resources in `strings.xml` — never hardcoded
- Colors and dimensions in theme — never hardcoded
- API base URL in a single constant — never duplicated

---

## 4. Hilt Dependency Injection — STRICTLY ENFORCED

### 4.1 Application & Activity

```kotlin
// app/ShopifyApplication.kt
@HiltAndroidApp
class ShopifyApplication : Application()

// app/MainActivity.kt
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopifyTheme {
                AppNavHost()
            }
        }
    }
}
```

### 4.2 Hilt Module Rules

```kotlin
// ✅ CORRECT — always use @Binds to bind interface to implementation
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): IProductRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl
    ): ICartRepository
}

// ✅ CORRECT — @Provides for third-party or constructed objects
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.SHOPIFY_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
```

### 4.3 Coroutine Dispatcher Injection

```kotlin
// app/di/DispatcherModule.kt
@Qualifier @Retention(BINARY) annotation class IoDispatcher
@Qualifier @Retention(BINARY) annotation class DefaultDispatcher
@Qualifier @Retention(BINARY) annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides @IoDispatcher      fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides @DefaultDispatcher fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    @Provides @MainDispatcher    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
```

### 4.4 Hilt Scopes

| Scope                | Use For                                          |
|----------------------|--------------------------------------------------|
| `@Singleton`         | Retrofit, OkHttpClient, Room DB, DataStore       |
| `@ViewModelScoped`   | Objects that should live with a single ViewModel |
| `@ActivityScoped`    | Objects shared across fragments/composables in one activity |

### 4.5 ViewModel Injection

```kotlin
// ✅ CORRECT
@HiltViewModel
class CartViewModel @Inject constructor(
    private val addToCartUseCase: AddToCartUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val updateQuantityUseCase: UpdateCartQuantityUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel()

// In Composable — always use hiltViewModel()
@Composable
fun CartScreen(viewModel: CartViewModel = hiltViewModel())

// ❌ WRONG — never instantiate ViewModel manually
val viewModel = CartViewModel(...)
```

---

## 5. Navigation — Jetpack Navigation Compose

### 5.1 NavHost Setup (in `app` module)

```kotlin
// presentation/common/navigation/AppNavHost.kt
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {
        // Auth graph
        navigation(startDestination = Screen.Login.route, route = "auth") {
            composable(Screen.Login.route)    { LoginScreen(onNavigateToHome = { navController.navigate(Screen.Home.route) { popUpTo("auth") { inclusive = true } } }) }
            composable(Screen.Register.route) { RegisterScreen(onNavigateBack = { navController.popBackStack() }) }
        }

        // Main graph
        composable(Screen.Home.route)                                { HomeScreen(navController) }
        composable(Screen.ProductList.route)                         { ProductListScreen(navController) }
        composable("${Screen.ProductDetail.route}/{productId}")       { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            ProductDetailScreen(productId = productId, onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.Cart.route)      { CartScreen(navController) }
        composable(Screen.Wishlist.route)  { WishlistScreen(navController) }
        composable(Screen.Checkout.route)  { CheckoutScreen(navController) }
        composable(Screen.Account.route)   { AccountScreen(navController) }
        composable(Screen.Search.route)    { SearchScreen(navController) }
        composable(Screen.Address.route)   { AddressScreen(navController) }
    }
}
```

### 5.2 Route Definitions

```kotlin
// presentation/common/navigation/Screen.kt
sealed class Screen(val route: String) {
    data object Home          : Screen("home")
    data object Login         : Screen("login")
    data object Register      : Screen("register")
    data object ProductList   : Screen("product_list")
    data object ProductDetail : Screen("product_detail")
    data object Cart          : Screen("cart")
    data object Wishlist      : Screen("wishlist")
    data object Checkout      : Screen("checkout")
    data object Account       : Screen("account")
    data object Search        : Screen("search")
    data object Address       : Screen("address")
}
```

### 5.3 Navigation Rules

| Rule                                                    | Detail                                              |
|---------------------------------------------------------|-----------------------------------------------------|
| **Single NavController**                                | Pass down as parameter — never access globally      |
| **Back stack management**                               | Use `popUpTo` with `inclusive = true` for auth flows|
| **Navigation in ViewModel effects**                     | Emit `UiEffect` → collect in screen → call navController |
| **Arguments**                                           | Pass IDs only — never pass objects through nav args |
| **Deep links**                                          | Declared in manifest + NavGraph                     |
| **Auth guard**                                          | Check auth state in NavHost before composable enters |

---

## 6. Kotlin Coroutines & Flows — RULES

### 6.1 Coroutine Scope Rules

```kotlin
// ✅ ViewModels — use viewModelScope ONLY
viewModelScope.launch(ioDispatcher) { ... }

// ✅ Repository — no own scope; use suspend functions and receive scope from caller
suspend fun getProducts(): Result<List<Product>>

// ✅ DataSources — same as Repository (suspend or Flow return types)

// ❌ WRONG — GlobalScope is BANNED
GlobalScope.launch { ... }

// ❌ WRONG — CoroutineScope(Dispatchers.IO) created inside a class without lifecycle awareness
```

### 6.2 Hot vs Cold Flows — RULES

| Flow Type        | Use Case                                               | Backpressure Strategy         |
|------------------|--------------------------------------------------------|-------------------------------|
| `Flow<T>` (cold) | Repository returning DB data / API response once       | Collected by ViewModel        |
| `StateFlow<T>`   | UI state exposed from ViewModel (always has a value)   | Latest value replayed         |
| `SharedFlow<T>`  | One-shot effects (navigation, snackbar)                | `replay = 0`, `extraBufferCapacity = 1` |

```kotlin
// ✅ CORRECT — Room DAO returns cold Flow; ViewModel converts to StateFlow
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun observeCartItems(): Flow<List<CartItemEntity>>
}

// In ViewModel:
val cartUiState: StateFlow<CartUiState> = cartRepository
    .observeCart()
    .map { items -> CartUiState.Success(items.map { it.toUiModel() }) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CartUiState.Loading,
    )
```

### 6.3 Flow Operators — Preferred

```kotlin
// Use these operators — NOT manual loop + emit
.map { ... }           // transform each item
.filter { ... }        // filter items
.catch { e -> ... }    // handle errors in flow chain
.onStart { ... }       // emit initial loading state
.combine(otherFlow) { a, b -> ... }   // combine two live streams
.flatMapLatest { ... } // cancel previous and switch to new flow (search queries)
.debounce(300)         // for search input throttling
.distinctUntilChanged()// avoid duplicate emissions
```

### 6.4 Exception Handling — RULES

```kotlin
// ✅ CORRECT — in suspend functions use Result<T>
suspend fun getProducts(): Result<List<Product>> = runCatching {
    val response = remoteDataSource.fetchProducts()
    response.products.map { it.toDomain() }
}

// ✅ CORRECT — in Flow chains use .catch
fun observeProducts(): Flow<List<Product>> = localDataSource
    .observeProducts()
    .catch { e -> emit(emptyList()) }

// ✅ CORRECT — in ViewModel use CoroutineExceptionHandler for launch
private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
    viewModelScope.launch { _uiEffect.emit(ProductListUiEffect.ShowError(throwable.message ?: "Error")) }
}

viewModelScope.launch(ioDispatcher + exceptionHandler) { ... }

// ❌ WRONG — bare try/catch swallowing exceptions silently
try { ... } catch (e: Exception) { /* empty */ }
```

---

## 7. Network Layer Rules

### 7.1 Shopify API Authentication

```kotlin
// data/remote/interceptor/AuthInterceptor.kt
class AuthInterceptor @Inject constructor(
    private val apiKey: String,    // injected from BuildConfig
    private val password: String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val credential = Credentials.basic(apiKey, password)
        val request = chain.request().newBuilder()
            .header("Authorization", credential)
            .build()
        return chain.proceed(request)
    }
}
```

### 7.2 API URL Pattern

All Shopify REST calls follow this exact URI:
```
https://{apikey}:{password}@{hostname}/admin/api/{version}/{resource}.json
```

Store in `BuildConfig`:
```
SHOPIFY_API_KEY=...
SHOPIFY_PASSWORD=...
SHOPIFY_HOSTNAME=...
SHOPIFY_API_VERSION=2024-01
```

### 7.3 Retrofit Service Rules

```kotlin
// ✅ CORRECT — suspend functions only, return types always wrapped
interface ProductApiService {
    @GET("products.json")
    suspend fun getProducts(
        @Query("vendor") vendor: String? = null,
        @Query("product_type") productType: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("page_info") pageInfo: String? = null,
    ): ProductsResponseDto

    @GET("products/{id}.json")
    suspend fun getProductById(@Path("id") id: Long): ProductResponseDto
}

// ❌ WRONG — Call<T> wrappers (no Rx, no Call)
// ❌ WRONG — Non-suspend functions in Retrofit service
```

### 7.4 Interceptor Stack

```
1. AuthInterceptor        — adds Basic Auth header
2. HttpLoggingInterceptor — debug builds only (Level.BODY)
3. ErrorInterceptor       — maps HTTP error codes to domain exceptions
```

### 7.5 Required Interceptors

```kotlin
// data/remote/interceptor/ErrorInterceptor.kt
class ErrorInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        when (response.code) {
            401  -> throw UnauthorizedException()
            403  -> throw ForbiddenException()
            404  -> throw NotFoundException()
            422  -> throw ValidationException(response.body?.string())
            in 500..599 -> throw ServerException(response.code)
        }
        return response
    }
}
```

---

## 8. Local Storage Rules

### 8.1 Room — Entity Rules

```kotlin
// ✅ CORRECT — Room entities in data/local/entity/ only
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val variantId: Long,
    val productId: Long,
    val title: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String,
)

// ❌ WRONG — putting @Entity in domain layer
// ❌ WRONG — naming entities the same as domain models
```

### 8.2 DAO Rules

```kotlin
// ✅ CORRECT — all DAO methods are suspend or return Flow
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items") fun observeCartItems(): Flow<List<CartItemEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(item: CartItemEntity)
    @Delete suspend fun delete(item: CartItemEntity)
    @Query("UPDATE cart_items SET quantity = :qty WHERE variantId = :id") suspend fun updateQuantity(id: Long, qty: Int)
    @Query("DELETE FROM cart_items") suspend fun clearAll()
}
```

### 8.3 DataStore Preferences (simple key-value)

```kotlin
// data/local/datasource/UserPreferencesDataSource.kt
class UserPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val authToken: Flow<String?> = dataStore.data.map { it[AUTH_TOKEN_KEY] }
    val selectedCurrency: Flow<String> = dataStore.data.map { it[CURRENCY_KEY] ?: "USD" }

    suspend fun saveAuthToken(token: String) {
        dataStore.edit { it[AUTH_TOKEN_KEY] = token }
    }

    companion object {
        val AUTH_TOKEN_KEY  = stringPreferencesKey("auth_token")
        val CURRENCY_KEY    = stringPreferencesKey("currency")
    }
}
```

### 8.4 Proto DataStore (structured user prefs)

```proto
// data/src/main/proto/user_preferences.proto
syntax = "proto3";
option java_package = "com.yourpackage.data.local.proto";
option java_multiple_files = true;

message UserPreferences {
    string auth_token    = 1;
    string currency_code = 2;
    bool   is_guest      = 3;
    string locale        = 4;
}
```

```kotlin
// Use for: auth state, currency, locale — data that must survive process death
// Use DataStore Preferences for: simple toggles, last-seen page cursor
```

---

## 9. Offline-First Architecture (SSOT)

Every feature that reads data **must** follow the Single Source of Truth pattern:

```
               ┌─────────────┐
               │  Composable │   observes StateFlow
               └──────┬──────┘
                      │
               ┌──────▼──────┐
               │  ViewModel  │   converts Flow → StateFlow
               └──────┬──────┘
                      │ calls UseCase
               ┌──────▼──────┐
               │   UseCase   │   orchestrates
               └──────┬──────┘
                      │ calls Repository
               ┌──────▼──────┐
               │  Repository │   ← SSOT logic lives here
               └──┬───────┬──┘
         local ◄──┘       └──► remote
          (Room)                (Retrofit)
```

### 9.1 Repository Offline-First Pattern

```kotlin
// ✅ CORRECT — emit local first, fetch remote, update local, re-emit
class ProductRepositoryImpl @Inject constructor(
    private val localDataSource: IProductLocalDataSource,
    private val remoteDataSource: IProductRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : IProductRepository {

    override fun observeProducts(): Flow<List<Product>> = flow {
        // 1. Emit local cache immediately
        emitAll(localDataSource.observeProducts().map { list -> list.map { it.toDomain() } })
    }.onStart {
        // 2. Fetch fresh data from remote in background
        withContext(ioDispatcher) {
            runCatching { remoteDataSource.fetchProducts() }
                .onSuccess { dto -> localDataSource.cacheProducts(dto.products.map { it.toEntity() }) }
                // Network failure is silent — local cache is the fallback
        }
    }
}
```

---

## 10. State Management & Recomposition Rules

### 10.1 State Hoisting — REQUIRED

```kotlin
// ✅ CORRECT — stateless component, state hoisted to caller
@Composable
fun QuantitySelector(
    quantity: Int,
    maxQuantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) { /* only rendering */ }

// ✅ CORRECT — stateful wrapper at screen level only
@Composable
fun CartItemRow(
    item: CartItemUiModel,
    onEvent: (CartUiEvent) -> Unit,
) {
    QuantitySelector(
        quantity    = item.quantity,
        maxQuantity = item.stockAvailable,
        onIncrease  = { onEvent(CartUiEvent.IncrementQuantity(item.variantId)) },
        onDecrease  = { onEvent(CartUiEvent.DecrementQuantity(item.variantId)) },
    )
}

// ❌ WRONG — local remember inside a deep component that should be controlled
@Composable
fun QuantitySelector(...) {
    var count by remember { mutableStateOf(0) }  // ← WRONG if parent needs to know
}
```

### 10.2 Intelligent Recomposition

```kotlin
// ✅ Use stable data classes for UiModels (data class equality by default)
data class ProductUiModel(val id: String, val title: String, val price: String)

// ✅ Use keys in LazyColumn to preserve state
LazyColumn {
    items(products, key = { it.id }) { product ->
        ProductCard(product = product, onEvent = onEvent)
    }
}

// ✅ Derive state in composable (never duplicate state)
val totalPrice by remember(cartItems) {
    derivedStateOf { cartItems.sumOf { it.price * it.quantity } }
}

// ❌ WRONG — storing computed values in ViewModel when they can be derived in UI
```

### 10.3 Side Effects Rules

```kotlin
// LaunchedEffect  — runs a coroutine tied to a key; re-runs when key changes
LaunchedEffect(productId) { viewModel.loadProductDetail(productId) }

// LaunchedEffect(Unit) — runs once on first composition (navigation effect collector)
LaunchedEffect(Unit) { viewModel.uiEffect.collect { /* handle */ } }

// SideEffect — runs after every successful recomposition (for non-Compose side effects)
SideEffect { analyticsTracker.setScreen("ProductList") }

// DisposableEffect — for resources needing cleanup
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { ... }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}

// ❌ WRONG — business logic in SideEffect
// ❌ WRONG — LaunchedEffect with no key (always put a meaningful key)
```

---

## 11. Testing Architecture

### 11.1 Test Strategy Per Layer

| Layer        | What to Test                                      | Tool                    | Coverage Target |
|--------------|---------------------------------------------------|-------------------------|-----------------|
| Domain       | UseCase logic, entity transformations             | JUnit5 + MockK          | 100%            |
| Data         | Repository SSOT logic, mapper correctness         | JUnit5 + MockK + Turbine | 90%            |
| Presentation | ViewModel state transitions, effect emissions     | JUnit5 + MockK + Turbine | 95%            |
| UI           | Composable rendering with given state             | Compose Test            | Key screens     |

### 11.2 Test Doubles Strategy

```
Fakes   ← preferred for Repository, DataSource (fast, controllable, no magic)
Mocks   ← acceptable for UseCase in ViewModel tests
Stubs   ← for simple return-value scenarios
Spies   ← only when you need to verify calls on real objects
```

```kotlin
// ✅ CORRECT — Fake repository
class FakeProductRepository : IProductRepository {
    var productsToReturn: List<Product> = emptyList()
    var shouldThrow: Boolean = false

    override fun observeProducts(): Flow<List<Product>> = flow {
        if (shouldThrow) throw IOException("Network error")
        emit(productsToReturn)
    }
}

// ✅ CORRECT — ViewModel test with Turbine
@Test
fun `when products loaded successfully, state is Success`() = runTest {
    val fakeRepo = FakeProductRepository().apply { productsToReturn = listOf(tProduct) }
    val useCase = GetProductsUseCase(fakeRepo)
    val viewModel = ProductListViewModel(useCase, UnconfinedTestDispatcher())

    viewModel.uiState.test {
        assertEquals(ProductListUiState.Loading, awaitItem())
        val success = awaitItem() as ProductListUiState.Success
        assertEquals(1, success.products.size)
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 11.3 Test File Locations

```
data/src/test/
├── local/repository/       ← CartRepositoryImplTest.kt, ProductRepositoryImplTest.kt
├── remote/datasource/      ← ProductRemoteDataSourceTest.kt
└── mapper/                 ← ProductDtoMapperTest.kt

domain/src/test/
└── {feature}/usecase/      ← GetProductsUseCaseTest.kt, AddToCartUseCaseTest.kt

presentation/src/test/
└── {feature}/              ← ProductListViewModelTest.kt, CartViewModelTest.kt
```

---

## 12. Naming Conventions — STRICTLY ENFORCED

| Type                   | Convention        | Example                                     |
|------------------------|-------------------|---------------------------------------------|
| Kotlin files           | `PascalCase`      | `ProductListViewModel.kt`                   |
| Composable functions   | `PascalCase`      | `ProductCard()`, `AppButton()`              |
| Packages               | `lowercase`       | `com.yourpackage.data.remote.dto`           |
| Variables/functions    | `camelCase`       | `loadProducts()`, `uiState`                 |
| Constants              | `UPPER_SNAKE_CASE`| `AUTH_TOKEN_KEY`, `MAX_CART_QUANTITY`       |
| Room tables            | `snake_case`      | `cart_items`, `wishlist_items`              |
| Navigation routes      | `snake_case`      | `"product_detail"`, `"cart"`               |
| DTO classes            | Suffix `Dto`      | `ProductDto`, `CartItemDto`                 |
| Room entity classes    | Suffix `Entity`   | `CartItemEntity`, `WishlistItemEntity`      |
| Domain model classes   | No suffix         | `Product`, `CartItem`, `User`              |
| UI model classes       | Suffix `UiModel`  | `ProductUiModel`, `CartItemUiModel`         |
| UiState                | Suffix `UiState`  | `ProductListUiState`, `CartUiState`         |
| UiEvent                | Suffix `UiEvent`  | `ProductListUiEvent`, `CartUiEvent`         |
| UiEffect               | Suffix `UiEffect` | `ProductListUiEffect`, `CheckoutUiEffect`   |
| UseCase classes        | Suffix `UseCase`  | `GetProductsUseCase`, `AddToCartUseCase`    |
| Repository interfaces  | Prefix `I`        | `IProductRepository`, `ICartRepository`     |
| Repository impls       | Suffix `Impl`     | `ProductRepositoryImpl`                     |
| DataSource interfaces  | Prefix `I`        | `IProductRemoteDataSource`                  |
| DataSource impls       | Suffix `Impl`     | `ProductRemoteDataSourceImpl`               |
| Hilt modules           | Suffix `Module`   | `NetworkModule`, `RepositoryModule`         |
| Test files             | Suffix `Test`     | `ProductListViewModelTest.kt`               |

---

## 13. Feature-Specific Business Rules

### 13.1 Authentication

- **Guest users** can: browse products, view categories, search
- **Guest users** cannot: add to cart, add to wishlist, checkout
- **Auth guard** lives in NavHost — redirect to Login if accessing cart/wishlist as guest
- Firebase Auth is the identity provider (Email/Password + Social)
- Auth token stored in Proto DataStore — never in SharedPreferences
- On login: save token → navigate to Home, clearing auth back stack

### 13.2 Product Catalog

- Products come from Shopify REST API: `GET /products.json`
- Product detail: `GET /products/{id}.json`
- Reviews: fetched from Shopify `metafields` or custom API endpoint
- **Offline-first**: products cached in Room, served from cache on network failure
- Images loaded with Coil `AsyncImage` — placeholder + error fallback required

### 13.3 Search, Filter & Sort

- Search is **debounced 300ms** using `flatMapLatest` + `debounce`
- Filter options: Main Category, Sub-Category, Brand — persisted in `StateFlow` in ViewModel
- Sort options: `Price ASC`, `Price DESC`, `Best Seller`, `Sub-Category`
- Filter/sort applied **client-side** on cached data when offline, **server-side** params when online

### 13.4 Cart Business Rules

- Cart is **local-first** (stored in Room) — no Shopify cart API required
- Max quantity per item = `stock_available` from Shopify response
- Quantity increment blocked when `quantity >= stock_available`
- Total price recalculated reactively via `Flow.combine(cartItems, discount)` 
- Show confirmation dialog before: item removal, cart clear
- Cart item count shown as badge in bottom navigation

### 13.5 Wishlist Rules

- Wishlist stored in Room — authenticated users only
- Redirect guest to Login screen when tapping "Add to Wishlist"
- Toggle behavior: if item exists → remove; if not → add

### 13.6 Checkout Rules

- Step 1: Address selection / entry (validate with Google Places or HERE Maps)
- Step 2: Apply coupon code (validate via API)
- Step 3: Select payment method (COD / Online)
- COD blocked if order total > configured threshold (`MAX_COD_AMOUNT`)
- Online payment: redirect to Shopify checkout or payment gateway WebView
- On order success: clear cart in Room, emit navigation to confirmation screen
- Order confirmation email is sent by backend (not client responsibility)
- **Confirmation dialog required** before final order placement

### 13.7 Address Rules

- Address fields: recipient name, mobile number, street, city, country
- Country list fetched from API — cached in Room
- Address coordinates validated via Google Places API or HERE Maps
- Text auto-complete on street input field

### 13.8 Account Rules

- Show: personalized greeting, recent order history (last 5), wishlist preview (last 4 items)
- Currency toggle: fetch exchange rates from external API; store selected currency in DataStore
- Show confirmation dialog before: logout

### 13.9 Destructive Action Rule (HARD REQUIREMENT)

**EVERY** destructive or negative action MUST show a confirmation dialog before execution:
- Delete cart item
- Clear entire cart
- Remove from wishlist
- Log out
- Cancel order (if implemented)

```kotlin
// ✅ CORRECT — emit ShowConfirmationDialog effect, execute only on user confirm
is CartUiEvent.OnRemoveItemClicked -> {
    _uiEffect.emit(CartUiEffect.ShowConfirmDialog(
        message = "Remove this item from cart?",
        onConfirm = CartUiEvent.OnRemoveItemConfirmed(event.variantId),
    ))
}
```

---

## 14. UI & Design System Rules

### 14.1 Color — Use ONLY Theme Colors

```kotlin
// presentation/common/theme/AppColors.kt
object AppColors {
    val Primary        = Color(0xFF6C3CE1)   // Purple — from design reference
    val PrimaryVariant = Color(0xFF4A1FA0)
    val Surface        = Color(0xFFFFFFFF)
    val Background     = Color(0xFFF5F5F5)
    val OnPrimary      = Color(0xFFFFFFFF)
    val TextPrimary    = Color(0xFF1C1C1E)
    val TextSecondary  = Color(0xFF8E8E93)
    val Success        = Color(0xFF34C759)
    val Error          = Color(0xFFFF3B30)
    val Divider        = Color(0xFFE5E5EA)
}
// ❌ WRONG — never hardcode Color(0xFF...) in a Composable
```

### 14.2 Typography — Use AppTypography

```kotlin
// presentation/common/theme/AppTypography.kt
val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleMedium   = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyMedium    = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)
// ❌ WRONG — hardcoding TextStyle inline in any Composable
```

### 14.3 Shared Components — Use These, Never Reinvent

```
AppButton(text, onClick, modifier, isLoading, enabled)   ← primary / secondary / destructive variants
AppTextField(value, onValueChange, label, isError)        ← with validation message support
AppLoadingOverlay()                                       ← full-screen semi-transparent loader
AppErrorWidget(message, onRetry)                          ← error state with retry
AppSnackbar(message, type: Success/Error)                 ← via SnackbarHostState
ConfirmationDialog(title, message, onConfirm, onDismiss)  ← REQUIRED for all destructive actions
EmptyStateWidget(title, subtitle, illustration)           ← empty list / empty wishlist
ProductCard(product, onAddToCart, onAddToWishlist)        ← used in grid and list views
LoadingShimmer(modifier)                                  ← skeleton screens while loading
```

### 14.4 Accessibility

- All `Image` composables must have a non-null `contentDescription`
- Touch targets minimum `48.dp` × `48.dp`
- Semantic roles applied to interactive composables (`Role.Button`, etc.)

---

## 15. Code Quality Rules

### 15.1 What NOT to Do

- ❌ `TODO()` left in production code
- ❌ Unused imports
- ❌ Hardcoded strings in Composables (use `stringResource`)
- ❌ Hardcoded dimensions in Composables (use `dimensionResource` or tokens)
- ❌ `println()` or `System.out.println()` (use `Timber.d(...)`)
- ❌ Empty `catch` blocks
- ❌ Non-null assertions `!!` without a documented reason in a comment
- ❌ Functions longer than 40 lines
- ❌ Files with more than 300 lines

### 15.2 Logging

```kotlin
// ✅ CORRECT — Timber for all logging
Timber.d("Loading products for vendor: $vendor")
Timber.e(exception, "Failed to fetch products")

// Timber planted in Application class — DEBUG builds only
if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
```

---

## 16. Scope & Safety Rules

- ❌ Do NOT modify files outside the scope of the requested task
- ❌ Do NOT rename existing files unless explicitly asked
- ❌ Do NOT refactor working code while implementing a new feature
- ❌ Do NOT add new libraries without explicit approval
- ❌ Do NOT change the module structure without discussion
- ✅ Work on ONE feature or layer at a time
- ✅ Show the plan (files to create/change) BEFORE writing code
- ✅ Map every feature to its spec in `Shopify_Project_Specs.pdf`
- ✅ Every ViewModel must have a corresponding test file before the feature is marked done
- ✅ Every UseCase must be covered at 100% before a PR is raised

---

## 17. Documentation Rules

### 17.1 After Completing Any Task

1. Update `README.md` — mark the feature status as ✅
2. Write a summary to `docs/ai/YYYY-MM-DD-task-name.md`
3. If a new architecture decision was made → create `docs/adr/ADR-XXX.md`
4. Confirm with user before closing the task

### 17.2 Quick Reference

| I need to know...              | Location                                        |
|--------------------------------|-------------------------------------------------|
| Full feature requirements      | `Shopify_Project_Specs.pdf`                     |
| Module structure               | §2 of this file                                 |
| MVI pattern with code examples | §3 of this file                                 |
| Hilt DI setup                  | §4 of this file                                 |
| Navigation setup               | §5 of this file                                 |
| Flow & coroutine rules         | §6 of this file                                 |
| Network / Shopify API rules    | §7 of this file                                 |
| Offline-first / SSOT           | §9 of this file                                 |
| Testing strategy               | §11 of this file                                |
| Business domain rules          | §13 of this file                                |
| Shared components              | `presentation/common/components/`               |
| Theme tokens                   | `presentation/common/theme/`                    |

---

*Last updated: June 2026 · JETS Mobile Lab — Android Track*
