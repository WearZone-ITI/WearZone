# Guest Mode Implementation

## Summary
Implemented and tightened guest browsing while keeping cart, checkout, profile account data, wishlist, orders, addresses, and favorite actions protected behind the app-wide auth access state.

## Key Changes
- Added `AuthAccessState` and `GetAuthAccessStateUseCase` to distinguish guest, authenticated-without-customer-id, and authenticated customer access.
- Routed onboarding guest entry to the existing main shopping flow and added pending protected-route return for checkout after login/register.
- Blocked guest cart access at the cart screen, so stored cart rows are not shown until an authenticated customer session is available.
- Skipped remote draft-order cart sync when no saved Shopify customer id exists.
- Cleared saved draft-order id whenever customer session state is reset or replaced, while preserving the local cart.
- Blocked guest add-to-cart from Home, Product Detail, and Vendor Products before a cart item is constructed or persisted.
- Blocked Wishlist from observing old per-user wishlist data unless the current access state is an authenticated Shopify customer.
- Added shared Sign In Required dialog usage for cart access, add-to-cart, checkout, wishlist, profile protected actions, home favorites, product detail favorites, and vendor product favorites.
- Added feature-specific sign-in messages for cart and wishlist actions.
- Updated Profile guest state to show "Welcome to WearZone" with Sign In, Create Account, and Continue Browsing actions.

## Verification
- `.\gradlew.bat assembleDebug` passed.
- `.\gradlew.bat :presentation:testDebugUnitTest --tests com.example.presentation.cart.CartViewModelTest --tests com.example.presentation.productdetails.ProductDetailViewModelTest --tests com.example.presentation.product.list.ProductListViewModelTest --tests com.example.presentation.search.SearchViewModelTest` passed.

## Notes
- Existing Firebase Auth, Google Sign-In, Shopify token handling, and Shopify customer-id saving logic were not changed.
- `README.md` was not updated because the repository does not currently contain one.
