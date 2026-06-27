# WEARZONE — Domain Module · Agent Rules & Architecture Contract

> **MANDATORY:** Read this file **completely** before writing a single line of code in the `domain` module.
> Every rule below is non-negotiable. Violations will be rejected in code review without exception.

---

## 0. Project Identity (Reference)

| Field           | Value                                                             |
|-----------------|-------------------------------------------------------------------|
| **App Name**    | WearZone                                                          |
| **Package**     | `com.wearzone`                                                    |
| **Platform**    | Android (Kotlin + Jetpack Compose)                                |
| **Domain**      | E-Commerce — Shopify-backed fashion/apparel browsing & checkout   |
| **Users**       | Guest · Authenticated Customer                                    |

---

## 1. Domain Module — Golden Rule

> The `domain` module is **pure Kotlin only**.
> It knows NOTHING about Android, Room, Retrofit, Hilt, Compose, or any framework.
> It contains only: UseCases, domain Models, Repository interfaces, and shared utilities.

### Module Dependency

```
domain → nothing (stdlib only)
```

**Hard Rules:**
- `domain` → imports NOTHING outside Kotlin stdlib
- ❌ NO `import android.*` anywhere in domain
- ❌ NO `@Entity`, `@Dao`, `@HiltViewModel`, or any framework annotation
- ❌ NO Room, Retrofit, Hilt, Coroutines (except as return types of interfaces), Compose imports
- Domain Models must never appear inside DTOs
- DTOs must never cross the repository boundary into domain

---

## 2. Technology Stack — Domain Module

| Concern          | Technology         | Notes                                       |
|------------------|--------------------|---------------------------------------------|
| **Language**     | Kotlin (latest)    | 100% Kotlin — zero Java files               |
| **Async**        | Kotlin Coroutines  | `suspend fun` in interfaces, `Flow<T>` only |
| **Testing**      | JUnit 5 + MockK    | 100% coverage required on all UseCases      |

### Explicitly Forbidden in Domain

| Technology            | Reason                                      |
|-----------------------|---------------------------------------------|
| Any Android import    | Domain is pure Kotlin — framework-agnostic  |
| `@Entity`             | Room annotation — belongs in data layer     |
| `@HiltViewModel`      | DI annotation — belongs in presentation     |
| `List<T>` in UiState  | UiState doesn't exist in domain             |
| `LiveData`            | Android-specific — not allowed              |
| `RxJava` / `RxKotlin` | Not part of the tech stack                 |

---

## 3. Domain Module — Folder Structure

```
domain/src/main/kotlin/com.wearzone.domain/
├── auth/
│   ├── model/                           ← Pure Kotlin (no suffix — just the concept name)
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
    │   ├── DataResult.kt               ← sealed class DataResult<T>
    │   └── RunCatchingCancellable.kt   ← suspend fun runCatchingCancellable(...)
    └── error/
        └── DomainError.kt              ← sealed class DomainError
```

---

## 4. UseCase Contract — STRICTLY ENFORCED

Every UseCase has **exactly ONE public `invoke()` operator**. No other public methods allowed.

```kotlin
// domain/product/usecase/GetProductsUseCase.kt
class GetProductsUseCase @Inject constructor(
    private val repository: IProductRepository,
) {
    // ✅ Single public invoke — takes parameters or none
    suspend operator fun invoke(): Result<List<Product>> =
        repository.getProducts()
}

// domain/product/usecase/SearchProductsUseCase.kt
class SearchProductsUseCase @Inject constructor(
    private val repository: IProductRepository,
) {
    suspend operator fun invoke(query: String): Result<List<Product>> =
        repository.searchProducts(query)
}
```

**UseCase violations:**
- ❌ UseCase with multiple public methods
- ❌ ViewModel calling repository directly (must always go through UseCase)
- ❌ UseCase injecting a dispatcher (dispatcher-agnostic — data layer handles it)

---

## 5. Repository Interface Contract

Repository interfaces live in `domain` — implementations live in `data`.

```kotlin
// domain/product/repository/IProductRepository.kt
interface IProductRepository {
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductDetail(id: String): Result<ProductDetail>
    suspend fun searchProducts(query: String): Result<List<Product>>
    fun observeProducts(brand: String?): Flow<List<Product>>   // cold Flow — Room-backed
}

// domain/cart/repository/ICartRepository.kt
interface ICartRepository {
    fun observeCart(): Flow<List<CartItem>>
    suspend fun addToCart(item: CartItem): Result<Unit>
    suspend fun removeFromCart(variantId: Long): Result<Unit>
    suspend fun updateQuantity(variantId: Long, quantity: Int): Result<Unit>
    suspend fun clearCart(): Result<Unit>
}

// domain/wishlist/repository/IWishlistRepository.kt
interface IWishlistRepository {
    fun observeWishlist(): Flow<List<WishlistItem>>
    suspend fun toggleWishlist(productId: String): Result<Unit>
    suspend fun removeFromWishlist(productId: String): Result<Unit>
}
```

**Interface rules:**
- Prefix `I` on all repository interfaces: `IProductRepository`, `ICartRepository`
- Only domain types in method signatures — never DTOs or Entities
- `Flow<T>` for observable streams, `suspend fun` for one-shot operations, always wrapped in `Result<T>`

---

## 6. Domain Models

Pure Kotlin data classes — no framework annotations, no Android imports.

```kotlin
// domain/product/model/Product.kt
data class Product(
    val id: String,
    val title: String,
    val vendor: String,
    val price: Double,
    val currencyCode: String,
    val imageUrl: String,
    val isAvailable: Boolean,
)

// domain/cart/model/CartItem.kt
data class CartItem(
    val variantId: Long,
    val productId: Long,
    val title: String,
    val price: Double,
    val quantity: Int,
    val maxQuantity: Int,
    val imageUrl: String,
)

// domain/checkout/model/Order.kt
data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalPrice: Double,
    val address: Address,
    val paymentMethod: PaymentMethod,
    val coupon: Coupon?,
)
```

**Model rules:**
- Domain models have NO suffix: `Product`, `CartItem`, `User` (not `ProductModel`, `CartItemDto`)
- No `@Entity`, `@Serializable`, or any annotation
- No Android imports (`Context`, `Parcelable`, etc.)

---

## 7. Shared Utilities in Domain

### DataResult / DomainError

```kotlin
// domain/common/result/DataResult.kt
sealed class DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>()
    data class Error(val error: DomainError) : DataResult<Nothing>()
    data object Loading : DataResult<Nothing>()
}

// domain/common/error/DomainError.kt
sealed class DomainError {
    data class NetworkError(val message: String) : DomainError()
    data class ServerError(val code: Int, val message: String) : DomainError()
    data object UnauthorizedError : DomainError()
    data object NotFoundError : DomainError()
    data class ValidationError(val message: String) : DomainError()
    data class UnknownError(val throwable: Throwable) : DomainError()
}
```

### runCatchingCancellable Utility

```kotlin
// domain/common/result/RunCatchingCancellable.kt
// Add once — use everywhere in data and domain suspend functions
suspend fun <T> runCatchingCancellable(block: suspend () -> T): Result<T> =
    runCatching { block() }.also { result ->
        result.exceptionOrNull()?.let { e ->
            if (e is CancellationException) throw e   // re-propagate cancellation
        }
    }
```

> ⚠️ **Never use bare `runCatching { }` in suspend functions.**
> `runCatching` catches `Throwable`, which includes `CancellationException`.
> Always use `runCatchingCancellable` in any coroutine context.

---

## 8. Layer Violation Rules — Zero Tolerance

| Rule                                           | Example of Violation                          |
|------------------------------------------------|-----------------------------------------------|
| Domain has ZERO Android imports                | `import android.*` in domain module           |
| Domain has ZERO Room/Retrofit imports          | `@Entity` on a domain Model                   |
| Presentation has ZERO data layer imports       | `ProductDto` referenced in ViewModel          |
| ViewModel never skips UseCase                  | Direct repo call from ViewModel               |
| UseCase has exactly ONE public `invoke()` operator | UseCase with multiple public methods     |
| DTOs never cross the repository boundary       | `ProductDto` passed to ViewModel              |
| Domain Models never enter DTOs                 | `Product` imported inside `ProductDto`        |

---

## 9. SOLID Checklist — Before Writing Any Domain Class

- **S** — Does this class have exactly one reason to change?
- **O** — Is the repository an interface (open for extension, closed for modification)?
- **L** — Can `FakeProductRepository` substitute `ProductRepositoryImpl` with zero caller changes?
- **I** — Is `IProductRepository` focused only on products (no cart or auth methods)?
- **D** — Does `ProductListViewModel` depend on `GetProductsUseCase`, not `ProductRepositoryImpl`?

---

## 10. Testing — Domain Layer

### Test Strategy

| Layer  | What to Test                            | Tools          | Target |
|--------|-----------------------------------------|----------------|--------|
| Domain | UseCase logic, model transformations    | JUnit5 + MockK | 100%   |

> **Every UseCase must reach 100% test coverage before a PR is raised.**

### Test File Locations

```
domain/src/test/
├── product/usecase/      ← GetProductsUseCaseTest, SearchProductsUseCaseTest
├── cart/usecase/         ← AddToCartUseCaseTest, RemoveFromCartUseCaseTest
└── checkout/usecase/     ← PlaceOrderUseCaseTest, ApplyCouponUseCaseTest
```

### Test Doubles Strategy

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
```

---

## 11. Feature-Specific Business Rules (Domain Layer Perspective)

### Authentication
- Auth token stored in Proto DataStore — never in Preferences DataStore
- Session persistence: read auth token from Proto DataStore on app start
- `IAuthRepository` handles login, register, logout, token refresh

### Product Catalog
- Products fetched from Shopify Storefront API (REST or GraphQL)
- **Offline-first**: products cached in Room, served from cache on network failure
- Search input debounced 300ms — handled at ViewModel level, not UseCase

### Cart
- Cart is **local-first** — stored in Room, no Shopify Cart API required
- `maxQuantity` per item = `inventory_quantity` from Shopify product response
- Checkout total recalculated reactively via `combine(cartItemsFlow, discountFlow)`

### Wishlist
- Stored in Room — authenticated users only
- Toggle: if item exists → remove; if not → add (single `ToggleWishlistUseCase`)

### Checkout
- Step 1: Address selection / entry
- Step 2: Apply coupon code (validate via API — `ApplyCouponUseCase`)
- Step 3: Select payment method
- Cash on Delivery blocked when `orderTotal > BuildConfig.MAX_COD_AMOUNT`
- On order success: clear cart, emit confirmation

### Destructive Actions — Must Have Confirmation
Every destructive action must be confirmed by the user before the UseCase executes:
- Delete a cart item
- Clear the entire cart
- Remove from wishlist
- Logout
- Cancel an order

---

## 12. Naming Conventions — Domain Module

| Type                       | Convention          | Example                                    |
|----------------------------|---------------------|--------------------------------------------|
| Domain model classes       | No suffix           | `Product`, `CartItem`, `User`              |
| Repository interfaces      | Prefix `I`          | `IProductRepository`, `ICartRepository`    |
| UseCase classes            | Suffix `UseCase`    | `GetProductsUseCase`, `AddToCartUseCase`   |
| Kotlin files               | `PascalCase`        | `GetProductsUseCase.kt`                    |
| Packages                   | `lowercase`         | `com.wearzone.domain.product.usecase`      |
| Variables / functions      | `camelCase`         | `getProducts()`, `invoke()`                |
| Constants                  | `UPPER_SNAKE_CASE`  | `MAX_COD_AMOUNT`                           |
| Test files                 | Suffix `Test`       | `GetProductsUseCaseTest`                   |

---

## 13. Scope & Safety Rules

- ❌ Do NOT modify files outside the scope of the requested task
- ❌ Do NOT rename existing files unless explicitly asked
- ❌ Do NOT refactor working code while implementing a new feature
- ❌ Do NOT add new Gradle dependencies without explicit approval
- ❌ Do NOT change module boundaries without a discussion
- ✅ Work on ONE feature or layer at a time
- ✅ Show the list of files to create/change BEFORE writing code
- ✅ Every UseCase must reach 100% test coverage before a PR is raised

---

*Last updated: June 2026 · JETS Mobile Lab — Android Track · WearZone v1.0*
