# Order List / Order History Feature Plan

## Summary

Implement only the Order History list flow, opened from Profile > My Orders. The screen fetches the current authenticated user's Shopify orders through the existing Admin REST Retrofit setup, scoped by the saved Shopify `customer_id`, limited to 50 orders.

## Feature Scope

- Add a type-safe `OrderHistoryRoute` under the existing Main/Profile navigation flow.
- Display order number/name, created date, total price, available statuses, and exactly **one dynamic line item thumbnail** from the actual order products.
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

Change:
- `app/di/NetworkModule.kt`, `DataSourceModule.kt`, `RepositoryModule.kt`, `UseCaseModule.kt`
- `app/navigation/Route.kt`, `app/navigation/MainScreen.kt`
- `presentation/src/main/res/values/strings.xml`
- `presentation/src/main/res/values-ar/strings.xml`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/profile/ProfileViewModel.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/profile/components/RecentOrderCard.kt`

## Data Layer Plan

- Add `OrderApiService.getOrders(version, status = "any", limit = 50, customerId)` using `GET admin/api/{version}/orders.json?status=any&limit=50&customer_id={customerId}`.
- Use the existing Retrofit/OkHttp Admin REST setup.
- Add an order remote data source that delegates to `OrderApiService`.
- Add `OrderHistoryRepositoryImpl` with injected `@IoDispatcher`.
- Map Shopify fields: `id`, `name`, `order_number`, `created_at`, `total_price`, `currency`, `financial_status`, `fulfillment_status`, `cancelled_at`, `closed_at`, `line_items`, and `payment_gateway_names`.

## Domain Layer Plan

- Add pure Kotlin models:
  - `OrderHistory(id, name, createdAt, totalPrice, currencyCode, financialStatus, fulfillmentStatus, orderStatus, paymentGatewayNames, lineItems)`
  - `OrderHistoryLineItem(id, title, quantity, imageUrl?)`
- Add `IOrderHistoryRepository.getOrders(customerId: Long): Result<List<OrderHistory>>`.

## Presentation Layer Plan

- **Order History**: 
  - Status Priority: `Cancelled` status overrides all.
  - Payment Status: Show "Paid" for Credit Card and "Pending" for COD.
  - Image: Show exactly one dynamic image of a product from the order.
  - Removed all generic placeholders and "No image" text.
- **Order Details**:
  - Tracking Timeline: Replaced "Processing" with "Preparing Order".
- **Profile Integration**:
  - Recent Orders: Replaced static demo data with real user order history (last 2 orders).
  - Interactivity: Recent Order cards are clickable and navigate to Order History.
  - Imagery: Uses `AsyncImage` for real-time product thumbnails.

## API Response Mapping Plan

- Map root `orders` array from `OrdersResponseDto`.
- Derive `orderStatus`: Cancelled when `cancelled_at` exists, Closed when `closed_at` exists, otherwise Open.
- Derive Financial Status from `payment_gateway_names`:
  - Credit Card -> `paid`
  - Cash on Delivery -> `pending`
- Parse ISO created date and format as localized "Placed on ...".

## Test Cases

- Domain: `GetOrderHistoryUseCase` forwards the provided Shopify customer id and returns repository result.
- Data: repository maps DTOs to domain models including payment gateway and dynamic images.
- Presentation:
  - verify "Cancelled" status takes priority.
  - verify "Paid" vs "Pending" labels based on payment method.
  - verify real order integration on Profile screen.
  - verify navigation from Profile cards.

## Manual Testing Checklist

- Profile > My Orders opens Order History.
- Cards show payment-accurate status (Paid for Card, Pending for COD).
- Cancelled orders show red "Cancelled" badge.
- Profile "Recent Orders" shows real product images from actual history.
- Clicking a Profile order card navigates to Order History.
- No static placeholder images appear in any order-related view.
- Tracking timeline step 2 says "Preparing Order".
