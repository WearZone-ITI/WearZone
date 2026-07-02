# Brands & Vendor Products Feature Plan

## Objective
Implement the "Brands/Vendors" feature, consisting of a "See All Brands" screen and a "Vendor Products" screen. Ensure strict adherence to Clean Architecture, MVI pattern, and the VogueVibe design system. Reuse existing components, specifically the `ProductCard`, and enforce Guest Access rules for cart and wishlist actions.

## 1. Domain Layer Updates
- **Models:** Use existing `Brand` and `Product` models.
- **Repositories (`IProductRepository`):**
  - Implement `getBrands(): DataResult<List<Brand>>`.
  - Implement `getProductsByVendor(vendor: String): DataResult<List<Product>>`.
- **Use Cases:**
  - Create `GetBrandsUseCase` to fetch the list of brands/smart collections.
  - Create `GetProductsByVendorUseCase` to fetch products filtered by vendor.

## 2. Presentation Layer
- **Brands Screen (`BrandsScreen.kt`):**
  - Grid view displaying all brand cards.
  - MVI elements: `BrandsUiState`, `BrandsUiIntent`, `BrandsUiEffect`.
  - `BrandsViewModel`: Loads brands on init, handles brand click to navigate.
  - Brand card uses `AsyncImage` with `ContentScale.Crop` to fill space, using `wearzone_img.png` as fallback.
  
- **Vendor Products Screen (`VendorProductsScreen.kt`):**
  - Grid view reusing `ProductCard` to display products for a specific brand.
  - MVI elements: `VendorProductsUiState`, `VendorProductsUiIntent`, `VendorProductsUiEffect`.
  - `VendorProductsViewModel`: 
    - Fetches products using `GetProductsByVendorUseCase`.
    - Integrates with `ObserveWishlistUseCase` to update favorite status dynamically.
    - Handles Guest Access rules for "Add to Cart" and "Favorite" actions, showing authentication required messages.

- **Home Screen Updates (`HomeScreen.kt` & `HomeViewModel.kt`):**
  - Wire up "See All" in the Top Brands section to navigate to `BrandsScreen`.
  - Wire individual brand clicks to navigate to `VendorProductsScreen`.

## 3. Navigation & Wiring
- **Routes:** Add `Route.BrandsRoute` and `Route.VendorProductsRoute` in `NavGraph`.
- **MainScreen.kt:** Add routing blocks inside the nested graph to handle `onNavigateToBrands` and `onNavigateToVendorProducts` passing down `vendorName`.

## 4. Verification & Testing
- Ensure UI matches the VogueVibe theme and utilizes material design standard typography.
- Verify that `AsyncImage` scales correctly inside the `BrandCard`.
- Verify compilation is successful without missing references.
