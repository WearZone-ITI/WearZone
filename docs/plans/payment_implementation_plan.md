# Payment Feature Implementation Plan (Paymob SDK Integration)

## Goal Description
Implement a production-grade payment flow using the **Paymob SDK**. The app integrates the Paymob mobile SDK to handle secure credit card payments. Upon successful payment verification, the app automatically creates a "Paid" order in Shopify and clears the local cart.

## Architecture & Flow
1. **Payment Intention**: App calls `CreatePaymentIntentionUseCase` to get a `clientSecret` from the backend/BFF.
2. **SDK Launch**: The `CheckoutScreen` uses `PaymobSdkLauncher` to open the Paymob UI with the `clientSecret`.
3. **Result Handling**: `PaymobSdkListener` captures the transaction result (Success/Failure).
4. **Order Automation**: On success, `CheckoutViewModel` automatically triggers `PlaceOrderUseCase` with the `transactionId`.
5. **UI Feedback**: A polished loading overlay displays "Securing Payment..." followed by "Placing Order..." with branded icons.

## Implemented Components

### Domain Layer
- **[CreatePaymentIntentionUseCase.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/usecase/CreatePaymentIntentionUseCase.kt)**: Requests a payment intent from Paymob.
- **[PlaceOrderUseCase.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/usecase/PlaceOrderUseCase.kt)**: Creates the Shopify order and clears the Room cart.
- **[PaymobPayment.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/domain/src/main/kotlin/com/example/wearzone/domain/checkout/model/PaymobPayment.kt)**: Domain models for intentions and responses.

### Data Layer
- **[PaymobApiService.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/api/PaymobApiService.kt)**: Retrofit service for Paymob communication.
- **[PaymobRepositoryImpl.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/repository/PaymobRepositoryImpl.kt)**: Handles the API calls for payment intentions.
- **[PaymobMappers.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/data/src/main/kotlin/com/example/wearzone/data/remote/mapper/PaymobMappers.kt)**: Maps SDK and API DTOs to domain models.

### Presentation Layer
- **[CheckoutViewModel.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutViewModel.kt)**: Orchestrates the flow, manages `isPlacingOrder` and `isProcessingPayment` states.
- **[CheckoutScreen.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/CheckoutScreen.kt)**:
    - Integrates `PaymobSdkLauncher`.
    - Features the **Polished Loading Overlay** with dynamic icons (Lock/Bag).
    - Features the **Enhanced Confirmation Dialog** (95% width).
- **[PaymobSdkLauncher.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/PaymobSdkLauncher.kt)**: Helper to encapsulate SDK initialization and launch logic.
- **[PaymentMethodSelector.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/kotlin/com/example/wearzone/presentation/checkout/components/PaymentMethodSelector.kt)**: UI for selecting between COD and Credit Card.

### Resources
- **[strings.xml](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/main/res/values/strings.xml)**: Localized strings for "Securing Payment", "Placing Order", etc. (EN/AR).

## Verification Plan

### Automated Tests
- **[CheckoutViewModelTest.kt](file:///C:/Users/user/AndroidStudioProjects/WearZone/presentation/src/test/java/com/example/presentation/checkout/CheckoutViewModelTest.kt)**: Verifies state transitions and order automation logic.

### Manual Verification
- **E2E Flow**: 
    1. Select "Credit Card".
    2. Click "Place Order" (verify large dialog).
    3. Complete Paymob flow (verify "Securing Payment...").
    4. Observe automatic return and "Placing Order..." overlay.
    5. Verify navigation to Order History.
