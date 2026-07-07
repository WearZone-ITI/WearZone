# UI/Theme Refactor Plan

## 1. Project Structure Snapshot
The following key files have been identified for modification:
- **Theme & Colors:**
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/common/theme/AppColors.kt`
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/common/theme/AppTheme.kt`
- **Typography:**
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/common/theme/AppTypography.kt`
- **Navigation:**
  - `app/src/main/kotlin/com/example/wearzone/navigation/MainScreen.kt`
- **Home Screen:**
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/home/HomeScreen.kt`
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/common/TopBar.kt`
- **Empty States (To be created/modified):**
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/common/PremiumEmptyState.kt` (New)
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/search/SearchScreen.kt`
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/cart/CartScreen.kt`
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/wishlist/WishlistScreen.kt`
- **Shimmer Effects (To be created/modified):**
  - `presentation/src/main/kotlin/com/example/wearzone/presentation/common/ShimmerComponents.kt` (New)
  - Multiple screens to replace `CircularProgressIndicator` with shimmer layouts.
- **Localization:**
  - `app/src/main/res/values/strings.xml`
  - Various Compose screens containing hardcoded strings.

## 2. Dependency Audit
- **Lottie:** Found `com.airbnb.android:lottie-compose:6.4.0` in `gradle/libs.versions.toml` and it is implemented in `presentation/build.gradle.kts`.
- **Google Fonts:** Found `androidx.compose.ui:ui-text-google-fonts` in `gradle/libs.versions.toml` and it is implemented in `presentation/build.gradle.kts`.
Both dependencies are successfully set up and ready to use.

## 3. Task Breakdown & Execution Order

**Task 1: Premium Dark Theme & Color Cleanup**
- **Files:** `AppColors.kt`, `AppTheme.kt`, and all UI screens.
- **Action:** Update `WearZoneDarkAppColors` and `WearZoneDarkColors` to use deep blacks (`#000000`, `#141414`), crisp whites/grays for text, and Gold (`#D4AF37`) for accents. Leave `WearZoneLightAppColors` and `WearZoneLightColors` completely untouched. Scan all screens and replace hardcoded colors with `MaterialTheme.colorScheme` or `AppTheme.colors`.

**Task 2: Typography Refactor**
- **Files:** `AppTypography.kt`, and all UI screens.
- **Action:** Replace `Inter` with `Outfit` as the primary font, and ensure `Playfair Display` is used as the secondary/accent font for hero headings. Clean up any hardcoded `TextStyle` in Composables to strictly use `MaterialTheme.typography`.

**Task 3: Material 3 Bottom Navigation Bar**
- **Files:** `MainScreen.kt`
- **Action:** Refactor `BottomNavigationBar` (currently in `MainScreen.kt`) to strictly follow Material 3 guidelines. Use the `surface` color for background, Gold (`#D4AF37` with 15% opacity) for the indicator, Gold for selected icons, and neutral gray for unselected. Prepare for custom vector drawables by using `painterResource`.

**Task 4: Home Screen UI Cleanup**
- **Files:** `HomeScreen.kt`, `TopBar.kt`
- **Action:** Remove the hamburger menu from `TopBar`. In `HomeScreen`, locate and completely delete `PromoAdsCarousel` (or the equivalent promo card immediately below the Search Bar) so the flow directly goes from Search Bar to the next section.

**Task 5: Premium Lottie Empty-State Placeholders**
- **Files:** `PremiumEmptyState.kt` (New), `SearchScreen.kt`, `CartScreen.kt`, `WishlistScreen.kt`
- **Action:** Create `PremiumEmptyState` Composable that takes a Lottie `@RawRes`, Title, Description, and an optional CTA button. Apply this component to the empty states of the Search, Cart, and Wishlist screens.

**Task 6: Universal Shimmer Loading Effects**
- **Files:** `ShimmerComponents.kt` (New), and various UI screens (Home, Categories, Search, Wishlist, Cart).
- **Action:** Build a reusable `Modifier.shimmerEffect()`. Create dummy shimmer layouts for product cards, lists, and grids. Replace all instances of `CircularProgressIndicator` with these skeletons across the app.

**Task 7: String Localization Cleanup**
- **Files:** All UI screens and `strings.xml`.
- **Action:** Systematically scan all Compose files for hardcoded strings. Extract them to `strings.xml` and replace them with `stringResource(id = R.string.X)`.

## 4. Final Quality Checklist
- [ ] Light Theme left completely untouched and intact.
- [ ] No hardcoded colors — all colors use `MaterialTheme.colorScheme` or `AppTheme.colors`.
- [ ] No hardcoded `TextStyle` — all text uses `MaterialTheme.typography`.
- [ ] No hardcoded strings — all text uses `stringResource()`.
- [ ] Bottom Nav uses M3.
- [ ] Shimmer applied everywhere.

---
> [!IMPORTANT]
> User Review Required. Please review this plan and provide explicit approval to begin Task 1.
