# Home Module Implementation Plan

This plan details the implementation of the Home screen and its associated layers (Presentation, Domain, Data) in accordance with the `AGENT.md` rules.

## User Review Required

> [!WARNING]
> **API Key Usage:** You provided both Admin and Storefront API keys. As per `AGENT.md` (Security Notice), we will **ONLY** use the Storefront token (`94f4cb99e27e5fcdea2a4a380e19a38c`) in the Android app. It will be stored safely in `local.properties`. The Admin API token must NEVER be bundled in the APK.

> [!IMPORTANT]
> **UI Design:** You mentioned "ال ui اهو" but no image was attached in my context. I will build the UI based on standard modern e-commerce patterns and the components listed in `AGENT.md` (`CategoryRow`, `BrandRow`, `FeaturedProductsGrid`). If you have a specific Figma link or image, please provide it.

## Proposed Changes

### 1. Configuration & Security
- Update `local.properties` with `SHOPIFY_STOREFRONT_TOKEN=94f4cb99e27e5fcdea2a4a380e19a38c`
- Expose the token securely via `BuildConfig` in `app/build.gradle.kts`.

---

### 2. Domain Layer (Pure Kotlin)
*Focus: Models and UseCases for Home.*

#### [NEW] `domain/src/main/kotlin/com/wearzone/domain/product/model/Category.kt`
#### [NEW] `domain/src/main/kotlin/com/wearzone/domain/product/model/Brand.kt`
#### [NEW] `domain/src/main/kotlin/com/wearzone/domain/product/model/Product.kt`
#### [NEW] `domain/src/main/kotlin/com/wearzone/domain/product/repository/IProductRepository.kt`
#### [NEW] `domain/src/main/kotlin/com/wearzone/domain/product/usecase/GetProductsUseCase.kt`

---

### 3. Data Layer
*Focus: Storefront API integration via Apollo GraphQL (or Retrofit for REST path A).*

#### [NEW] `data/src/main/kotlin/com/wearzone/data/remote/api/ProductApiService.kt`
#### [NEW] `data/src/main/kotlin/com/wearzone/data/remote/dto/ProductDto.kt`
#### [NEW] `data/src/main/kotlin/com/wearzone/data/repository/ProductRepositoryImpl.kt`
#### [NEW] `data/src/main/kotlin/com/wearzone/data/remote/datasource/IProductRemoteDataSource.kt`
#### [NEW] `data/src/main/kotlin/com/wearzone/data/remote/datasource/ProductRemoteDataSourceImpl.kt`

---

### 4. Presentation Layer (Jetpack Compose + MVI)
*Focus: Unidirectional Data Flow using `StateFlow` and immutable collections.*

#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/HomeUiState.kt`
- Implements `sealed interface HomeUiState` with `Success` holding `ImmutableList` for categories, brands, and featured products.
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/HomeUiIntent.kt`
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/HomeUiEffect.kt`
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/HomeViewModel.kt`
- Manages state via `StateFlow` and effects via `Channel`.
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/HomeScreen.kt`
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/components/CategoryRow.kt`
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/components/BrandRow.kt`
#### [NEW] `presentation/src/main/kotlin/com/wearzone/presentation/home/components/FeaturedProductsGrid.kt`

## Verification Plan

### Automated Tests
- Unit tests for `HomeViewModel` using `Turbine` and `MockK`.
- Flow testing for `GetProductsUseCase`.

### Manual Verification
- Launch the app and navigate to the Home Screen.
- Verify categories, brands, and featured products load correctly using the Storefront token.
