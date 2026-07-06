# Guest Mode Data Leak Fix

## Summary
- Continue as Guest now enters a clean guest session by using the existing logout/session-clearing path before navigating to the main shopping flow.
- Protected order and address screens now re-check `AuthAccessState` before resolving customer-scoped data.
- Guest users see sign-in-required states for orders, order details, saved addresses, and address forms instead of stale user data or technical customer-id errors.
- Cart remote draft-order sync now requires a current authenticated Firebase user and a saved Shopify customer id; guest cart changes remain local.

## Root Cause
Guest entry previously only navigated to the main flow. A previous Firebase session and saved Shopify customer id could remain available, so customer-scoped ViewModels and repositories treated the guest as the previous authenticated customer.

## Verification
- `./gradlew.bat :app:compileDebugKotlin`
- `./gradlew.bat :presentation:testDebugUnitTest`
- `./gradlew.bat :app:assembleDebug`
