# Real-Time Client Review Loop (Firebase Firestore)

This plan outlines the implementation of a real-time product review feature using Firebase Firestore within the Clean Architecture MVI structure.

## Proposed Changes

---

### Data Layer (Firestore Integration)

The Firestore dependency is already present in `data/build.gradle.kts`.

#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/product/model/ClientReview.kt`
- Create `ClientReview` domain model (id, productId, shopperName, rating, comment, timestamp).

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/remote/dto/ClientReviewDto.kt`
- Create `ClientReviewDto` matching the domain model with mappings to/from Domain.

#### [NEW] `domain/src/main/kotlin/com/example/wearzone/domain/product/repository/IReviewRepository.kt`
- Define `getReviewsForProduct(productId: String): Flow<List<ClientReview>>`
- Define `addReview(productId: String, shopperName: String, rating: Double, comment: String): DataResult<Unit>`

#### [NEW] `data/src/main/kotlin/com/example/wearzone/data/repository/ReviewRepositoryImpl.kt`
- Implement `IReviewRepository`.
- Inject `FirebaseFirestore`.
- Implement `getReviewsForProduct` using Firestore's `snapshots()` extension (from `kotlinx-coroutines-play-services`) to return a `Flow`.
- Implement `addReview` using `add()` to the `"reviews"` collection.

#### [MODIFY] `app/src/main/kotlin/com/example/wearzone/di/RepositoryModule.kt`
- Bind `IReviewRepository` to `ReviewRepositoryImpl`.

---

### Presentation Layer (ViewModel)

#### [MODIFY] `presentation/src/main/kotlin/com/example/wearzone/presentation/product/detail/ProductDetailUiState.kt`
- Update `ProductDetailUiState.Success` to include:
  - `reviews: ImmutableList<ClientReviewUiModel>`
  - `averageRating: Double`
  - `totalReviewCount: Int`
- Create `ClientReviewUiModel` to hold the pre-formatted UI strings (e.g., date).

#### [MODIFY] `presentation/src/main/kotlin/com/example/wearzone/presentation/product/detail/ProductDetailUiIntent.kt`
- Add `data class SubmitReview(val rating: Double, val comment: String) : ProductDetailUiIntent`

#### [MODIFY] `presentation/src/main/kotlin/com/example/wearzone/presentation/product/detail/ProductDetailViewModel.kt`
- Inject `IReviewRepository`.
- Collect the `getReviewsForProduct` Flow.
- On each emission, calculate `averageRating` and `totalReviewCount`. Update the `UiState.Success`.
- Handle `SubmitReview` intent, call `addReview` with a placeholder name.

---

### Compose UI Integration

#### [MODIFY] `presentation/src/main/kotlin/com/example/wearzone/presentation/product/detail/ProductDetailScreen.kt`
- Render "Customer Reviews" section under description.
- Display `averageRating` and `totalReviewCount`.
- Render List/Column of reviews.
- Add a "Write a Review" button.
- Create a `ModalBottomSheet` containing a custom 1-5 star rating selector, a `TextField` for comments, and a Submit button.

---

## Phase 2: Premium UI/UX Overhaul

This phase covers refactoring `ProductDetailScreen.kt` and its components to match premium, modern e-commerce application standards.

### 1. Edge-to-Edge Image Header & Floating Buttons
- Remove the Scaffold TopAppBar.
- Stretch the product image/carousel edge-to-edge behind the transparent status bar.
- Floating back and favorite buttons over the image using a `Box` with a frosted/translucent glass background (`MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)`).

### 2. Premium Skeleton Loading (Shimmer Effect)
- Replace generic `CircularProgressIndicator` with a shimmer skeleton screen (`ProductDetailShimmer.kt`).
- Use an infinite transition color/alpha pulse based on `MaterialTheme.colorScheme` (e.g., pulsing between `surface` and `surfaceVariant`).
- Introduce a 1-second delay in `ProductDetailViewModel.kt` during loading to showcase the skeleton screen.

### 3. Modern Content Layout
- Wrap details in a `Surface` with `RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)`.
- Use a negative offset (`offset(y = (-32).dp)`) to overlap the surface over the bottom of the image header.
- Clean typography hierarchy using Material 3 standard roles.

### 4. Polished Customer Reviews & Bottom Bar
- Refactor `ReviewListCard` with elevation, rounded corner shape, and circular letter-avatar placeholders.
- Integrate the "Write a Review" button directly into the persistent `BottomAppBar` next to the "Add to Cart" button, creating a unified action area.

