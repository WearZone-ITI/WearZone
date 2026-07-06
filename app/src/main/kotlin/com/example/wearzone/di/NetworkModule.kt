package com.example.wearzone.di

import android.util.Log
import com.example.wearzone.BuildConfig
import com.example.wearzone.data.di.IoDispatcher
import com.example.wearzone.data.di.PaymobOkHttp
import com.example.wearzone.data.di.PaymobRetrofit
import com.example.wearzone.data.di.ShopifyOkHttp
import com.example.wearzone.data.di.ShopifyRetrofit
import com.example.wearzone.data.remote.api.AddressApiService
import com.example.wearzone.data.remote.api.AuthApiService
import com.example.wearzone.data.remote.api.CartApiService
import com.example.wearzone.data.remote.api.DiscountApiService
import com.example.wearzone.data.remote.api.OrderApiService
import com.example.wearzone.data.remote.api.PaymobApiService
import com.example.wearzone.data.remote.api.ProductApiService
import com.example.wearzone.data.repository.PaymentRepositoryImpl
import com.example.wearzone.domain.checkout.repository.IPaymentRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://mad46-and9.myshopify.com/"
    private const val PAYMOB_BASE_URL = "https://accept.paymob.com/"

    @Provides
    @Named("secretKey")
    fun providePaymobSecretKey(): String = BuildConfig.PAYMOB_SECRET_KEY

    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
        }
    }

    @Provides
    @ShopifyOkHttp
    fun provideShopifyOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request()
                if (BuildConfig.DEBUG) {
                    Log.d("ShopifyRequest", "${request.method} ${request.url}")
                }
                val authenticatedRequest = request.newBuilder().addHeader(
                        "X-Shopify-Access-Token",
                        BuildConfig.SHOPIFY_ADMIN_TOKEN
                    ).addHeader("Content-Type", "application/json").build()
                chain.proceed(authenticatedRequest)
            }.build()
    }

    @Provides
    @PaymobOkHttp
    fun providePaymobOkHttpClient(): OkHttpClient {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder().addInterceptor { chain ->
                val request = chain.request()
                val authenticatedRequest =
                    request.newBuilder().addHeader("Content-Type", "application/json").build()

                if (BuildConfig.DEBUG) {
                    Log.d(
                        "PaymobRequest",
                        "${authenticatedRequest.method} ${authenticatedRequest.url}"
                    )
                }
                chain.proceed(authenticatedRequest)
            }.addInterceptor(logging).build()
    }

    @Provides
    @ShopifyRetrofit
    fun provideRetrofit(
        @ShopifyOkHttp okHttpClient: OkHttpClient, json: Json
    ): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
    }

    @Provides
    @PaymobRetrofit
    fun providePaymobRetrofit(
        @PaymobOkHttp okHttpClient: OkHttpClient, json: Json
    ): Retrofit {
        return Retrofit.Builder().baseUrl(PAYMOB_BASE_URL).client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
    }

    @Provides
    fun provideProductApiService(@ShopifyRetrofit retrofit: Retrofit): ProductApiService {
        return retrofit.create(ProductApiService::class.java)
    }

    @Provides
    fun provideCartApiService(@ShopifyRetrofit retrofit: Retrofit): CartApiService {
        return retrofit.create(CartApiService::class.java)
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    fun provideAuthService(@ShopifyRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    fun provideAddressApiService(@ShopifyRetrofit retrofit: Retrofit): AddressApiService {
        return retrofit.create(AddressApiService::class.java)
    }

    @Provides
    fun provideOrderApiService(
        @ShopifyRetrofit retrofit: Retrofit,
    ): OrderApiService {
        return retrofit.create(OrderApiService::class.java)
    }

    @Provides
    fun provideDiscountApiService(
        @ShopifyRetrofit retrofit: Retrofit,
    ): DiscountApiService {
        return retrofit.create(DiscountApiService::class.java)
    }


    @Provides
    @Singleton
    fun providePaymobApiService(@PaymobRetrofit retrofit: Retrofit): PaymobApiService =
        retrofit.create(PaymobApiService::class.java)

}
