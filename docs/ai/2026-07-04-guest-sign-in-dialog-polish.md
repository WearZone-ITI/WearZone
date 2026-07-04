# Guest Sign-In Dialog Polish

## Summary
- Reworked the shared `SignInRequiredDialog` into a premium lock-card dialog with a soft gradient header, circular lock badge, primary sign-in/register action, and secondary continue-browsing action.
- Reused the shared dialog for guest-required cart, wishlist, checkout, product detail, home, vendor products, order history, order details, saved addresses, and address form states.
- Updated Profile guest behavior so the old guest profile panel is no longer rendered. Guest Profile now emits and shows the shared sign-in-required dialog immediately.

## Behavior
- `SIGN IN / REGISTER` routes through the existing login entry point.
- `CONTINUE BROWSING` dismisses the dialog. On the protected Profile tab it returns to the public Home tab through the existing callback.
- Guest guards remain in the existing ViewModels and protected screens; no Firebase Auth, Google Sign-In, Shopify token, or Shopify customer-id logic was changed.

## Verification
- `.\gradlew.bat assembleDebug` passed.

## Notes
- `README.md` was not updated because the repository does not currently contain one.
