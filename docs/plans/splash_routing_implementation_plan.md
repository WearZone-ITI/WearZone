# Finalize Splash Screen & Startup Routing Logic

This plan outlines the steps to update the Splash Screen visuals and implement dynamic startup routing logic based on the user's onboarding completion and login state.

## User Review Required

> [!IMPORTANT]
> The navigation destinations from the splash screen will be managed cleanly inside `NavGraph.kt` via callbacks passed to `SplashScreen` (`onNavigateToOnboarding`, `onNavigateToLogin`, `onNavigateToMain`). This separates Compose UI/ViewModel logic from Jetpack Compose Navigation.

## Proposed Changes

---

### UI & Color Palette Updates

#### [MODIFY] [WearZoneAnimatedLoader.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/common/WearZoneAnimatedLoader.kt)
- Change the Lottie dynamic property tint for the keyPath `"BG"` from a faint background color to `Color.Transparent` to eliminate any baked-in background rings/shapes.
- Confirm the main shopping bag and sparkles are tinted to ChampagneGold, and inner elements are tinted to the contrasting theme text/foreground color.

#### [MODIFY] [SplashScreen.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/splash/SplashScreen.kt)
- Update background color to `#F9F9F9` (`Color(0xFFF9F9F9)`).
- Inject `SplashViewModel` (using Hilt).
- Set up a `LaunchedEffect` to collect the `SplashUiEffect` from the ViewModel's `uiEffect` flow.
- Add an artificial `delay(2000)` inside the splash effect handler to ensure the loader animation is visible and appreciated before transitioning.
- Trigger the corresponding callbacks: `onNavigateToOnboarding`, `onNavigateToLogin`, or `onNavigateToMain`.

---

### Startup Routing Logic (MVI Contract & ViewModel)

#### [NEW] [SplashUiState.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/splash/SplashUiState.kt)
- Create the basic interface for MVI state:
  ```kotlin
  package com.example.wearzone.presentation.splash

  sealed interface SplashUiState {
      data object Initial : SplashUiState
  }
  ```

#### [NEW] [SplashUiIntent.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/splash/SplashUiIntent.kt)
- Create the basic interface for MVI intent:
  ```kotlin
  package com.example.wearzone.presentation.splash

  sealed interface SplashUiIntent {
      data object Init : SplashUiIntent
  }
  ```

#### [NEW] [SplashUiEffect.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/splash/SplashUiEffect.kt)
- Create the basic interface for MVI one-shot navigation side-effects:
  ```kotlin
  package com.example.wearzone.presentation.splash

  sealed interface SplashUiEffect {
      data object NavigateToOnboarding : SplashUiEffect
      data object NavigateToLogin : SplashUiEffect
      data object NavigateToMain : SplashUiEffect
  }
  ```

#### [NEW] [SplashViewModel.kt](file:///d:/projects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/splash/SplashViewModel.kt)
- Create the Hilt ViewModel `SplashViewModel` injecting:
  - `ObserveOnboardingCompletedUseCase` (to get `isOnboardingCompleted`)
  - `IAuthRepository` (to check `isLoggedIn()`)
- On initialization, trigger the `Init` intent.
- Fetch the first emitted value from `observeOnboardingCompletedUseCase()`.
- Check if logged in via `authRepository.isLoggedIn()`.
- Emit the corresponding `SplashUiEffect`.

---

### Navigation Graph Integration

#### [MODIFY] [NavGraph.kt](file:///d:/projects/WearZone/app/src/main/kotlin/com/example/wearzone/navigation/NavGraph.kt)
- Update `composable<Route.SplashRoute>` block to:
  - Render the new `SplashScreen`.
  - Provide lambdas that navigate to `Route.OnboardingRoute`, `Route.LoginRoute`, or `Route.MainRoute`.
  - Pop `Route.SplashRoute` off the backstack with `inclusive = true` on navigation.

## Verification Plan

### Automated Verification
- Run `./gradlew clean assembleDebug` to ensure compilation and dependency resolution are correct.
- Verify unit tests by running `./gradlew test`.
