# Payment Feature Implementation Plan (with Paymob Gateway)

## Goal Description
Implement a **Native Payment** feature using **Paymob Gateway** as the production-grade payment handler. The app will collect card details natively via Compose components, tokenize them securely, and process the payment via Paymob's direct payment APIs. Upon success, a "Paid" order will be created in Shopify.

## User Review Required
- **Required Keys**: I need your **Secret API Key**, **Public Key**, and **Integration ID** from the Paymob Dashboard.
- **3DS Handling**: While card entry is native, some banks require a 3DS secure page for verification. This will be handled via a system `CustomTab` only if necessary, keeping the core experience native.

## Proposed Changes

### Domain Layer
- **[CheckoutPaymentMethod](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/model/CheckoutPaymentMethod.kt)**: Ensure `CreditCard` is supported.
- **[NEW] [ProcessPaymobNativePaymentUseCase](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/usecase/ProcessPaymobNativePaymentUseCase.kt)**: New use case to coordinate the native Paymob 3-step + Direct Pay flow.

### Data Layer
- **[NEW] [PaymobApiService](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/api/PaymobApiService.kt)**: Retrofit interface for Paymob acceptance and ecommerce APIs.
- **[NEW] [PaymobRepositoryImpl](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/repository/PaymobRepositoryImpl.kt)**: Implementation of the native payment flow sequence.

### Presentation Layer
- **[CheckoutViewModel](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutViewModel.kt)**: Update to use the native Paymob use case and handle complex state transitions.
- **[CreditCardForm](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/components/CreditCardForm.kt)**: Native UI for card details (already present, ensuring full wiring).

## Verification Plan

### Automated Tests
- Run Paymob integration tests.
  ```powershell
  ./gradlew :data:testDebugUnitTest --tests "com.example.wearzone.data.repository.PaymobRepositoryImplTest"
  ```

### Manual Verification
- Test "Credit Card" flow: enter native card data, verify Paymob API sequence, verify Shopify order created with `financial_status: paid`.
