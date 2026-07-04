# Pending Protected Route Saveable

## Summary
- Changed `pendingProtectedRoute` in the root navigation graph from `remember` to `rememberSaveable`.
- Made the sealed `Route` type implement `java.io.Serializable` so pending protected destinations can be saved in instance state.

## Behavior
- Guest protected navigation still stores the intended protected destination before opening Login/Register.
- After a configuration change while on auth, successful login/register still navigates to the stored protected destination.
- Existing Login/Register/Auth, Guest Mode, and navigation behavior were otherwise unchanged.

## Verification
- `.\gradlew.bat assembleDebug` passed.
