# Customer Addresses Feature Plan

## Summary

Build Customer Addresses as a scoped Clean Architecture + MVI feature entered from `ProfileScreen -> Saved Addresses`. The feature includes a saved-address list and an add/edit form using the supplied Shopify Customer Address responses. Shopify address `id` and `customer_id` are `Long`.

## Current Project Findings

- The app has `app`, `data`, `domain`, and `presentation` modules.
- Profile already emits `NavigateToSavedAddresses`, but saved-address navigation was not wired.
- No address feature existed before this implementation.
- `User` currently has Firebase `uid`, `email`, `displayName`, and `photoUrl`, but no real Shopify `customer_id`.
- Retrofit `baseUrl` is `https://mad46-and9.myshopify.com/`, so address endpoints include `admin/api/{version}/...`.
- `values-ar` exists; only real Arabic translations should be added there.

## Existing Patterns Discovered

- Retrofit services live in `data.remote.api`.
- Remote data source interfaces and implementations wrap API services.
- Repositories live in `data.repository` and handle dispatcher switching.
- Domain repositories and use cases are framework-free.
- Presentation screens use `StateFlow` for state and `Channel` for one-shot effects.
- Navigation uses serializable type-safe route objects.
- Hilt modules in `app/di` provide APIs, data sources, repositories, and use cases.

## Files To Create

- Domain customer address models, repository, customer-id provider interface, and use cases.
- Data address API service, DTOs, remote data source, repository implementation, and failing customer-id provider implementation.
- Presentation address list and form MVI contracts, ViewModels, screens, and components.

## Files To Modify

- `app/src/main/kotlin/com/example/wearzone/navigation/Route.kt`
- `app/src/main/kotlin/com/example/wearzone/navigation/NavGraph.kt`
- `app/src/main/kotlin/com/example/wearzone/navigation/MainScreen.kt`
- `app/src/main/kotlin/com/example/wearzone/di/NetworkModule.kt`
- `app/src/main/kotlin/com/example/wearzone/di/DataSourceModule.kt`
- `app/src/main/kotlin/com/example/wearzone/di/RepositoryModule.kt`
- `app/src/main/kotlin/com/example/wearzone/di/UseCaseModule.kt`
- `presentation/src/main/res/values/strings.xml`
- `presentation/src/main/res/values-ar/strings.xml` only with real translations.

## Domain Layer Design

- `CustomerAddress` mirrors Shopify response data with `Long` ids.
- `AddressInput` mirrors create/update request fields.
- `ICustomerAddressRepository` exposes list, single, create, update, set default, and delete methods returning `Result<T>`.
- `ICustomerIdProvider` exists because the real Shopify customer id is not available yet.
- `GetCurrentCustomerIdUseCase` returns a clear failure until Firebase-to-Shopify mapping is implemented.

## Data Layer Design

- `AddressApiService` uses Retrofit and includes `admin/api/{version}` because the current base URL is only the shop domain.
- DTOs use `@SerialName` for Shopify snake_case fields.
- `CustomerAddressRepositoryImpl` maps DTOs to domain models and request input to request DTOs.
- `CustomerAddressRemoteDataSourceImpl` parses Shopify delete errors and maps default-delete failures to a domain exception.
- `CurrentCustomerIdProviderImpl` fails with a clear TODO-style exception and never returns a random or hardcoded id.

## DTO Design Based On Real Responses

- `CustomerAddressesResponseDto(addresses: List<CustomerAddressDto>)`
- `CustomerAddressResponseDto(customer_address: CustomerAddressDto)`
- `CustomerAddressDto` includes `id: Long`, `customer_id: Long`, nullable Shopify address fields, and `default`.
- `ShopifyAddressErrorResponseDto` parses `errors.base`.

## Request Body Design

Create/update request body:

```json
{
  "address": {
    "first_name": "...",
    "last_name": "...",
    "company": null,
    "address1": "...",
    "address2": null,
    "city": "...",
    "province": "...",
    "country": "...",
    "zip": "...",
    "phone": "..."
  }
}
```

## Repository And Use Cases Design

- Use cases are single-purpose and expose one `invoke`.
- The ViewModels never call repositories directly.
- Customer id is resolved before every API operation through `GetCurrentCustomerIdUseCase`.
- Missing Shopify customer id is surfaced as a user-friendly error.

## Presentation Layer Design

- Saved-address list supports loading, empty, error, refresh, delete confirmation, set default, add, edit, and delete success/error flows.
- The list blocks default-address deletion in UI and shows a friendly message.
- Backend default-delete error remains handled as a fallback.
- Form supports add and edit modes, field validation, save loading, set default, and navigation back after success.
- All list collections in UI state use immutable collections.

## Navigation Flow

- Profile saved-address row navigates to `SavedAddressesRoute`.
- List add button navigates to `AddressFormRoute(null)`.
- List edit button navigates to `AddressFormRoute(addressId)`.
- Form save success pops back to the list.

## DI Changes

- Provide `AddressApiService`.
- Bind address remote data source.
- Bind address repository.
- Bind customer-id provider.
- Provide all address use cases.

## Validation Rules

- Required: recipient name, mobile number, street/address1, city, country, zip/postal code.
- Optional: company, address2, province.
- Recipient name is split into `first_name` and `last_name`.
- Phone must contain at least one digit.
- Save is disabled while required fields are blank or a submit is running.

## Error, Loading, And Empty Handling

- List loading uses a centered progress indicator.
- List empty state shows a friendly CTA to add an address.
- List errors show retry.
- Form loading is shown while edit data loads.
- Form errors show localized inline or top-level messages.

## Special Handling For Deleting Default Address

- If `address.default == true`, delete is prevented in UI and no delete API call is made.
- If the backend still returns `Cannot delete the customer's default address`, it is mapped to the same friendly message.

## Customer Id Assumptions Or Blockers

- The app currently has no real Shopify customer id mapping.
- The implementation must not invent, hardcode, or ask the UI for `customer_id`.
- `CurrentCustomerIdProviderImpl` intentionally fails until a real Firebase-to-Shopify customer mapping is added.

## Manual Testing Checklist

- Profile -> Saved Addresses opens.
- Loading, empty, populated, and error states render.
- Default address cannot be deleted from UI.
- Backend default-delete error is shown gracefully if reached.
- Add, edit, set default, delete non-default, retry, and refresh flows work once real customer id mapping exists.
- No UI hardcodes or asks for `customer_id`.

## Build Verification Checklist

- Run domain, data, and presentation tests.
- Run `gradlew :app:assembleDebug`.
- Confirm no Shopify Admin token literal appears in source.
- Confirm any pre-existing navigation/DI syntax issues are fixed or separately documented.
