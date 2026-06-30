# Profile + Settings Implementation Plan

## Scope

This is a plan-only document for the WearZone Profile and Settings feature. No Kotlin, Gradle, XML, string resource, navigation, DI, test, repository, use case, ViewModel, UI state, UI effect, or implementation files should be changed until this plan is reviewed and approved.

Primary design reference:
- Profile screen screenshot from `stitch_luxecart_premium_shopify_commerce/screen.png`

Secondary design references:
- Logout confirmation screenshot from `stitch_luxecart_premium_shopify_commerce (1)/screen.png`
- Settings style reference from `stitch_luxecart_premium_shopify_commerce (2)/screen.png`
- VogueVibe design tokens from the provided `DESIGN.md` files

## Requirements

- Profile is the main required screen.
- Logout remains only in Profile.
- Logout must show the confirmation design before executing.
- Settings is a new planned screen because the current Figma has no separate Settings screen.
- Settings must use the same minimal premium visual style as Profile and the existing app screens.
- Settings contains app/user preferences only:
  - Theme mode: Light, Dark, System Default
  - Notifications toggle
  - Language preference / future support
  - App version
  - Terms & Conditions / Privacy Policy as TODO if not implemented
- Settings must not duplicate Logout.

## Current Repo Observations

- The app currently has four modules: `app`, `data`, `domain`, and `presentation`.
- Type-safe route objects already exist in `app/src/main/kotlin/com/example/wearzone/navigation/Route.kt`, but only onboarding, register, login, and home routes are currently declared.
- `NavGraph.kt` currently wires only onboarding, register, login, and home.
- `HomeScreen.kt` includes a static bottom bar with a Profile tab, but the Profile tab has no navigation callback yet.
- There is no existing `presentation/account`, `presentation/profile`, or `presentation/settings` feature package.
- `IAuthRepository` exposes `getCurrentUser()` but does not expose logout yet.
- `AuthRemoteDataSourceImpl` uses Firebase Auth and can be extended later with a `signOut()` wrapper.
- Existing theme files already include VogueVibe-style colors, Playfair Display, Inter, and rounded shapes.
- Existing strings are localized through `presentation/src/main/res/values/strings.xml` and `values-ar/strings.xml`; future UI strings must follow that pattern.

## Architecture Mapping

Relevant `AGENTS.md` sections:
- Account/Profile behavior maps to section 13.8: personalized greeting, recent orders, wishlist preview, currency selector, logout confirmation.
- Logout confirmation maps to section 13.9: destructive actions must emit confirmation first.
- MVI implementation maps to section 3: `UiState`, `UiIntent`, `UiEffect`, `StateFlow`, and `Channel`.
- Settings preferences map to section 1.5 and 8.3: DataStore Preferences for non-sensitive app/user preferences.
- Navigation must remain type-safe and stay in `Route.kt` plus `NavGraph.kt`.

## Proposed Feature Shape

### Profile Screen

Design intent:
- Soft off-white background.
- Centered brand title in Playfair Display.
- Header row with menu icon, brand title, and bag/cart icon.
- User identity block with circular avatar, greeting, and email.
- Recent orders section with two horizontal cards.
- Account action rows:
  - My Orders
  - Wishlist
  - Saved Addresses
  - Currency with current value
  - Settings
  - Logout
- Bottom navigation with Profile selected.

Behavior:
- On screen load, read the current authenticated user.
- If a user exists, show display name and email.
- If no user exists, show guest-safe fallback text and route protected actions to Login where needed.
- Recent order cards can initially use static UI models until order history is implemented.
- Logout tap emits a `ShowLogoutConfirmation` effect first.
- Confirming logout sends a separate `ConfirmLogout` intent.
- Successful logout emits `NavigateToLogin`.
- Failed logout emits a localized snackbar/error effect.

### Logout Confirmation

Design intent:
- Use the provided confirmation screenshot only for Profile logout.
- Dim/blur the Profile background.
- Center a white rounded modal/sheet.
- Icon at top, title, supporting message.
- Primary black `Sign Out` button.
- Secondary outlined `Cancel` button.

Behavior:
- Dialog opens only after the Profile ViewModel emits an effect.
- Execution happens only after explicit confirmation.
- Dismiss/cancel does not call logout.

### Settings Screen

Design intent:
- Match the Profile visual language: off-white background, serif page title, clean white grouped panels, thin dividers, black active controls.
- Top bar with back arrow, brand title, and bag/cart icon.
- No logout row or logout button.

Sections:
- Appearance:
  - System Default
  - Light Mode
  - Dark Mode
- Preferences:
  - Notifications toggle
  - Language row with current value, initially English
  - Currency row if current app preference flow supports it later; otherwise keep currency only in Profile for this phase
- About:
  - Privacy Policy, TODO navigation/action if not implemented
  - Terms & Conditions, TODO navigation/action if not implemented
  - App Version, initially `1.0.0` or sourced from app build config when wired

Behavior:
- Theme mode selection updates Settings state and persists when the Settings repository is implemented.
- Notifications toggle updates state and persists when the Settings repository is implemented.
- Language row can emit a TODO snackbar/effect until language selection is implemented.
- Privacy and Terms rows can emit TODO snackbar/effects until document screens/links exist.

## Proposed Future File Changes

Do not make these changes yet. They are checklist items for the later implementation task.

### Domain

- [ ] Add `domain/src/main/kotlin/com/example/wearzone/domain/auth/usecase/LogoutUseCase.kt`.
- [ ] Extend `IAuthRepository` with `suspend fun logout(): Result<Unit>`.
- [ ] Add settings domain models, for example `SettingsPreferences` and `ThemeMode`.
- [ ] Add `ISettingsRepository` for non-sensitive preferences.
- [ ] Add settings use cases:
  - [ ] `ObserveSettingsPreferencesUseCase`
  - [ ] `SetThemeModeUseCase`
  - [ ] `SetNotificationsEnabledUseCase`
  - [ ] `SetLanguageUseCase`

### Data

- [ ] Extend `IAuthRemoteDataSource` with a sign-out method.
- [ ] Implement Firebase sign-out in `AuthRemoteDataSourceImpl`.
- [ ] Implement `AuthRepositoryImpl.logout()` with dispatcher handling in the data layer.
- [ ] Add a DataStore Preferences data source for settings preferences.
- [ ] Add `SettingsRepositoryImpl` that maps DataStore values to domain settings models.
- [ ] Bind the new settings repository in `RepositoryModule`.

### App Navigation and DI

- [ ] Add `ProfileRoute` and `SettingsRoute` to the existing type-safe route sealed interface.
- [ ] Add Profile and Settings destinations in `NavGraph.kt`.
- [ ] Wire Home bottom Profile tab to navigate to Profile.
- [ ] Wire Profile Settings row to navigate to Settings.
- [ ] Wire Profile logout success to Login with the correct back-stack cleanup.
- [ ] Provide any new use cases in `UseCaseModule`.
- [ ] Avoid adding a separate nav file.

### Presentation - Profile

- [ ] Create `presentation/src/main/kotlin/com/example/wearzone/presentation/profile/ProfileUiState.kt`.
- [ ] Create `ProfileUiIntent.kt`.
- [ ] Create `ProfileUiEffect.kt`.
- [ ] Create `ProfileViewModel.kt`.
- [ ] Create `ProfileScreen.kt`.
- [ ] Create focused components only if needed, for example:
  - [ ] `ProfileHeader.kt`
  - [ ] `RecentOrderCard.kt`
  - [ ] `ProfileMenuRow.kt`
  - [ ] `LogoutConfirmationDialog.kt`
- [ ] Keep `ProfileScreen` as the only stateful composable that touches the ViewModel.
- [ ] Keep content/components stateless.
- [ ] Use `StateFlow<ProfileUiState>` and `Channel<ProfileUiEffect>`.
- [ ] Use string resources for all user-facing text.

### Presentation - Settings

- [ ] Create `presentation/src/main/kotlin/com/example/wearzone/presentation/settings/SettingsUiState.kt`.
- [ ] Create `SettingsUiIntent.kt`.
- [ ] Create `SettingsUiEffect.kt`.
- [ ] Create `SettingsViewModel.kt`.
- [ ] Create `SettingsScreen.kt`.
- [ ] Create focused components only if needed, for example:
  - [ ] `SettingsSection.kt`
  - [ ] `ThemeModeRow.kt`
  - [ ] `SettingsPreferenceRow.kt`
- [ ] Do not add logout to Settings.
- [ ] Use string resources for all user-facing text.

### Resources

- [ ] Add all Profile strings to `presentation/src/main/res/values/strings.xml`.
- [ ] Add Arabic translations or placeholders to `values-ar/strings.xml`.
- [ ] Add content descriptions for avatar, order images, navigation icons, cart icon, and settings controls.
- [ ] Prefer existing drawable assets for placeholder order/profile visuals unless design-approved product/order assets are added later.

### Tests

- [ ] Add `ProfileViewModelTest`.
- [ ] Add `SettingsViewModelTest`.
- [ ] Add `LogoutUseCaseTest`.
- [ ] Add settings use case tests if settings persistence is implemented in the same task.
- [ ] Test Profile logout flow:
  - [ ] tapping logout emits confirmation effect
  - [ ] cancel does not call logout
  - [ ] confirm calls logout use case
  - [ ] success emits navigation to login
  - [ ] failure emits localized error effect
- [ ] Test Settings preference state transitions:
  - [ ] theme mode selection
  - [ ] notifications toggle
  - [ ] language TODO effect
  - [ ] privacy/terms TODO effect

## Proposed MVI Contracts

### ProfileUiState

- `Loading`
- `Content`
  - user display name
  - user email
  - avatar URL or placeholder flag
  - recent order UI models as an immutable list
  - selected currency
- `Error`

Note: if recent orders are static placeholders in the first pass, still store them as `ImmutableList` in state.

### ProfileUiIntent

- `OnMyOrdersClicked`
- `OnWishlistClicked`
- `OnSavedAddressesClicked`
- `OnCurrencyClicked`
- `OnSettingsClicked`
- `OnLogoutClicked`
- `OnLogoutConfirmed`
- `OnLogoutCancelled`
- `OnRetry`

### ProfileUiEffect

- `NavigateToLogin`
- `NavigateToSettings`
- `NavigateToWishlist`
- `NavigateToOrders`
- `NavigateToSavedAddresses`
- `ShowLogoutConfirmation`
- `ShowError`

### SettingsUiState

- `Loading`
- `Content`
  - selected theme mode
  - notifications enabled
  - selected language label
  - app version label
- `Error`

### SettingsUiIntent

- `OnBackClicked`
- `OnThemeModeSelected`
- `OnNotificationsToggled`
- `OnLanguageClicked`
- `OnPrivacyPolicyClicked`
- `OnTermsClicked`
- `OnRetry`

### SettingsUiEffect

- `NavigateBack`
- `ShowLanguageComingSoon`
- `ShowPrivacyPolicyTodo`
- `ShowTermsTodo`
- `ShowError`

## Design Notes

- Use `AppColors` and `MaterialTheme.colorScheme`; do not hardcode colors in composables.
- Use existing `AppTypography` styles where possible.
- For the brand title, use Playfair Display via the existing typography setup.
- Keep touch targets at least 48 dp.
- Use icons from Material Icons already present in the project.
- Keep Profile card imagery visual but lightweight; use existing drawable assets unless new assets are approved.
- Avoid card nesting.
- Use full-width bands/sections and clean grouped panels.

## Open Decisions

- Decide whether the first Profile implementation should show static recent order UI models or wait for account/order history data APIs.
- Decide whether Settings persistence is required in the first implementation or can start as ViewModel-only state with repository work in a follow-up.
- Decide how to source app version in presentation without violating module boundaries. Preferred options are a small domain/app-info provider or an app-level callback/value passed through DI.
- Decide whether Currency remains only in Profile for this phase or is also listed in Settings. The user requested Settings preferences only and explicitly included no logout; currency was in the design reference but not in the explicit Settings requirement list.

## Verification Plan For Later Implementation

- Run targeted domain tests after adding logout/settings use cases.
- Run targeted presentation ViewModel tests after adding Profile and Settings ViewModels.
- Build the app after wiring navigation and DI.
- Manually verify Profile:
  - profile opens from Home bottom nav
  - Profile visual structure matches the main design reference
  - Settings row opens Settings
  - Logout opens confirmation only from Profile
  - Cancel dismisses confirmation
  - Confirm logs out and navigates to Login
- Manually verify Settings:
  - no logout appears anywhere on Settings
  - theme choices update selected state
  - notifications toggle updates state
  - language, privacy, and terms TODO actions are non-crashing
  - app version is visible

