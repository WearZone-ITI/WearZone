package com.example.wearzone.di

import com.example.wearzone.data.repository.AuthRepositoryImpl
import com.example.wearzone.data.repository.AddressLookupRepositoryImpl
import com.example.wearzone.data.repository.CategoryRepositoryImpl
import com.example.wearzone.data.repository.CurrentCustomerIdProviderImpl
import com.example.wearzone.data.repository.CurrentLocationRepositoryImpl
import com.example.wearzone.data.repository.CustomerAddressRepositoryImpl
import com.example.wearzone.data.repository.CountryRepositoryImpl
import com.example.wearzone.data.repository.OrderHistoryRepositoryImpl
import com.example.wearzone.data.repository.PayMockRepositoryImpl
import com.example.wearzone.data.repository.ProductRepositoryImpl
import com.example.wearzone.data.repository.search.RecentSearchRepositoryImpl
import com.example.wearzone.data.repository.CartRepositoryImpl
import com.example.wearzone.data.repository.CheckoutRepositoryImpl
import com.example.wearzone.data.repository.DiscountRepositoryImpl
import com.example.wearzone.domain.account.repository.IOrderHistoryRepository
import com.example.wearzone.domain.auth.repository.IAuthRepository
import com.example.wearzone.domain.checkout.repository.ICheckoutRepository
import com.example.wearzone.domain.checkout.repository.IDiscountRepository
import com.example.wearzone.domain.checkout.repository.IPayMockRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerAddressRepository
import com.example.wearzone.domain.customer.address.repository.ICustomerIdProvider
import com.example.wearzone.domain.customer.address.repository.IAddressLookupRepository
import com.example.wearzone.domain.customer.address.repository.ICountryRepository
import com.example.wearzone.domain.customer.address.repository.ICurrentLocationRepository
import com.example.wearzone.domain.product.repository.IProductRepository
import com.example.wearzone.domain.search.repository.IRecentSearchRepository
import com.example.wearzone.domain.cart.repository.ICartRepository
import com.example.wearzone.data.repository.ReviewRepositoryImpl
import com.example.wearzone.domain.product.repository.IReviewRepository
import com.example.wearzone.data.repository.SettingsRepositoryImpl
import com.example.wearzone.domain.category.repository.ICategoryRepository
import com.example.wearzone.data.repository.WishlistRepositoryImpl
import com.example.wearzone.domain.settings.repository.ISettingsRepository
import com.example.wearzone.domain.wishlist.repository.IWishlistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindProductRepository(productRepositoryImpl: ProductRepositoryImpl): IProductRepository

    @Binds
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds
    abstract fun bindRecentSearchRepository(impl: RecentSearchRepositoryImpl): IRecentSearchRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): ISettingsRepository

    @Binds
    abstract fun bindCartRepository(cartRepositoryImpl: CartRepositoryImpl): ICartRepository

    @Binds
    abstract fun bindCheckoutRepository(impl: CheckoutRepositoryImpl): ICheckoutRepository

    @Binds
    abstract fun bindDiscountRepository(impl: DiscountRepositoryImpl): IDiscountRepository

    @Binds
    abstract fun bindPayMockRepository(impl: PayMockRepositoryImpl): IPayMockRepository

    @Binds
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): ICategoryRepository

    @Binds
    abstract fun bindCustomerAddressRepository(
        impl: CustomerAddressRepositoryImpl,
    ): ICustomerAddressRepository

    @Binds
    abstract fun bindCurrentCustomerIdProvider(
        impl: CurrentCustomerIdProviderImpl,
    ): ICustomerIdProvider

    @Binds
    abstract fun bindWishlistRepository(impl: WishlistRepositoryImpl): IWishlistRepository

    @Binds
    abstract fun bindOrderHistoryRepository(impl: OrderHistoryRepositoryImpl): IOrderHistoryRepository

    @Binds
    abstract fun bindCountryRepository(impl: CountryRepositoryImpl): ICountryRepository

    @Binds
    abstract fun bindAddressLookupRepository(impl: AddressLookupRepositoryImpl): IAddressLookupRepository

    @Binds
    abstract fun bindCurrentLocationRepository(
        impl: CurrentLocationRepositoryImpl,
    ): ICurrentLocationRepository

    @Binds
    abstract fun bindReviewRepository(impl: ReviewRepositoryImpl): IReviewRepository
}

