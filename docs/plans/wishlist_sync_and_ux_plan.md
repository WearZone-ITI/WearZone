# Wishlist Real-Time Sync & UX Enhancements Plan

## Objective
Implement a robust, real-time "Offline-First" synchronization mechanism for the Wishlist feature using Firebase `ValueEventListener` to ensure data consistency across multiple devices simultaneously. Additionally, enhance the user experience by adding a reusable "Remove from Favorites" confirmation dialog, "Added to favorites" feedback via snackbars, and a new empty state UI in the Wishlist screen that follows the VogueVibe design system.

## Phase 1: Data Layer (Offline-First Real-Time Architecture)
1. **Local DB (Room):**
   - Add a synchronous `getWishlist(userId: String): List<WishlistEntity>` function to `WishlistDao` for single-shot fetching.
2. **Remote DB (Firebase) & Synchronization:**
   - Refactor `WishlistRepositoryImpl.getWishlistFlow(userId)` to use a `callbackFlow`.
   - Attach a `ValueEventListener` to `users/{userId}/wishlist` in Firebase Realtime Database.
   - Diff incoming remote data against local Room data.
   - Perform batch inserts and deletions in Room to mirror remote state.
   - Push new local state out of the `callbackFlow` to keep the UI reactive.
3. **Toggle Logic:**
   - Update `toggleFavorite(item)` to optimistically update Room immediately, then push the change to Firebase.

## Phase 2: UI Component Implementation (VogueVibe System)
1. **Remove Item Confirmation Dialog (`RemoveFavoriteDialog.kt`):**
   - Create a reusable modal component using Material 3 `AlertDialog` or standard `Dialog` matching VogueVibe aesthetic.
   - Include a destructive primary action ("Remove from Wishlist") and secondary action ("Keep in Wishlist").
   - Tie styling to `AppTheme.colors.selected` and appropriate `WearZoneColors`.
2. **Empty State UI (`WishlistEmptyState.kt`):**
   - Create an empty state referencing the minimalist HTML templates provided by VogueVibe.
   - Use `Icons.Outlined.FavoriteBorder` instead of a shopping bag.
   - Localize hardcoded texts using `R.string.wishlist_empty_title` and `R.string.wishlist_empty_subtitle`.
   - Ensure the "START SHOPPING" button is removed per late revision requests.

## Phase 3: State & Logic Integration (Presentation Layer)
1. **View Models (`HomeViewModel`, `ProductDetailViewModel`, `WishlistViewModel`):**
   - Add state fields for the confirmation dialog (e.g., `itemToRemove` or `showRemoveDialog`).
   - Implement `OnToggleFavorite` to first check if an item is being added or removed.
   - Handle `OnConfirmRemove` and `OnCancelRemove` user intents.
   - Fire `UiEffect.ShowSnackbar` (or Toast) when an item is successfully added to the wishlist.
2. **Screens (`HomeScreen`, `ProductDetailScreen`, `WishlistScreen`):**
   - Collect and display `RemoveFavoriteDialog` conditionally based on the respective `UiState`.
   - Replace the empty wishlist content in `WishlistScreen` with the newly created `WishlistEmptyState`.

## Phase 4: Code Refactoring & Quality Assurance
1. **Clean Imports:**
   - Scan all newly created/modified DI modules (`RepositoryModule`, `UseCaseModule`, `DatabaseModule`) and UI intents/viewmodels.
   - Replace fully-qualified class names with standard `import` statements to adhere to clean code standards.
2. **Localization:**
   - Verify that no English strings are hardcoded in the UI components, ensuring full Arabic translation support.
3. **Verification:**
   - Run `gradlew assembleDebug` to confirm there are zero compilation errors and all resource paths resolve successfully.
