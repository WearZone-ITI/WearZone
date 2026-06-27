# WEARZONE — Presentation Module · Agent Rules & Architecture Contract

> **MANDATORY:** Read this file **completely** before writing a single line of code in the `presentation` module.
> Every rule below is non-negotiable. Violations will be rejected in code review without exception.

---

## 0. Project Identity (Reference)

| Field            | Value                                                            |
|------------------|------------------------------------------------------------------|
| **App Name**     | WearZone                                                         |
| **Package**      | `com.wearzone`                                                   |
| **Platform**     | Android (Kotlin + Jetpack Compose)                               |
| **Figma**        | https://www.figma.com/design/k80kg88yUwwpiU5YxNFh0x/E-Commerce-App |
| **Architecture** | Clean Architecture + MVI                                         |

---

## 1. Technology Stack — Presentation Module

| Concern                   | Technology                                              | Notes                                         |
|--------------------------|---------------------------------------------------------|-----------------------------------------------|
| **Language**             | Kotlin (latest stable)                                  | 100% Kotlin — zero Java files                 |
| **UI Toolkit**           | Jetpack Compose (BOM latest stable)                     | Zero XML layouts anywhere                     |
| **Architecture**         | Clean Architecture + MVI                                | Strict layer separation                       |
| **Dependency Injection** | Hilt                                                    | Only DI framework allowed — no Koin           |
| **ViewModel**            | `androidx.lifecycle:lifecycle-viewmodel-compose`        | One ViewModel per screen                      |
| **State Management**     | `StateFlow<UiState>` + `Channel<UiEffect>`              | No LiveData                                   |
| **Async**                | Kotlin Coroutines (`viewModelScope`)                    | No dispatcher injection in ViewModel          |
| **Collections in UiState**| `kotlinx.collections.immutable`                        | `ImmutableList`, `ImmutableMap`               |
| **Image Loading**        | Coil 3 (Compose)                                        | `AsyncImage` everywhere                       |
| **Navigation**           | Jetpack Navigation Compose 2.8.0+                       | Type-safe routing only — string routes BANNED |
| **Annotation Processing**| KSP                                                     | KAPT is BANNED                                |

### ❌ Explicitly Forbidden in Presentation

| Technology                                    | Use Instead                          |
|-----------------------------------------------|--------------------------------------|
| String-based nav routes                       | Type-safe nav (`@Serializable`)      |
| `List<T>` in UiState                          | `ImmutableList<T>`                   |
| `MutableSharedFlow` for effects               | `Channel(BUFFERED)`                  |
| `LiveData`                                    | `StateFlow`                          |
| `RxJava` / `RxKotlin`                         | Kotlin Flow                          |
| `Koin`                                        | Hilt                                 |
| XML layouts                                   | Jetpack Compose                      |
| `GlobalScope`                                 | `viewModelScope`                     |
| `CoroutineDispatcher` injected into ViewModel | Inject into repos only               |
| `mutableStateOf` for VM-level state           | `StateFlow` in VM                    |
| `findViewById`                                | Compose                              |
| Any global `object` with mutable state        | Inject via Hilt                      |
| `kapt`                                        | KSP                                  |
| Direct repo call from ViewModel               | Always go through UseCase            |
| `ProductDto` or any DTO referenced in VM      | Map to `UiModel` via domain model    |
| `NavController` passed into ViewModel         | Emit `UiEffect` → handle in screen   |

---

## 2. Presentation Module — Folder Structure

```
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
    │   ├── AppNavHost.kt              ← single NavGraph for the entire app
    │   └── AppRoutes.kt               ← @Serializable graph roots + route objects
    └── theme/
        ├── AppTheme.kt
        ├── AppColors.kt
        ├── AppTypography.kt
        └── AppShapes.kt
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

Every screen folder **must** contain exactly these three contract files.

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
    data class OnProductClicked(val productId: String)    : ProductListUiIntent
    data class OnFilterApplied(val filter: ProductFilter) : ProductListUiIntent
    data class OnSortSelected(val sort: SortOption)       : ProductListUiIntent
    data class OnWishlistToggled(val productId: String)   : ProductListUiIntent
    data object OnRetry                                    : ProductListUiIntent
    data object OnLoadMore                                 : ProductListUiIntent
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

---

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
                        ProductListUiState.Success(
                            products.map { it.toUiModel() }.toImmutableList()
                        )
                }
                .onFailure { error ->
                    _uiState.value = ProductListUiState.Error(
                        error.localizedMessage ?: "Unknown error"
                    )
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

**ViewModel rules:**
- Always `@HiltViewModel` + `@Inject constructor`
- Never inject a `CoroutineDispatcher` — ViewModel is threading-agnostic
- Never call a repository directly — always go through a UseCase
- `handleIntent` is a lean dispatcher only — real logic lives in private functions
- Each private function does exactly ONE thing
- Never pass `NavController` into ViewModel — emit `UiEffect` instead

```kotlin
// ✅ In Composable — always hiltViewModel()
@Composable
fun ProductListScreen(viewModel: ProductListViewModel = hiltViewModel())

// ❌ NEVER instantiate ViewModel manually
val viewModel = ProductListViewModel(...)
```

---

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
| Presentation has ZERO data layer imports       | `ProductDto` referenced in ViewModel          |
| ViewModel never skips UseCase                  | Direct repo call from ViewModel               |
| DTOs never cross the repository boundary       | `ProductDto` passed to ViewModel              |
| `List<T>` never appears in a UiState           | `val products: List<ProductUiModel>`          |
| `MutableSharedFlow` never used for UI effects  | `_uiEffect = MutableSharedFlow<>()`           |
| `viewModelScope.launch(Dispatchers.IO)`        | Dispatcher argument in any ViewModel launch   |
| `NavController` passed into ViewModel          | `navController.navigate(...)` inside VM       |

---

## 4. Navigation — Type-Safe Jetpack Navigation Compose 2.8.0+

> **Rule:** All navigation for the entire application lives in exactly **two files**:
> `AppRoutes.kt` (route definitions) and `AppNavHost.kt` (the single NavHost with all
> nested graphs). No other file may declare routes or navigation logic.

### 4.1 Route & Graph Root Definitions — AppRoutes.kt

```kotlin
// presentation/common/navigation/AppRoutes.kt

// ── Graph roots ──────────────────────────────────────────────────────────────
@Serializable data object AuthGraph
@Serializable data object HomeGraph
@Serializable data object ProductGraph
@Serializable data object SearchGraph
@Serializable data object CartGraph
@Serializable data object WishlistGraph
@Serializable data object CheckoutGraph
@Serializable data object AccountGraph

// ── Screen routes — type-safe, @Serializable objects ─────────────────────────
@Serializable data object HomeRoute
@Serializable data object LoginRoute
@Serializable data object RegisterRoute
@Serializable data object CartRoute
@Serializable data object WishlistRoute
@Serializable data object CheckoutRoute
@Serializable data object AddressRoute    // ← part of CheckoutGraph, not standalone
@Serializable data object AccountRoute
@Serializable data object SearchRoute

// Routes with navigation arguments use data class (type-safe — no string parsing)
@Serializable data class ProductListRoute(val brand: String? = null, val category: String? = null)
@Serializable data class ProductDetailRoute(val productId: String)

// ❌ BANNED — string routes
// "product_detail/{productId}"  ← NEVER
```

### 4.2 AppNavHost — Single NavGraph for the Entire Application

```kotlin
// presentation/common/navigation/AppNavHost.kt
//
// This is the ONLY navigation file in the project.
// Every screen composable and every nested graph is declared here.
// Do NOT create additional NavGraph files anywhere in the project.

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = AuthGraph,  // resolved at launch from Proto DataStore auth state
) {
    NavHost(
        navController    = navController,
        startDestination = startDestination,
    ) {

        // ════ AUTH GRAPH ════════════════════════════════════════════════════
        navigation<AuthGraph>(startDestination = LoginRoute) {

            composable<LoginRoute> {
                LoginScreen(
                    onLoginSuccess       = {
                        navController.navigate(HomeGraph) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(RegisterRoute) },
                )
            }

            composable<RegisterRoute> {
                RegisterScreen(
                    onNavigateBack    = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(HomeGraph) {
                            popUpTo<AuthGraph> { inclusive = true }
                        }
                    },
                )
            }
        }

        // ════ HOME GRAPH ════════════════════════════════════════════════════
        navigation<HomeGraph>(startDestination = HomeRoute) {

            composable<HomeRoute> {
                HomeScreen(
                    onNavigateToProductList = { brand, category ->
                        navController.navigate(ProductListRoute(brand = brand, category = category))
                    },
                    onNavigateToSearch   = { navController.navigate(SearchGraph) },
                    onNavigateToCart     = { navController.navigate(CartGraph) },
                    onNavigateToAccount  = { navController.navigate(AccountGraph) },
                    onNavigateToWishlist = { navController.navigate(WishlistGraph) },
                )
            }
        }

        // ════ PRODUCT GRAPH ═════════════════════════════════════════════════
        navigation<ProductGraph>(startDestination = ProductListRoute()) {

            composable<ProductListRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ProductListRoute>()  // ✅ type-safe
                ProductListScreen(
                    brand              = route.brand,
                    category           = route.category,
                    onNavigateToDetail = { id -> navController.navigate(ProductDetailRoute(id)) },
                    onNavigateBack     = { navController.popBackStack() },
                )
            }

            composable<ProductDetailRoute> { backStackEntry ->
                val route = backStackEntry.toRoute<ProductDetailRoute>() // ✅ type-safe
                ProductDetailScreen(
                    productId         = route.productId,
                    onNavigateBack    = { navController.popBackStack() },
                    onNavigateToCart  = { navController.navigate(CartGraph) },
                    onNavigateToLogin = {
                        navController.navigate(AuthGraph) {
                            popUpTo<HomeGraph> { inclusive = false }
                        }
                    },
                )
            }
        }

        // ════ SEARCH GRAPH ══════════════════════════════════════════════════
        navigation<SearchGraph>(startDestination = SearchRoute) {
            composable<SearchRoute> {
                SearchScreen(
                    onNavigateBack            = { navController.popBackStack() },
                    onNavigateToProductDetail = { id -> navController.navigate(ProductDetailRoute(id)) },
                )
            }
        }

        // ════ CART GRAPH ════════════════════════════════════════════════════
        navigation<CartGraph>(startDestination = CartRoute) {
            composable<CartRoute> {
                CartScreen(
                    onNavigateBack       = { navController.popBackStack() },
                    onNavigateToCheckout = { navController.navigate(CheckoutGraph) },
                    onNavigateToLogin    = {
                        navController.navigate(AuthGraph) {
                            popUpTo<HomeGraph> { inclusive = false }
                        }
                    },
                )
            }
        }

        // ════ WISHLIST GRAPH ════════════════════════════════════════════════
        navigation<WishlistGraph>(startDestination = WishlistRoute) {
            composable<WishlistRoute> {
                WishlistScreen(
                    onNavigateBack            = { navController.popBackStack() },
                    onNavigateToProductDetail = { id -> navController.navigate(ProductDetailRoute(id)) },
                    onNavigateToLogin         = {
                        navController.navigate(AuthGraph) {
                            popUpTo<HomeGraph> { inclusive = false }
                        }
                    },
                )
            }
        }

        // ════ CHECKOUT GRAPH ════════════════════════════════════════════════
        navigation<CheckoutGraph>(startDestination = CheckoutRoute) {

            composable<CheckoutRoute> {
                CheckoutScreen(
                    onNavigateBack                = { navController.popBackStack() },
                    onNavigateToAddress           = { navController.navigate(AddressRoute) },
                    onNavigateToOrderConfirmation = {
                        navController.navigate(AccountGraph) {
                            popUpTo<CheckoutGraph> { inclusive = true }
                        }
                    },
                )
            }

            composable<AddressRoute> {
                AddressScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }
        }

        // ════ ACCOUNT GRAPH ═════════════════════════════════════════════════
        navigation<AccountGraph>(startDestination = AccountRoute) {
            composable<AccountRoute> {
                AccountScreen(
                    onNavigateToLogin       = {
                        navController.navigate(AuthGraph) {
                            popUpTo<HomeGraph> { inclusive = true }
                        }
                    },
                    onNavigateToWishlist    = { navController.navigate(WishlistGraph) },
                    onNavigateToOrderDetail = { id -> navController.navigate(ProductDetailRoute(id)) },
                )
            }
        }
    }
}
```

### 4.3 Navigation Rules

| Rule                                              | Detail                                                                      |
|---------------------------------------------------|-----------------------------------------------------------------------------|
| String routes                                     | BANNED — use `@Serializable` objects / data classes only                    |
| Navigation files                                  | Exactly two: `AppRoutes.kt` and `AppNavHost.kt` — no others                |
| Argument passing                                  | IDs only via typed route data class — never pass domain objects             |
| NavController in ViewModel                        | NEVER — emit `UiEffect` → collect in screen → call navController            |
| Cross-graph navigation                            | Navigate to the **graph root** (e.g. `navController.navigate(CartGraph)`)   |
| Back-stack clearing on auth success               | `popUpTo<AuthGraph> { inclusive = true }` when entering `HomeGraph`         |
| Auth guard for protected screens                  | Navigate to `AuthGraph` with `popUpTo` to clear back stack                  |
| NavController scope                               | Pass as lambda callbacks — **never** pass `NavController` into a ViewModel  |
| `AddressRoute`                                    | Declared inside `CheckoutGraph` — it is part of checkout flow, not global   |

---

## 5. Kotlin Coroutines & Flows — Presentation Rules

### 5.1 Dispatcher Responsibility in Presentation

```
┌─────────────────┐    viewModelScope.launch { }   ┌─────────────────┐
│   ViewModel     │ ──────────── (Main) ──────────► │   UseCase       │
│  (no dispatcher)│                                 │  (no dispatcher)│
└─────────────────┘                                 └────────┬────────┘
                                                             │  suspend fun call
                                                    ┌────────▼────────┐
                                                    │   Repository    │
                                                    │  withContext(   │  ← @IoDispatcher
                                                    │  ioDispatcher)  │     injected there
                                                    └─────────────────┘
```

**Rule:** ViewModels launch on `Main` via `viewModelScope`. They NEVER specify a dispatcher. Threading is handled entirely inside the Repository.

### 5.2 Hot vs Cold Flows in ViewModel

| Type              | Use Case                                          | Behaviour                                   |
|-------------------|---------------------------------------------------|---------------------------------------------|
| `StateFlow<T>`    | UiState in ViewModel                              | Always has value; replays last to collector |
| `Channel<T>`      | One-shot UiEffects (navigation, snackbar)         | Guaranteed delivery via `receiveAsFlow()`   |

```kotlin
// ✅ CORRECT — stateIn converts cold Flow from UseCase to hot StateFlow for UI
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

### 5.3 Search Debounce Pattern

```kotlin
// ✅ Always 300ms debounce for search input — applied in ViewModel
.debounce(300).flatMapLatest { query -> searchUseCase(query) }
```

---

## 6. Compose State Management & Recomposition

### 6.1 Immutable Collections in UiState — MANDATORY

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

### 6.2 State Hoisting

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

### 6.3 Intelligent Recomposition

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

### 6.4 Side Effects Rules

```kotlin
// LaunchedEffect(Unit)  — collect UiEffects from Channel (runs once)
LaunchedEffect(Unit) {
    viewModel.uiEffect.collect { effect -> /* navigate, show snackbar */ }
}

// LaunchedEffect(key)   — re-run when key changes
LaunchedEffect(productId) { viewModel.loadDetail(productId) }

// SideEffect            — post-recomposition non-Compose side effects
SideEffect { analyticsTracker.setCurrentScreen("ProductList") }

// DisposableEffect      — setup/teardown paired resources
DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event -> /* ... */ }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
}

// ❌ WRONG — business logic inside SideEffect
// ❌ WRONG — LaunchedEffect without a key (always specify a meaningful key)
// ❌ WRONG — navigating inside Composable body (use UiEffect Channel)
```

---

## 7. Design System — STRICTLY ENFORCED

### 7.1 AppColors

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
// ❌ NEVER hardcode Color(0xFF...) inside a Composable — always use AppColors.*
```

### 7.2 AppTypography

```kotlin
// ❌ NEVER hardcode TextStyle inline in a Composable
val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Bold,     fontSize = 28.sp),
    titleMedium   = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
    bodyMedium    = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Normal,   fontSize = 14.sp),
    labelSmall    = TextStyle(fontFamily = PoppinsFamily, fontWeight = FontWeight.Medium,   fontSize = 11.sp),
)
```

### 7.3 Shared Component Catalogue — Use These, Never Reinvent

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

All these live in `presentation/common/components/`. Never create a new component when one already exists.

### 7.4 Accessibility Checklist

- Every `Image` and `AsyncImage` must have a non-empty `contentDescription`
- Every tappable element: minimum touch target `48.dp × 48.dp`
- Apply `semantics { role = Role.Button }` to custom interactive elements
- Color contrast ratio ≥ 4.5:1 for all text on background

---

## 8. Destructive Action Contract — HARD REQUIREMENT

Every destructive/negative action MUST emit a `ShowConfirmDialog` effect first.
Execution happens only after the user explicitly confirms.

**Destructive actions list:**
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

## 9. Feature-Specific Business Rules — Presentation Concerns

### Authentication
- Guest users can browse products, search, and view product detail
- Guest users cannot add to cart / wishlist / checkout → redirect to Login
- On login success: navigate to `HomeGraph`, pop entire `AuthGraph` from back stack
- `startDestination` in `NavHost` determined by reading Proto DataStore auth state

### Product Catalog & Search
- Search input debounced 300ms: `.debounce(300).flatMapLatest { query -> searchUseCase(query) }`
- Active filters and sort selection held in ViewModel state — NOT in NavGraph arguments
- Filter options: Main Category, Sub-Category, Brand
- Sort options: `PRICE_ASC`, `PRICE_DESC`, `BEST_SELLER`, `SUB_CATEGORY`
- All images via Coil `AsyncImage` — placeholder shimmer (`LoadingShimmer`) + error fallback required

### Cart
- Increment button blocked at UI level when `item.quantity >= item.maxQuantity`
- Cart item count badge: exposed as `StateFlow<Int>` from `CartViewModel`, collected by bottom nav
- Checkout total recalculated reactively using `combine`

### Wishlist
- Unauthenticated tap on "Add to Wishlist" → emit `NavigateToLogin` effect

### Checkout
- Step 1: Address → Step 2: Coupon → Step 3: Payment Method
- Cash on Delivery option hidden/disabled when `orderTotal > BuildConfig.MAX_COD_AMOUNT`
- On order success: emit `CheckoutUiEffect.NavigateToOrderConfirmation`
- **Confirmation dialog required before submitting the final order** (use `ConfirmationDialog`)

### Account
- Personalized greeting, last 5 orders, last 4 wishlist items
- Currency selector: persist selection in DataStore; update displayed prices reactively
- Confirmation dialog required before logout

---

## 10. Code Quality Rules

### 10.1 Prohibited Patterns

| Pattern                          | Use Instead                                          |
|----------------------------------|------------------------------------------------------|
| `TODO()` in production code      | Resolve before commit                                |
| `println()` / `Log.d()`          | `Timber.d()` / `Timber.e()`                         |
| Empty `catch` blocks             | At minimum log with Timber                           |
| `!!` without a comment           | Use `?: return`, `let`, or `requireNotNull` with message |
| Functions > 40 lines             | Extract private functions                            |
| Files > 300 lines                | Extract components or helpers                        |
| Hardcoded strings in Composables | `stringResource(R.string.xxx)`                       |
| Hardcoded dimensions             | `dimensionResource` or theme tokens                  |
| Hardcoded colors                 | `AppColors.*` only                                   |

### 10.2 Logging

```kotlin
// Plant in Application class — DEBUG builds only
if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())

// Usage
Timber.d("Cart loaded: ${items.size} items")
Timber.e(exception, "Failed to place order")
Timber.w("Coupon '$code' was invalid")
```

---

## 11. Testing — Presentation Layer

### Test Strategy

| Layer        | What to Test                                         | Tools                    | Target      |
|--------------|------------------------------------------------------|--------------------------|-------------|
| Presentation | ViewModel state transitions, effect emissions        | JUnit5 + MockK + Turbine | 95%         |
| UI           | Composable rendering with given UiState              | Compose Test             | Key screens |

> **Every ViewModel must have a test file before the feature is marked done.**

### Test File Locations

```
presentation/src/test/
├── product/list/         ← ProductListViewModelTest
├── cart/                 ← CartViewModelTest
├── checkout/             ← CheckoutViewModelTest
└── auth/login/           ← LoginViewModelTest
```

### Test Dispatcher Rule

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRule : TestWatcher() {
    val testDispatcher = UnconfinedTestDispatcher()

    override fun starting(description: Description) { Dispatchers.setMain(testDispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
```

### ViewModel State Test (Turbine)

```kotlin
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

### Channel Effect Test (Turbine)

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

---

## 12. Naming Conventions — Presentation Module

| Type                       | Convention          | Example                                    |
|----------------------------|---------------------|--------------------------------------------|
| Composable functions       | `PascalCase`        | `ProductCard()`, `AppButton()`             |
| Kotlin files               | `PascalCase`        | `ProductListViewModel.kt`                  |
| Packages                   | `lowercase`         | `com.wearzone.presentation.product.list`   |
| Variables / functions      | `camelCase`         | `loadProducts()`, `uiState`                |
| Constants                  | `UPPER_SNAKE_CASE`  | `MAX_COD_AMOUNT`                           |
| Navigation routes          | Suffix `Route`      | `ProductDetailRoute`, `HomeRoute`          |
| UI model classes           | Suffix `UiModel`    | `ProductUiModel`, `CartItemUiModel`        |
| UiState                    | Suffix `UiState`    | `ProductListUiState`, `CartUiState`        |
| UiIntent                   | Suffix `UiIntent`   | `ProductListUiIntent`, `CartUiIntent`      |
| UiEffect                   | Suffix `UiEffect`   | `ProductListUiEffect`, `CheckoutUiEffect`  |
| Test files                 | Suffix `Test`       | `ProductListViewModelTest`                 |

---

## 13. Scope & Safety Rules

- ❌ Do NOT modify files outside the scope of the requested task
- ❌ Do NOT rename existing files unless explicitly asked
- ❌ Do NOT refactor working code while implementing a new feature
- ❌ Do NOT add new Gradle dependencies without explicit approval
- ❌ Do NOT change module boundaries without a discussion
- ✅ Work on ONE feature or layer at a time
- ✅ Show the list of files to create/change BEFORE writing code
- ✅ Every ViewModel must have a test file before the feature is marked done

---

*Last updated: June 2026 · JETS Mobile Lab — Android Track · WearZone v1.0*
