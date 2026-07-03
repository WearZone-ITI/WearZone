## Feature Scope

Implement a discount-code flow inside the existing checkout feature that lets users:

- Navigate from Cart to the already-wired Checkout route.
- View an order summary using existing cart items in `CheckoutScreen`.
- Enter a discount code in a promo-code field styled after the supplied design and WearZone theme.
- Validate the code through the app's Shopify API layer and see the discount reflected in the order total.
- Remove or replace the applied code.
- See localized validation, success, loading, and failure feedback.
- Place the order with the applied discount included in the Shopify order request.
- Navigate to the existing order list / order history feature after successful order placement.

Out of scope for this pass:

- Payment gateway integration.
- Replacing the existing order-history feature.
- New external dependencies.

## Proposed Discount Rules

Use the existing Shopify-backed Retrofit stack already present in the project.

- Trim input before validation.
- Empty code: show localized required-code error.
- Validate codes through a new Shopify discount API service/data source.
- Fetch enough Shopify discount data to determine percentage or fixed amount discount.
- Invalid code: keep totals unchanged and show localized invalid-code error.
- Discount cannot reduce total below zero.
- Applying a new valid code replaces the previous applied code.
- Successful order placement clears the cart through the existing `PlaceOrderUseCase` flow and emits navigation to the existing order list.

## Current Code Findings From Re-Read

- `Route.CheckoutRoute` already exists in the Hend block.
- `NavGraph` already navigates from `CartScreen` to `CheckoutScreen`.
- `NavGraph` already has `OrderHistoryRoute` and `CheckoutScreen.onNavigateToOrderHistory`.
- `CheckoutScreen`, `CheckoutViewModel`, `CheckoutUiState`, `CheckoutUiIntent`, and `CheckoutUiEffect` already exist.
- `PlaceOrderUseCase` already creates Shopify orders and clears the cart.
- `CheckoutRepositoryImpl` maps `CheckoutOrderRequest` to `ShopifyOrderRequestDto`.
- `OrderApiService` already posts orders to `admin/api/{version}/orders.json`.
- `NetworkModule` currently injects `SHOPIFY_ADMIN_TOKEN`; this exists already, but the root rules warn that Admin API should be server-side only.
- No `CheckoutViewModelTest` exists yet.

## Files To Create

Domain:

- `domain/src/main/kotlin/com/example/wearzone/domain/checkout/model/CheckoutDiscount.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/checkout/repository/IDiscountRepository.kt`
- `domain/src/main/kotlin/com/example/wearzone/domain/checkout/usecase/ApplyDiscountCodeUseCase.kt`

Data:

- `data/src/main/kotlin/com/example/wearzone/data/remote/api/DiscountApiService.kt`
- `data/src/main/kotlin/com/example/wearzone/data/remote/datasource/IDiscountRemoteDataSource.kt`
- `data/src/main/kotlin/com/example/wearzone/data/remote/datasource/DiscountRemoteDataSourceImpl.kt`
- `data/src/main/kotlin/com/example/wearzone/data/remote/dto/DiscountDto.kt`
- `data/src/main/kotlin/com/example/wearzone/data/repository/DiscountRepositoryImpl.kt`

Presentation:

- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/components/PromoCodeCard.kt`
- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/components/CheckoutOrderSummaryCard.kt`

Tests:

- `domain/src/test/java/com/example/domain/checkout/usecase/ApplyDiscountCodeUseCaseTest.kt`
- `presentation/src/test/java/com/example/presentation/checkout/CheckoutViewModelTest.kt`

## Files To Modify

- `app/src/main/kotlin/com/example/wearzone/navigation/NavGraph.kt`
  - Keep `CartRoute -> CheckoutRoute` wiring.
  - Confirm `CheckoutRoute -> OrderHistoryRoute` navigation after successful order placement.
  - Ensure order-history back behavior returns to a sensible app screen.
- `app/src/main/kotlin/com/example/wearzone/di/NetworkModule.kt`
  - Provide `DiscountApiService`.
- `app/src/main/kotlin/com/example/wearzone/di/DataSourceModule.kt`
  - Bind/provide `DiscountRemoteDataSourceImpl` if the module uses bindings for remote data sources.
- `app/src/main/kotlin/com/example/wearzone/di/RepositoryModule.kt`
  - Bind `DiscountRepositoryImpl` to `IDiscountRepository`.
- `app/src/main/kotlin/com/example/wearzone/di/UseCaseModule.kt`
  - Provide `ApplyDiscountCodeUseCase` to match current project style.
- `domain/src/main/kotlin/com/example/wearzone/domain/checkout/model/CheckoutOrder.kt`
  - Add optional discount data to `CheckoutOrderRequest`.
- `domain/src/main/kotlin/com/example/wearzone/domain/checkout/usecase/PlaceOrderUseCase.kt`
  - Accept an optional applied discount and pass it to `CheckoutOrderRequest`.
- `data/src/main/kotlin/com/example/wearzone/data/remote/dto/OrderDto.kt`
  - Add Shopify order discount DTO fields to the request payload.
- `data/src/main/kotlin/com/example/wearzone/data/repository/CheckoutRepositoryImpl.kt`
  - Map `CheckoutDiscount` into Shopify order request discount fields.
- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutViewModel.kt`
  - Add promo-code state, validation, discount calculation, apply/remove intents, and pass discount to `PlaceOrderUseCase`.
- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutUiState.kt`
  - Add discount fields, promo input state, loading flag, and optional validation message resource.
- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutUiIntent.kt`
  - Add promo text changed, apply discount, and remove discount intents.
- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutUiEffect.kt`
  - Reuse localized message effects for success/failure and existing order-list navigation effect.
- `presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutScreen.kt`
  - Add promo card and discount row using existing `AppTheme`.
- `presentation/src/main/res/values/strings.xml`
  - Add Hend discount and checkout strings.
- `presentation/src/main/res/values-ar/strings.xml`
  - Add Arabic equivalents for the same Hend keys.

## Architecture Plan

1. Domain
   - Add `CheckoutDiscount` with code, value, value type, and calculated amount data.
   - Add `IDiscountRepository` with an apply/validate method that returns a domain discount result.
   - Add `ApplyDiscountCodeUseCase` with exactly one public `invoke`.
   - Extend `PlaceOrderUseCase` to accept an optional discount and include it in `CheckoutOrderRequest`.

2. Data
   - Add Shopify discount remote service/data source.
   - Validate discount code with Shopify and map Shopify response into domain `CheckoutDiscount`.
   - Map applied discount into Shopify order creation in `CheckoutRepositoryImpl`.
   - Keep dispatcher usage in repositories/data layer only.

3. Presentation
   - Extend existing Checkout MVI contracts instead of creating a second checkout flow.
   - `CheckoutViewModel` continues observing cart through `ObserveCartUseCase`, applies discounts through `ApplyDiscountCodeUseCase`, and exposes:
     - cart items
     - subtotal
     - discount row when applied
     - total
     - promo field text
     - applying/loading state
     - validation error state
     - order placing state
   - Use `Channel(BUFFERED)` for one-shot snackbar/navigation effects.
   - On successful order placement, emit `CheckoutUiEffect.NavigateToOrderHistory`.
   - Do not inject dispatchers into ViewModel.

4. UI
   - Adapt the supplied design using existing `AppTheme`.
   - Use WearZone app name rather than VogueVibe.
   - Use localized strings via `stringResource`.
   - Use existing colors/typography/shapes; no hardcoded Compose colors.
   - Keep touch targets at least 48 dp.
   - Keep the existing order-placement CTA, but update the total to include discount.
   - Add a promo-code card and order-summary card structure inspired by the reference.

5. Navigation
   - Keep existing type-safe `CheckoutRoute`.
   - Keep existing Cart to Checkout navigation.
   - Confirm Checkout back action returns to Cart.
   - Connect successful discounted order placement to existing `OrderHistoryRoute`.
   - Confirm OrderHistory back action returns to previous screen or `MainRoute`.

6. Localization
   - Add all Hend-owned keys to English and Arabic resources.
   - Candidate keys:
     - `checkout_title`
     - `checkout_promo_code_title`
     - `checkout_promo_code_placeholder`
     - `checkout_apply_discount`
     - `checkout_remove_discount`
     - `checkout_discount_applied`
     - `checkout_discount_invalid`
     - `checkout_discount_required`
     - `checkout_discount_unavailable`
     - `checkout_order_summary`
     - `checkout_discount`
     - `checkout_place_order_with_total`
     - `checkout_discount_code_content_description`

7. Tests
   - Domain usecase tests for valid code and invalid Shopify repository result.
   - Add focused coverage for optional discount passed into `CheckoutOrderRequest` if checkout domain tests are introduced.
   - ViewModel tests for:
     - initial cart content maps to checkout summary
     - valid discount updates total and emits success feedback
     - invalid/empty code sets validation error and keeps total unchanged
     - remove discount restores total
     - checkout/place-order success emits `NavigateToOrderHistory`
     - place-order failure emits localized error message

## Verification Commands

Run from:

`C:/Users/user/AndroidStudioProjects/WearZone`

- `./gradlew.bat :domain:test`
- `./gradlew.bat :presentation:test`
- `./gradlew.bat :app:compileDebugKotlin`

If the full suite is slow or blocked, at minimum run the new/affected module tests and report the exact blocker.

## Approval Notes

- The plan now assumes the feature extends the existing checkout/order-history flow.
- The plan assumes Shopify discount validation is added through the existing Retrofit API structure.
- The root rules warn against Admin API from mobile, while current app code already uses Admin REST for orders. If strict compliance is required, the discount endpoint should be served by the BFF instead of the Android app calling Admin discount endpoints directly.
