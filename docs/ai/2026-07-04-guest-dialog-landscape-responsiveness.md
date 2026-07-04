# Guest Dialog Landscape Responsiveness

## Summary
- Updated the shared guest sign-in-required dialog to stay centered and width-limited in landscape.
- Added safe-area padding and a scrollable card body so actions remain reachable on compact heights.
- Preserved the existing rounded card, top gradient, lock badge, title, message, and primary/secondary actions.

## Verification
- `.\gradlew.bat assembleDebug` passed.
