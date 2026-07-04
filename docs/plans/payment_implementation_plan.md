# Payment Feature Implementation Plan (with PayMock Gateway)

## Goal Description
Implement a **Native Payment** feature using **PayMock Gateway** (https://github.com/PayMock/gateway) as the external payment handler. The app will collect card details, process payment via PayMock, and then create a "Paid" order in Shopify using the Admin REST API with the PayMock transaction reference.

## User Review Required
- **PayMock Endpoint**: I will assume PayMock is running at a configurable base URL (e.g., `http://localhost:8080/api/v1` for local dev).
- **API Key**: A placeholder or environment-provided API key for PayMock will be needed.
- **Shopify Order Status**: Once PayMock approves the payment, I will set the Shopify order's `financial_status` to `paid` and include the PayMock `payment_id` in the order's `note` or `transaction` details.

## Proposed Changes

### Domain Layer
- **[CheckoutPaymentMethod](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/model/CheckoutPaymentMethod.kt)**: Add `CreditCard` to the enum.
- **[NEW] [CardInfo](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/model/CardInfo.kt)**: Model for card details.
- **[NEW] [ProcessPayMockPaymentUseCase](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/usecase/ProcessPayMockPaymentUseCase.kt)**: Use case to call PayMock API and get a transaction ID.

### Data Layer
- **[NEW] [PayMockApiService](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/api/PayMockApiService.kt)**: Retrofit interface for PayMock (`POST /payments`).
- **[CheckoutRepositoryImpl](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/repository/CheckoutRepositoryImpl.kt)**: Update `createOrder` to accept a `paymentId` and set `financial_status: "paid"`.

### Presentation Layer
- **[CheckoutPaymentMethodUi](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutDeliveryAddressUiModel.kt)**: Add `CreditCard` and translations.
- **[CheckoutUiIntent](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutUiIntent.kt)**: Add intents for payment method selection and card data input.
- **[CheckoutViewModel](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutViewModel.kt)**: Coordinate PayMock payment call *before* creating the Shopify order.
- **[NEW] [CreditCardForm](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/components/CreditCardForm.kt)**: Native UI for card details.
- **[CheckoutScreen](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutScreen.kt)**: Integrate the new payment method and form.

### Localization
- **[strings.xml](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/res/values/strings.xml)**: Add strings for PayMock, Card details, and payment statuses.
- **[strings.xml (Arabic)](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/res/values-ar/strings.xml)**: Arabic translations for all checkout/payment strings.

### Testing
- **[CheckoutViewModelTest](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/test/java/com/example/presentation/checkout/CheckoutViewModelTest.kt)**: Verify the flow: PayMock Payment -> Shopify Order Creation.

## Verification Plan

### Automated Tests
- Run updated ViewModel tests.
  ```powershell
  ./gradlew :presentation:testDebugUnitTest --tests "com.example.presentation.checkout.CheckoutViewModelTest"
  ```

### Manual Verification
- Test "Credit Card" flow: enter mock card data, verify PayMock API call, verify Shopify order created with `financial_status: paid`.
- Test "Cash on Delivery" flow: verify Shopify order created with `financial_status: pending`.
- Verify full localization in Arabic.
