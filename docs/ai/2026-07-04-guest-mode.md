# Guest Mode Implementation

## Summary
Implemented and tightened guest browsing and local cart support while keeping checkout, profile account data, wishlist, orders, addresses, and favorite actions protected behind the app-wide auth access state.

## Key Changes
- Added `AuthAccessState` and `GetAuthAccessStateUseCase` to distinguish guest, authenticated-without-customer-id, and authenticated customer access.
- Routed onboarding guest entry to the existing main shopping flow and added pending protected-route return for checkout after login/register.
- Allowed guests to view and update the local Room cart, and moved sign-in gating to checkout.
- Skipped remote draft-order cart sync when no saved Shopify customer id exists.
- Cleared saved draft-order id whenever customer session state is reset or replaced, while preserving the local cart.
- Allowed guest add-to-cart from Home, Product Detail, and Vendor Products while keeping wishlist/favorite actions protected.
- Blocked Wishlist from observing old per-user wishlist data unless the current access state is an authenticated Shopify customer.
- Added shared Sign In Required dialog usage for cart checkout, checkout, wishlist, profile protected actions, home favorites, product detail favorites, and vendor product favorites.
- Updated Profile guest state to show "Welcome to WearZone" with Sign In, Create Account, and Continue Browsing actions.

## Verification
- `.\gradlew.bat assembleDebug` passed.
- `.\gradlew.bat :presentation:testDebugUnitTest --tests com.example.presentation.productdetails.ProductDetailViewModelTest` passed.

## Notes
- Existing Firebase Auth, Google Sign-In, Shopify token handling, and Shopify customer-id saving logic were not changed.
- `README.md` was not updated because the repository does not currently contain one.
