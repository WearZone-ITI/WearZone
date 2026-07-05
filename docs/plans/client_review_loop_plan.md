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
