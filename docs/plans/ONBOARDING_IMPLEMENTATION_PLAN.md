# Onboarding Implementation Plan

## Summary

Implement onboarding only, limited to:

- Three onboarding pages, each with a local drawable image, title, and description.
- Onboarding MVI contract: `UiState`, `UiIntent`, `UiEffect`, and `ViewModel`.
- DataStore Preferences boolean flag for onboarding completion only.
- Startup decision based only on onboarding completion.
- Type-safe Navigation Compose routes.
- Minimal Login placeholder because onboarding completion needs a destination.
- Focused `OnboardingViewModel` unit tests.

Current startup behavior:

- First launch -> Onboarding
- Onboarding completed -> Login placeholder

Future auth behavior is not implemented here:

- Onboarding completed + authenticated user -> Home
- Onboarding completed + unauthenticated user -> Login

This task must not implement authentication, Firebase Auth integration, token/session persistence, guest mode, Home routing, final auth architecture, or package migration.

## Current Project Structure Observations

- The repo has `app`, `data`, `domain`, and `presentation` modules.
- The current scaffold uses `com.example.*` packages.
- Package migration to `com.wearzone.*` is out of scope.
- Domain onboarding files are Kotlin (`.kt`) for this task.
- Current app start flow is `MainActivity -> App() -> NavGraph()`.
- Startup routing remains onboarding-only.

## Files To Create Or Rename

- Kotlin domain repository contract:
  - `domain/src/main/kotlin/com/example/domain/onboarding/repository/IAuthRepository.kt`
- Kotlin onboarding use cases:
  - `domain/src/main/kotlin/com/example/domain/onboarding/usecase/ObserveOnboardingCompletedUseCase.kt`
  - `domain/src/main/kotlin/com/example/domain/onboarding/usecase/SetOnboardingCompletedUseCase.kt`
- Rename data repository implementation:
  - `data/src/main/kotlin/com/example/data/repository/AuthRepositoryImpl.kt`
- Add local drawable onboarding images:
  - `presentation/src/main/res/drawable/onboarding_style_discovery.png`
  - `presentation/src/main/res/drawable/onboarding_shopping_flow.png`
  - `presentation/src/main/res/drawable/onboarding_delivery_moment.png`
- Add ViewModel tests:
  - `presentation/src/test/java/com/example/presentation/MainDispatcherRule.kt`
  - `presentation/src/test/java/com/example/presentation/onboarding/OnboardingViewModelTest.kt`

## Files To Modify

- `presentation/src/main/kotlin/com/example/presentation/onboarding/OnboardingScreen.kt`
  - Render three local-image onboarding pages.
  - Use simple page navigation with existing Compose state/ViewModel state.
  - Do not add Coil or a pager dependency.
- `presentation/src/main/kotlin/com/example/presentation/onboarding/OnboardingViewModel.kt`
  - Manage onboarding page state.
  - Save onboarding completion before navigating to Login.
  - Emit guest unavailable for Continue as Guest.
- `presentation/src/main/kotlin/com/example/presentation/onboarding/OnboardingUiState.kt`
  - Include current page.
- `presentation/src/main/kotlin/com/example/presentation/onboarding/OnboardingUiIntent.kt`
  - Include page navigation intents.
- `app/src/main/java/com/example/wearzone/di/RepositoryModule.kt`
  - Bind/provide `IAuthRepository` to `AuthRepositoryImpl`.
  - Remove old onboarding repository binding.
- `app/src/main/java/com/example/wearzone/di/DataModule.kt`
  - Bind/provide onboarding preferences data source to its implementation.
- `app/src/main/java/com/example/wearzone/navigation/NavGraph.kt`
  - Keep startup decision onboarding-only.
  - Navigate to Login after completion and clear onboarding from back stack.
  - Add TODO only for future auth-based routing.
- `presentation/build.gradle.kts` and version catalog
  - Add only focused coroutine test support if missing.

## Repository Scope

`IAuthRepository` is intentionally narrow for this task:

- `observeOnboardingCompleted()`
- `setOnboardingCompleted(completed)`

`IAuthRepository` must only manage the onboarding completion flag right now.

TODO for future task:

- Add real authentication methods after Firebase/auth work is approved.

## DataStore Responsibility

- Use Preferences DataStore for one boolean:
  - `has_seen_onboarding`
  - Default: `false`
- DataStore access stays in the data layer.
- Do not store auth token, user id, Firebase state, guest state, or session state.
- `AuthRepositoryImpl` delegates only to the onboarding preferences data source.

## Navigation Flow

Current task flow:

- App launches.
- Read onboarding completion flag.
- If `hasSeenOnboarding == false`, show `OnboardingRoute`.
- If `hasSeenOnboarding == true`, show `LoginRoute` placeholder.
- After onboarding completion, navigate to `LoginRoute` and remove onboarding from the back stack.

Future TODO:

- Replace temporary onboarding-only startup decision with combined onboarding + auth state.
- Add real `HomeRoute` only when Home/auth implementation exists.

## UI Behavior

- Page 1: image, title, description, Next.
- Page 2: image, title, description, Back and Next.
- Page 3: image, title, description, main CTA, and Continue as Guest unavailable action.
- Final page `Get Started`: save onboarding completion and navigate to Login.
- Do not show a `Sign In` button on onboarding while Login is only a placeholder and no auth flow exists.
- `Continue as Guest`: show guest mode unavailable, do not save, do not navigate.

## ViewModel Unit Tests

Required focused tests using `kotlinx-coroutines-test` and fakes:

- Initial state is `Idle`.
- Get Started on final onboarding page saves completion and emits `NavigateToLogin`.
- Write failure emits `ShowError` and does not navigate.
- Continue as Guest emits `ShowGuestModeUnavailable` and does not save or navigate.
- Page navigation updates the current onboarding page.

## Compliance Checklist

- No Firebase Auth added.
- No real authentication logic added.
- No token/session persistence added.
- No guest mode implementation added.
- No Home routing added.
- No package migration from `com.example.*` to `com.wearzone.*`.
- No Coil or new image-loading dependency added.
- No pager dependency added.