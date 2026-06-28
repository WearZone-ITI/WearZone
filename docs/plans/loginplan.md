# Login Feature — Implementation Plan

## Goal

Implement the Login screen for WearZone following the exact Figma design (VogueVibe style screenshot provided by the user). This includes updating the UI to match the typography (`Playfair Display` and `Inter`), background gradients, input field styles, and adding social login logos (Google and Apple). Additionally, implement the full logic for Firebase Authentication using Email/Password and Google Sign-In via Android Credential Manager, adhering to Clean Architecture + MVI compliance per `AGENTS.md`.

---

## User Review Required

> [!IMPORTANT]
> **Google Credential Manager Implementation:** Google Sign-In requires a Web Client ID string to be available. We will fetch this from the `google-services.json` default web client ID string.
> **Apple Sign-In:** Apple Sign-In requires an Apple Developer account and significant backend configuration. For this phase, the Apple button will be visually implemented but will trigger a "Coming Soon" snackbar.
> **Fonts:** We will add `androidx.compose.ui:ui-text-google-fonts` to fetch "Playfair Display" and "Inter" dynamically, avoiding inflating the APK size with raw TTF/OTF files.

---

## Proposed Changes

### Component 1: UI & Theming Updates (Presentation Layer)

#### [MODIFY] `gradle/libs.versions.toml` & `presentation/build.gradle.kts`
- Add `androidx.compose.ui:ui-text-google-fonts` to support dynamic loading of `Playfair Display` and `Inter`.

#### [MODIFY] `presentation/src/main/java/com/example/presentation/common/theme/AppTypography.kt`
- Configure Google Fonts Provider.
- Define `headlineLarge` (40px, Bold, Playfair Display) for the "VogueVibe" title.
- Define `titleLarge` (28px, SemiBold, Playfair Display) for "Welcome Back".
- Define `bodyLarge` (16px, Regular, Inter) for the subtitle.
- Define `labelMedium` (14px, Medium, Inter) for text links and buttons.
- Define `labelSmall` (12px, SemiBold, Inter, uppercase, 1.2px letter-spacing) for the "OR CONTINUE WITH" divider text.

#### [MODIFY] `presentation/src/main/java/com/example/presentation/common/theme/AppColors.kt`
- Add the required colors:
  - `Background` = `#FBF9F9`
  - `InputBackground` = `#FFFFFF`
  - `TextPrimary` = `#000000`
  - `TextSecondary` = `#76777B`
  - `InputBorder` = `#6B7280`
  - `DividerColor` = `#E9E8E7`
  - `ButtonBackground` = `#1A1A1B`

#### [NEW] Vector Assets (Google & Apple Logos)
- Add `ic_google.xml` and `ic_apple.xml` in `presentation/src/main/res/drawable/`.

### Component 2: Login Screen Layout Reconstruction

#### [MODIFY] `presentation/src/main/java/com/example/presentation/auth/login/LoginScreen.kt`
- Restructure the UI to exactly match the provided screenshot:
  - Background: `linear-gradient(0deg, #FBF9F9, #FBF9F9), #FFFFFF`
  - Header: "VogueVibe" centered at the top.
  - Subtitle: "Welcome Back" & "Sign in to access your curated collection."
  - Form Fields: Transparent background, bottom border (`1px solid #6B7280`), floating labels ("Email Address", "Password") with an eye icon suffix for password.
  - "Forgot Password?" right-aligned.
  - Primary "Sign In" button: `#1A1A1B` background, white text, 18dp rounded corners.
  - Divider: "OR CONTINUE WITH" centered between two 1px `#E9E8E7` horizontal lines.
  - Social Buttons: Row with Google and Apple buttons (18dp rounded corners, `1px solid #1A1A1B` border).
  - Bottom Text: "Don't have an account? Register" (Register in bold/black).

### Component 3: Google Credential Manager Integration

#### [MODIFY] `presentation/src/main/java/com/example/presentation/auth/login/LoginScreen.kt`
- Implement a `LaunchedEffect` that observes the `LoginUiEffect.LaunchGoogleSignIn` effect.
- Use `androidx.credentials.CredentialManager` to launch a `GetCredentialRequest` with `GetGoogleIdOption`.
- Extract the ID Token from the successful credential response and dispatch `LoginUiIntent.OnGoogleSignInResult(idToken)`.

#### [MODIFY] `presentation/src/main/java/com/example/presentation/auth/login/LoginViewModel.kt`
- Verify the Email/Password and Google Sign-In intent handlers are correctly forwarding requests to `LoginWithEmailUseCase` and `LoginWithGoogleUseCase`.
- Update error handling to send user-friendly snackbar messages for authentication failures.

---

## Verification Plan

### Automated Tests
- Run `LoginViewModelTest` to ensure UI state transitions (Idle -> Loading -> Success/Error) work correctly.

### Manual Verification
1. Launch the app and navigate to the Login screen.
2. Verify the typography, colors, and layout exactly match the "VogueVibe" screenshot.
3. Attempt an invalid Email/Password login and verify the error snackbar.
4. Attempt a valid Email/Password login and verify navigation to Home.
5. Click the Google Sign-In button, verify the Google account picker appears, select an account, and verify successful login/navigation.
6. Click the Apple Sign-In button and verify the "Coming Soon" snackbar appears.
