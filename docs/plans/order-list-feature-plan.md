# Order List / Order History Feature Plan

## Summary

Implement only the Order History list flow, opened from Profile > My Orders. The screen fetches the current authenticated user's Shopify orders through the existing Admin REST Retrofit setup, scoped by the saved Shopify `customer_id`, limited to 50 orders.

No order details, tracking, create/update/cancel/delete/close/reopen behavior will be added.

## Feature Scope

- Add a type-safe `OrderHistoryRoute` under the existing Main/Profile navigation flow.
- Display order number/name, created date, total price, available statuses, and line item thumbnails when the Shopify order response includes image data.
- Support loading, empty, error with retry, and manual refresh.
- Reuse the existing saved Shopify customer id flow through `GetCurrentCustomerIdUseCase` / `ICustomerIdProvider`.
- Do not pass `customer_id` through UI/navigation, hardcode it, or treat Firebase UID as Shopify customer id.
- Reuse existing Admin REST Retrofit client/token handling without exposing, moving, or changing tokens.

## Files To Add / Change

Add:
- `domain/account/model/OrderHistory.kt`
- `domain/account/repository/IOrderHistoryRepository.kt`
- `domain/account/usecase/GetOrderHistoryUseCase.kt`
- `domain/account/usecase/OrderHistoryUseCases.kt`
- `data/remote/api/OrderApiService.kt`
- `data/remote/dto/OrderDto.kt`
- `data/remote/datasource/IOrderRemoteDataSource.kt`
- `data/remote/datasource/OrderRemoteDataSourceImpl.kt`
- `data/repository/OrderHistoryRepositoryImpl.kt`
- `presentation/order/history/OrderHistoryScreen.kt`
- `presentation/order/history/OrderHistoryViewModel.kt`
- `presentation/order/history/OrderHistoryUiState.kt`
- `presentation/order/history/OrderHistoryUiIntent.kt`
- `presentation/order/history/OrderHistoryUiEffect.kt`
- focused order-history components and tests.

Change:
- `app/di/NetworkModule.kt`, `DataSourceModule.kt`, `RepositoryModule.kt`, `UseCaseModule.kt`
- `app/navigation/Route.kt`, `app/navigation/MainScreen.kt`
- `presentation/src/main/res/values/strings.xml`
- `presentation/src/main/res/values-ar/strings.xml`

## Data Layer Plan

- Add `OrderApiService.getOrders(version, status = "any", limit = 50, customerId)` using `GET admin/api/{version}/orders.json?status=any&limit=50&customer_id={customerId}`.
- Use the existing Retrofit/OkHttp Admin REST setup and existing `SHOPIFY_API_VERSION = "2024-04"` pattern from address data source.
- Add an order remote data source that delegates to `OrderApiService`.
- Add `OrderHistoryRepositoryImpl` with injected `@IoDispatcher`, `withContext(ioDispatcher)`, and `runCatchingCancellable`.
- Map HTTP/network/serialization failures to order-specific domain exceptions or reusable customer-id/address-style failures.

## Domain Layer Plan

- Add pure Kotlin models:
  - `OrderHistory(id, name, createdAt, totalPrice, currencyCode, financialStatus, fulfillmentStatus, orderStatus, lineItems)`
  - `OrderHistoryLineItem(id, title, quantity, imageUrl?)`
- Add `IOrderHistoryRepository.getOrders(customerId: Long): Result<List<OrderHistory>>`.
- Add `GetOrderHistoryUseCase(customerId: Long)` with one public `invoke`.
- Add `OrderHistoryUseCases(getCurrentCustomerId, getOrderHistory)` to mirror the existing address feature pattern.

## Presentation Layer Plan

- Add `OrderHistoryUiState`: `Loading`, `Empty`, `Error(messageRes)`, `Content(orders: ImmutableList<OrderHistoryUiModel>, isRefreshing: Boolean)`.
- Add `OrderHistoryUiIntent`: `OnBackClicked`, `OnRetry`, `OnRefresh`.
- Add `OrderHistoryUiEffect`: `NavigateBack`, `ShowMessage(messageRes)`.
- `OrderHistoryViewModel` resolves and caches Shopify customer id using `OrderHistoryUseCases.getCurrentCustomerId`, then fetches orders.
- `OrderHistoryScreen` uses `StateFlow`, `Channel`, `LaunchedEffect(Unit)`, stateless content, localized strings, and `AsyncImage` for thumbnails.
- UI follows the reference direction: off-white background, brand-consistent top bar, large page title, subtitle, rounded white order cards, status pills, horizontal thumbnails, and Profile tab context.
- Do not render working View Details or Track Package buttons in this phase.

## DI / Navigation Plan

- Provide `OrderApiService` from the existing Retrofit in `NetworkModule`.
- Bind `IOrderRemoteDataSource` and `IOrderHistoryRepository`.
- Provide `GetOrderHistoryUseCase` and `OrderHistoryUseCases`.
- Add `Route.OrderHistoryRoute`.
- In `MainScreen`, replace `onNavigateToOrders = { }` with navigation to `Route.OrderHistoryRoute`.
- Add nested `composable<Route.OrderHistoryRoute>` inside the MainScreen NavHost so the bottom bar remains visible with Profile context.

## API Response Mapping Plan

- Map root `orders` array from `OrdersResponseDto`.
- Map Shopify fields: `id`, `name`, `order_number`, `created_at`, `total_price`, `currency`, `financial_status`, `fulfillment_status`, `cancelled_at`, `closed_at`, and `line_items`.
- Derive `orderStatus` as Cancelled when `cancelled_at` exists, Closed when `closed_at` exists, otherwise Open.
- Use `name` first for display, fallback to `#order_number`, fallback to localized unknown order label.
- Parse ISO created date in presentation mapping and format as localized "Placed on ...".
- Line item images are optional; use image fields only if present in the order payload. Do not add extra product fetches for missing images.

## Empty / Loading / Error Plan

- Initial load: centered progress indicator.
- Refresh from content: keep current content visible and show a small progress indicator near the header.
- Empty: localized title/subtitle explaining there are no orders yet.
- Error: localized message with retry button.
- Missing Shopify customer id: reuse the friendly customer-id-unavailable style and route no ids through UI.

## Test Cases

- Domain: `GetOrderHistoryUseCase` forwards the provided Shopify customer id and returns repository result.
- Data: repository maps DTOs to domain models and maps HTTP/network failures.
- Presentation:
  - initial success becomes `Content`
  - empty response becomes `Empty`
  - failure becomes `Error`
  - refresh preserves content with `isRefreshing`
  - resolved Shopify customer id is passed to order use case
  - missing customer id shows the correct error state
- Build checks: targeted tests plus `:app:assembleDebug`.

## Manual Testing Checklist

- Profile > My Orders opens Order History.
- Request includes `status=any`, `limit=50`, and the saved Shopify `customer_id`.
- Loading, empty, populated, retry, and refresh states render.
- Cards show order name/number, date, total, statuses, and available thumbnails.
- No details/tracking/cancel/edit actions appear.
- No Shopify token literal such as `shpat_` appears in Android source.
- Existing Profile, Wishlist, Addresses, and bottom navigation still work.

## Risks / Assumptions

- Shopify Admin order `line_items` may not include product image URLs; thumbnails appear only when image data exists in the order response.
- The current mobile app already contains an Admin REST client; this feature reuses it as requested and does not change token handling.
- The Profile Recent Orders placeholder cards remain unchanged unless a later task explicitly asks to bind them to real order data.
- Arabic strings should be added with real translations, following the existing `values-ar` resource file.
