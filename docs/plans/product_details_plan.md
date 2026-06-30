# Product Details Feature — Implementation Plan v2

## Overview

End-to-end implementation of the Product Details screen fetching real product data from the Shopify Admin REST API (academic use per `AGENTS.md`). Rendered via Clean Architecture + MVI with the **VogueVibe** design system. Screen starts in isolation for testing. All strings externalised to EN + AR.

**v2 additions (JETS Lab spec compliance):**
- Ratings & Reviews in domain model and UI State
- Wishlist (`isFavorite`) in domain model, state, and intent
- Guest access control — gates `AddToCart` and `OnToggleFavorite`

---

## Critical Decisions

**Admin Token**: Read from `BuildConfig.SHOPIFY_ADMIN_TOKEN` (backed by `local.properties`). The existing `NetworkModule` OkHttp interceptor already injects it. No `shpat_` token hardcoded in source. Only compliant path per `AGENTS.md §0`.

**Ratings & Reviews — Shopify limitation**: The Shopify Admin REST API does not return a rating or review list on `GET products/{id}.json`. Ratings are **mocked** in `ProductRepositoryImpl` (`rating = 4.8`, `reviewsCount = 120`). A comment marks the exact location for future Shopify Metafields/third-party replacement.

**Wishlist (`isFavorite`)**: `IWishlistRepository` does not yet exist. `isFavorite` is **mocked as `false`** in `ProductRepositoryImpl` with the same future-replacement comment. Full intent/state wiring is in place.

**Guest Access Control**: `IAuthRepository.isLoggedIn(): Boolean` already exists. `ProductDetailViewModel` injects `IAuthRepository` and calls `isLoggedIn()` before processing `AddToCart` and `OnToggleFavorite`. If guest → emits `ShowAuthRequiredError` effect.

**`runCatchingCancellable` is missing**: Defined in `domain/AGENTS.md §7` but absent from disk. This plan creates it.

---

## Files to Create / Modify

### Data Layer

| File | Action | Notes |
|---|---|---|
| `data/remote/dto/ProductDetailDto.kt` | NEW | `SingleProductResponse`, `ShopifyProductDetail`, `ImageDto`, `VariantDto` + `toDomain(rating, reviewsCount, isFavorite)` mapper |
| `data/remote/api/ProductApiService.kt` | MODIFY | Add `getProductDetail(@Path product_id: Long)` |
| `data/remote/datasource/IProductRemoteDataSource.kt` | MODIFY | Add `getProductDetail(productId: Long)` |
| `data/remote/datasource/ProductRemoteDataSourceImpl.kt` | MODIFY | Implement |
| `data/repository/ProductRepositoryImpl.kt` | MODIFY | Implement `getProductDetail()` with mocked rating/isFavorite |

### Domain Layer

| File | Action | Notes |
|---|---|---|
| `domain/common/result/RunCatchingCancellable.kt` | NEW | Missing utility — re-throws `CancellationException` |
| `domain/product/model/ProductDetail.kt` | NEW | Includes `rating`, `reviewsCount`, `isFavorite` |
| `domain/product/repository/IProductRepository.kt` | MODIFY | Add `getProductDetail(productId: Long): Result<ProductDetail>` |
| `domain/product/usecase/GetProductDetailUseCase.kt` | NEW | Single `invoke(productId: Long)` |

### Presentation Layer

| File | Action | Notes |
|---|---|---|
| `presentation/product/detail/ProductDetailUiState.kt` | NEW | Includes `rating`, `reviewsCount`, `isFavorite`, `selectedSize` |
| `presentation/product/detail/ProductDetailUiIntent.kt` | NEW | `LoadProduct`, `Retry`, `SelectSize`, `AddToCart`, `OnToggleFavorite` |
| `presentation/product/detail/ProductDetailUiEffect.kt` | NEW | `ShowToast`, `NavigateToCart`, `ShowAuthRequiredError` |
| `presentation/product/detail/ProductDetailViewModel.kt` | NEW | Injects `IAuthRepository` for guest check |
| `presentation/product/detail/components/ImageCarousel.kt` | NEW | `HorizontalPager` + dots |
| `presentation/product/detail/components/SizeSelector.kt` | NEW | `LazyRow` of `FilterChip` |
| `presentation/product/detail/components/StarRatingRow.kt` | NEW | Gold stars + rating + review count |
| `presentation/product/detail/ProductDetailScreen.kt` | NEW | Full VogueVibe UI, stateful root + stateless content |
| `presentation/src/main/res/values/strings.xml` | MODIFY | 16 new strings in `<!--Omar-->` block |
| `presentation/src/main/res/values-ar/strings.xml` | MODIFY | Arabic translations |

### App Layer

| File | Action | Notes |
|---|---|---|
| `app/di/UseCaseModule.kt` | MODIFY | Add `provideGetProductDetailUseCase` |
| `app/navigation/Route.kt` | MODIFY | Add `data class ProductDetailRoute(val productId: Long) : Route` |
| `app/navigation/NavGraph.kt` | MODIFY | Add composable + change `startDestination` |

### Unit Tests

| File | Action | Tests |
|---|---|---|
| `domain/src/test/.../GetProductDetailUseCaseTest.kt` | NEW | 3 cases: success, failure, CancellationException |
| `presentation/src/test/.../ProductDetailViewModelTest.kt` | NEW | 9 cases: Loading, Success, Error, SelectSize, AddToCart variants, guest restrictions, Retry |

---

## Execution Order

```
1.  RunCatchingCancellable.kt              [domain — NEW]
2.  ProductDetail.kt                       [domain — NEW]
3.  IProductRepository.kt                 [domain — MODIFY]
4.  GetProductDetailUseCase.kt             [domain — NEW]
5.  ProductDetailDto.kt                    [data — NEW]
6.  ProductApiService.kt                   [data — MODIFY]
7.  IProductRemoteDataSource.kt            [data — MODIFY]
8.  ProductRemoteDataSourceImpl.kt         [data — MODIFY]
9.  ProductRepositoryImpl.kt               [data — MODIFY]
10. ProductDetailUiState.kt               [presentation — NEW]
11. ProductDetailUiIntent.kt              [presentation — NEW]
12. ProductDetailUiEffect.kt              [presentation — NEW]
13. ProductDetailViewModel.kt             [presentation — NEW]
14. ImageCarousel.kt                      [presentation — NEW]
15. SizeSelector.kt                       [presentation — NEW]
16. StarRatingRow.kt                      [presentation — NEW]
17. ProductDetailScreen.kt                [presentation — NEW]
18. strings EN + AR                       [presentation — MODIFY]
19. UseCaseModule.kt                      [app — MODIFY]
20. Route.kt                              [app — MODIFY]
21. NavGraph.kt                           [app — MODIFY]
22. GetProductDetailUseCaseTest.kt        [domain test — NEW]
23. ProductDetailViewModelTest.kt         [presentation test — NEW]
```

## Verification Plan

### Automated
```
.\gradlew :domain:test
.\gradlew :presentation:test
.\gradlew assembleDebug
```

### Manual
Launch app → `ProductDetailScreen` opens directly → Adidas Backpack renders:
- Image carousel + back arrow + heart icon
- Vendor, Title (Playfair Display), Price (Inter)
- Star rating row: 4.8 (120 reviews)
- Selectable size chips (Champagne Gold when selected)
- Description body
- Add to Cart button — guest shows auth error, no size shows toast, logged in + size = cart navigation
