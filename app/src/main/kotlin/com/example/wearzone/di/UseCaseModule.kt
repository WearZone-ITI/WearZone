package com.example.wearzone.di

import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.account.usecase.CancelOrderUseCase
import com.example.wearzone.domain.account.usecase.GetOrderDetailsUseCase
import com.example.wearzone.domain.account.usecase.GetOrderHistoryUseCase
import com.example.wearzone.domain.account.usecase.OrderHistoryUseCases
import com.example.wearzone.domain.auth.usecase.CheckEmailVerifiedUseCase
import com.example.wearzone.domain.auth.usecase.GetCurrentUserUseCase
import com.example.wearzone.domain.auth.usecase.GetAuthAccessStateUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithEmailUseCase
import com.example.wearzone.domain.auth.usecase.LoginWithGoogleUseCase
import com.example.wearzone.domain.auth.usecase.LogoutUseCase
import com.example.wearzone.domain.auth.usecase.RegisterUseCase
import com.example.wearzone.domain.auth.usecase.SendEmailVerificationUseCase
import com.example.wearzone.domain.auth.usecase.SendPasswordResetEmailUseCase
import com.example.wearzone.domain.category.repository.ICategoryRepository
import com.example.wearzone.domain.category.usecase.GetCategoriesUseCase
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.domain.cart.usecase.AddToCartUseCase
import com.example.wearzone.domain.cart.usecase.ClearCartUseCase
import com.example.wearzone.domain.cart.usecase.ObserveCartUseCase
import com.example.wearzone.domain.cart.usecase.RemoveFromCartUseCase
import com.example.wearzone.domain.cart.usecase.UpdateCartQuantityUseCase
import com.example.wearzone.domain.checkout.repository.ICheckoutRepository
import com.example.wearzone.domain.checkout.repository.IDiscountRepository
import com.example.wearzone.domain.checkout.repository.IPayMockRepository
import com.example.wearzone.domain.checkout.usecase.ApplyDiscountCodeUseCase
import com.example.wearzone.domain.checkout.usecase.PlaceOrderUseCase
import com.example.wearzone.domain.checkout.usecase.ProcessPayMockPaymentUseCase
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository
import com.example.wearzone.domain.customer.address.repository.ICountryRepository
import com.example.wearzone.domain.customer.address.repository.ICurrentLocationRepository
import com.example.wearzone.domain.customer.address.usecase.CreateCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.CustomerAddressUseCases
import com.example.wearzone.domain.customer.address.usecase.DeleteCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCountriesUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentAddressCoordinatesUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCurrentCustomerIdUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.GetCustomerAddressesUseCase
import com.example.wearzone.domain.customer.address.usecase.ReverseGeocodeAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.SearchAddressSuggestionsUseCase
import com.example.wearzone.domain.customer.address.usecase.SetDefaultCustomerAddressUseCase
import com.example.wearzone.domain.customer.address.usecase.UpdateCustomerAddressUseCase
import com.example.wearzone.domain.onboarding.usecase.ObserveOnboardingCompletedUseCase
import com.example.wearzone.domain.onboarding.usecase.SetOnboardingCompletedUseCase
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.product.usecase.GetBrandsUseCase
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.product.usecase.GetProductsByVendorUseCase
import com.example.wearzone.domain.product.usecase.GetProductsUseCase
import com.example.wearzone.domain.product.usecase.SearchProductsUseCase
import com.example.wearzone.domain.search.repository.IRecentSearchRepository
import com.example.wearzone.domain.search.usecase.ClearRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.GetRecentSearchesUseCase
import com.example.wearzone.domain.search.usecase.SaveRecentSearchUseCase
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import com.example.wearzone.domain.settings.usecase.ObserveSettingsPreferencesUseCase
import com.example.wearzone.domain.settings.usecase.SetLanguageUseCase
import com.example.wearzone.domain.settings.usecase.SetNotificationsEnabledUseCase
import com.example.wearzone.domain.settings.usecase.SetThemeModeUseCase
import com.example.wearzone.domain.wishlist.repository.IWishlistRepository
import com.example.wearzone.domain.wishlist.usecase.ObserveWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.SyncWishlistUseCase
import com.example.wearzone.domain.wishlist.usecase.ToggleFavoriteUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides
    fun provideObserveOnboardingCompletedUseCase(
        repository: IAuthRepository,
    ): ObserveOnboardingCompletedUseCase = ObserveOnboardingCompletedUseCase(repository)

    @Provides
    fun provideSetOnboardingCompletedUseCase(
        repository: IAuthRepository,
    ): SetOnboardingCompletedUseCase = SetOnboardingCompletedUseCase(repository)

    @Provides
    fun provideGetProductsUseCase(
        repository: IProductRepository,
    ): GetProductsUseCase = GetProductsUseCase(repository)

    @Provides
    fun provideGetBrandsUseCase(
        repository: IProductRepository,
    ): GetBrandsUseCase = GetBrandsUseCase(repository)

    @Provides
    fun provideGetProductsByVendorUseCase(
        repository: IProductRepository,
    ): GetProductsByVendorUseCase = GetProductsByVendorUseCase(repository)

    @Provides
    fun provideSearchProductsUseCase(
        repository: IProductRepository,
    ): SearchProductsUseCase = SearchProductsUseCase(repository)

    @Provides
    fun provideGetRecentSearchesUseCase(
        repository: IRecentSearchRepository,
    ): GetRecentSearchesUseCase = GetRecentSearchesUseCase(repository)

    @Provides
    fun provideSaveRecentSearchUseCase(
        repository: IRecentSearchRepository,
    ): SaveRecentSearchUseCase = SaveRecentSearchUseCase(repository)

    @Provides
    fun provideClearRecentSearchesUseCase(
        repository: IRecentSearchRepository,
    ): ClearRecentSearchesUseCase = ClearRecentSearchesUseCase(repository)

    @Provides
    fun provideGetProductDetailUseCase(
        repository: IProductRepository
    ): GetProductDetailUseCase {
        return GetProductDetailUseCase(repository)
    }

    @Provides
    fun provideSendEmailVerificationUseCase(
        repository: IAuthRepository,
    ): SendEmailVerificationUseCase = SendEmailVerificationUseCase(repository)

    @Provides
    fun provideCheckEmailVerifiedUseCase(
        repository: IAuthRepository,
    ): CheckEmailVerifiedUseCase = CheckEmailVerifiedUseCase(repository)



    @Provides
    fun provideLoginWithEmailUseCase(
        repository: IAuthRepository,
    ): LoginWithEmailUseCase = LoginWithEmailUseCase(repository)

    @Provides
    fun provideLoginWithGoogleUseCase(
        repository: IAuthRepository,
    ): LoginWithGoogleUseCase = LoginWithGoogleUseCase(repository)

    @Provides
    fun provideRegisterUseCase(
        repository: IAuthRepository,
    ): RegisterUseCase = RegisterUseCase(repository)

    @Provides
    fun provideSendPasswordResetEmailUseCase(
        repository: IAuthRepository,
    ): SendPasswordResetEmailUseCase = SendPasswordResetEmailUseCase(repository)


    @Provides
    fun provideGetCurrentUserUseCase(
        repository: IAuthRepository
    ): GetCurrentUserUseCase = GetCurrentUserUseCase(repository)

    @Provides
    fun provideGetAuthAccessStateUseCase(
        repository: IAuthRepository,
        customerIdProvider: ICustomerIdProvider,
    ): GetAuthAccessStateUseCase = GetAuthAccessStateUseCase(repository, customerIdProvider)

    @Provides
    fun provideLogoutUseCase(
        repository: IAuthRepository
    ): LogoutUseCase {
        return LogoutUseCase(repository)
    }

    @Provides
    fun provideObserveSettingsPreferencesUseCase(
        repository: ISettingsRepository,
    ): ObserveSettingsPreferencesUseCase {
        return ObserveSettingsPreferencesUseCase(repository)
    }

    @Provides
    fun provideSetThemeModeUseCase(
        repository: ISettingsRepository,
    ): SetThemeModeUseCase {
        return SetThemeModeUseCase(repository)
    }

    @Provides
    fun provideSetNotificationsEnabledUseCase(
        repository: ISettingsRepository,
    ): SetNotificationsEnabledUseCase {
        return SetNotificationsEnabledUseCase(repository)
    }

    @Provides
    fun provideSetLanguageUseCase(
        repository: ISettingsRepository,
    ): SetLanguageUseCase {
        return SetLanguageUseCase(repository)
    }

    @Provides
    fun provideObserveCartUseCase(
        repository: ICartRepository,
    ): ObserveCartUseCase {
        return ObserveCartUseCase(repository)
    }

    @Provides
    fun provideAddToCartUseCase(
        repository: ICartRepository,
    ): AddToCartUseCase {
        return AddToCartUseCase(repository)
    }

    @Provides
    fun provideRemoveFromCartUseCase(
        repository: ICartRepository,
    ): RemoveFromCartUseCase {
        return RemoveFromCartUseCase(repository)
    }

    @Provides
    fun provideUpdateCartQuantityUseCase(
        repository: ICartRepository,
    ): UpdateCartQuantityUseCase {
        return UpdateCartQuantityUseCase(repository)
    }

    @Provides
    fun provideClearCartUseCase(
        repository: ICartRepository,
    ): ClearCartUseCase {
        return ClearCartUseCase(repository)
    }

    @Provides
    fun provideApplyDiscountCodeUseCase(
        discountRepository: IDiscountRepository,
    ): ApplyDiscountCodeUseCase = ApplyDiscountCodeUseCase(discountRepository)

    @Provides
    fun providePlaceOrderUseCase(
        checkoutRepository: ICheckoutRepository,
        cartRepository: ICartRepository,
        customerIdProvider: ICustomerIdProvider,
        customerAddressRepository: ICustomerAddressRepository,
    ): PlaceOrderUseCase =
        PlaceOrderUseCase(
            checkoutRepository = checkoutRepository,
            cartRepository = cartRepository,
            customerIdProvider = customerIdProvider,
            customerAddressRepository = customerAddressRepository,
        )

    @Provides
    fun provideProcessPayMockPaymentUseCase(
        repository: IPayMockRepository,
    ): ProcessPayMockPaymentUseCase = ProcessPayMockPaymentUseCase(repository)

    @Provides
    fun getCategoriesUseCase(
        repository: ICategoryRepository
    ): GetCategoriesUseCase {
        return GetCategoriesUseCase(repository)
    }

    @Provides
    fun provideGetCurrentCustomerIdUseCase(
        customerIdProvider: ICustomerIdProvider,
    ): GetCurrentCustomerIdUseCase = GetCurrentCustomerIdUseCase(customerIdProvider)

    @Provides
    fun provideGetCustomerAddressesUseCase(
        repository: ICustomerAddressRepository,
    ): GetCustomerAddressesUseCase = GetCustomerAddressesUseCase(repository)

    @Provides
    fun provideGetCustomerAddressUseCase(
        repository: ICustomerAddressRepository,
    ): GetCustomerAddressUseCase = GetCustomerAddressUseCase(repository)

    @Provides
    fun provideCreateCustomerAddressUseCase(
        repository: ICustomerAddressRepository,
    ): CreateCustomerAddressUseCase = CreateCustomerAddressUseCase(repository)

    @Provides
    fun provideUpdateCustomerAddressUseCase(
        repository: ICustomerAddressRepository,
    ): UpdateCustomerAddressUseCase = UpdateCustomerAddressUseCase(repository)

    @Provides
    fun provideSetDefaultCustomerAddressUseCase(
        repository: ICustomerAddressRepository,
    ): SetDefaultCustomerAddressUseCase = SetDefaultCustomerAddressUseCase(repository)

    @Provides
    fun provideDeleteCustomerAddressUseCase(
        repository: ICustomerAddressRepository,
    ): DeleteCustomerAddressUseCase = DeleteCustomerAddressUseCase(repository)

    @Provides
    fun provideCustomerAddressUseCases(
        getCurrentCustomerId: GetCurrentCustomerIdUseCase,
        getAddresses: GetCustomerAddressesUseCase,
        getAddress: GetCustomerAddressUseCase,
        createAddress: CreateCustomerAddressUseCase,
        updateAddress: UpdateCustomerAddressUseCase,
        setDefaultAddress: SetDefaultCustomerAddressUseCase,
        deleteAddress: DeleteCustomerAddressUseCase,
    ): CustomerAddressUseCases = CustomerAddressUseCases(
        getCurrentCustomerId = getCurrentCustomerId,
        getAddresses = getAddresses,
        getAddress = getAddress,
        createAddress = createAddress,
        updateAddress = updateAddress,
        setDefaultAddress = setDefaultAddress,
        deleteAddress = deleteAddress,
    )

    @Provides
    fun provideGetCountriesUseCase(
        repository: ICountryRepository,
    ): GetCountriesUseCase = GetCountriesUseCase(repository)

    @Provides
    fun provideSearchAddressSuggestionsUseCase(
        repository: IAddressLookupRepository,
    ): SearchAddressSuggestionsUseCase = SearchAddressSuggestionsUseCase(repository)

    @Provides
    fun provideReverseGeocodeAddressUseCase(
        repository: IAddressLookupRepository,
    ): ReverseGeocodeAddressUseCase = ReverseGeocodeAddressUseCase(repository)

    @Provides
    fun provideGetCurrentAddressCoordinatesUseCase(
        repository: ICurrentLocationRepository,
    ): GetCurrentAddressCoordinatesUseCase = GetCurrentAddressCoordinatesUseCase(repository)

    @Provides
    fun provideObserveWishlistUseCase(
        repository: IWishlistRepository
    ): ObserveWishlistUseCase = ObserveWishlistUseCase(repository)

    @Provides
    fun provideToggleFavoriteUseCase(
        repository: IWishlistRepository
    ): ToggleFavoriteUseCase = ToggleFavoriteUseCase(repository)

    @Provides
    fun provideSyncWishlistUseCase(
        repository: IWishlistRepository
    ): SyncWishlistUseCase = SyncWishlistUseCase(repository)

    @Provides
    fun provideGetOrderHistoryUseCase(
        repository: IOrderHistoryRepository,
    ): GetOrderHistoryUseCase = GetOrderHistoryUseCase(repository)

    @Provides
    fun provideGetOrderDetailsUseCase(
        repository: IOrderHistoryRepository,
    ): GetOrderDetailsUseCase = GetOrderDetailsUseCase(repository)

    @Provides
    fun provideCancelOrderUseCase(
        repository: IOrderHistoryRepository,
    ): CancelOrderUseCase = CancelOrderUseCase(repository)

    @Provides
    fun provideOrderHistoryUseCases(
        getCurrentCustomerId: GetCurrentCustomerIdUseCase,
        getOrderHistory: GetOrderHistoryUseCase,
        getOrderDetails: GetOrderDetailsUseCase,
        cancelOrder: CancelOrderUseCase,
    ): OrderHistoryUseCases = OrderHistoryUseCases(
        getCurrentCustomerId = getCurrentCustomerId,
        getOrderHistory = getOrderHistory,
        getOrderDetails = getOrderDetails,
        cancelOrder = cancelOrder,
    )


}
