# Cart Feature Implementation Plan

## Approved Decisions

1. Size is product data only.
   - The cart will display the size or variant from product/cart item data.
   - Users cannot change size from the cart screen.

2. Guests are not allowed.
   - Cart actions require an authenticated user.
   - Add-to-cart and checkout should redirect to Login when unauthenticated.

3. Checkout is a callback for now.
   - `Proceed to Checkout` will call `onNavigateToCheckout: () -> Unit`.
   - No checkout screen is implemented in this feature.

## Goal

Implement a local-first cart feature for WearZone using Room, Clean Architecture, MVI, Hilt, Jetpack Compose, and type-safe navigation. The UI follows the provided premium cart design reference: top bar, item cards, quantity controls, remove action, low-stock warning, empty state, and sticky checkout summary.

## Rules To Follow

- Kotlin only, no Java.
- Jetpack Compose only, no XML layouts.
- Clean Architecture module boundaries:
  - `domain` contains models, repository interfaces, and use cases only.
  - `data` implements repositories, Room entities/DAO/data sources, and mapper logic.
  - `presentation` contains Compose screens, ViewModels, UiState, UiIntent, UiEffect, and UI components.
  - `app` wires navigation and Hilt bindings.
- MVI with `StateFlow<UiState>` and `Channel<UiEffect>`.
- Use `ImmutableList` in UiState.
- ViewModels call use cases only, never repositories.
- ViewModels do not inject dispatchers.
- Data repositories handle IO dispatching internally.
- Type-safe navigation with `@Serializable`; no string routes.
- No hardcoded strings in composables.
- No hardcoded colors in composables; use existing theme tokens.
- Every destructive cart action shows confirmation before execution.

## Files To Create

### Domain

- `domain/src/main/kotlin/com/example/wearzone/domain/cart/model/CartItem.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/cart/repository/ICartRepository.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/cart/usecase/ObserveCartUseCase.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/cart/usecase/AddToCartUseCase.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/cart/usecase/RemoveFromCartUseCase.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/cart/usecase/UpdateCartQuantityUseCase.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/cart/usecase/ClearCartUseCase.kt`

### Data

- `data/src/main/kotlin/com/example/wearzone/data/local/entity/CartItemEntity.kt`
- `data/src/main/kotlin/com/example/wearzone/data/local/dao/CartDao.kt`
- `data/src/main/kotlin/com/example/wearzone/data/local/datasource/ICartLocalDataSource.kt`
- `data/src/main/kotlin/com/example/wearzone/data/local/datasource/CartLocalDataSourceImpl.kt`
- `data/src/main/kotlin/com/example/wearzone/data/repository/CartRepositoryImpl.kt`

### Presentation

- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/CartScreen.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/CartViewModel.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/CartUiState.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/CartUiIntent.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/CartUiEffect.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/components/CartItemRow.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/components/QuantitySelector.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/components/PriceSummaryBar.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/components/EmptyCartContent.kt`

### Tests

- `domain/src/test/java/com/example/domain/cart/usecase/CartUseCasesTest.kt`
- `presentation/src/test/java/com/example/presentation/cart/CartViewModelTest.kt`

## Files To Modify

- `data/src/main/kotlin/com/example/wearzone/data/db/WearZoneDatabase.kt`
  - Add `CartItemEntity`.
  - Add `abstract fun cartDao(): CartDao`.
  - Bump Room version from `1` to `2`.

- `app/src/main/kotlin/com/example/wearzone/di/RepositoryModule.kt`
  - Bind `CartRepositoryImpl` to `ICartRepository`.

- `app/src/main/kotlin/com/example/wearzone/di/UseCaseModule.kt`
  - Provide cart use cases.

- `app/src/main/kotlin/com/example/wearzone/di/DataSourceModule.kt`
  - Bind cart local data source.

- `app/src/main/kotlin/com/example/wearzone/navigation/Route.kt`
  - Add `@Serializable data object CartRoute : Route`.

- `app/src/main/kotlin/com/example/wearzone/navigation/NavGraph.kt`
  - Add `composable<Route.CartRoute>`.
  - Wire `CartScreen`.
  - Connect Home cart icon to cart route.

- `presentation/src/main/kotlin/com/example/wearzone/presentation/home/components/TopBar.kt`
  - Replace dummy cart count with real callback/state input.
  - Add cart click callback.

- `presentation/src/main/kotlin/com/example/wearzone/presentation/home/HomeScreen.kt`
  - Accept `onNavigateToCart`.
  - Pass it to `TopBar`.

- `presentation/src/main/res/values/strings.xml`
  - Add cart strings.

- `presentation/src/main/res/values-ar/strings.xml`
  - Add Arabic cart strings.

## Implementation Steps

1. Add domain cart model, repository interface, and use cases.
2. Add Room entity, DAO, local data source, and cart repository implementation.
3. Update database and Hilt modules.
4. Add cart MVI contracts and ViewModel.
5. Build cart Compose UI and components from the provided design.
6. Wire typed navigation and Home cart icon callback.
7. Add strings and localization entries.
8. Add focused domain and ViewModel tests.
9. Run build/tests and fix compile or rule issues.

## Verification

- Confirm module boundaries are preserved.
- Confirm cart UiState uses `ImmutableList`.
- Confirm destructive actions require confirmation.
- Confirm quantity increment is blocked at `maxQuantity`.
- Confirm checkout button calls the empty callback.
- Run Gradle tests/build where available.
