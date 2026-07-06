package com.example.wearzone.di

import android.util.Log
import com.example.wearzone.data.di.CurrencyOkHttp
import com.example.wearzone.data.di.CurrencyRetrofit
import com.example.wearzone.data.di.PayMockOkHttp
import com.example.wearzone.data.di.PayMockRetrofit
import com.example.wearzone.data.di.MapboxAccessToken
import com.example.wearzone.data.di.MapboxOkHttp
import com.example.wearzone.data.di.MapboxRetrofit
import com.example.wearzone.data.di.ShopifyOkHttp
import com.example.wearzone.data.di.ShopifyRetrofit
import com.example.wearzone.data.remote.api.AddressApiService
import com.example.wearzone.data.remote.api.AuthApiService
import com.example.wearzone.data.remote.api.CartApiService
import com.example.wearzone.data.remote.api.CurrencyApiService
import com.example.wearzone.data.remote.api.DiscountApiService
import com.example.wearzone.data.remote.api.MapboxApiService
import com.example.wearzone.data.remote.api.OrderApiService
import com.example.wearzone.data.remote.api.PayMockApiService
import com.example.wearzone.data.remote.interceptor.PayMockAuthInterceptor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.example.wearzone.data.remote.api.ProductApiService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://mad46-and9.myshopify.com/"
    private const val PAYMOCK_BASE_URL = "http://10.87.46.72:8000/api/v1/"
    private const val MAPBOX_BASE_URL = "https://api.mapbox.com/"
    private const val CURRENCY_BASE_URL = "https://open.er-api.com/"

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
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                if (com.example.wearzone.BuildConfig.DEBUG) {
                    Log.d("ShopifyRequest", "${request.method} ${request.url}")
                }
                val authenticatedRequest = request.newBuilder()
                    .addHeader("X-Shopify-Access-Token", com.example.wearzone.BuildConfig.SHOPIFY_ADMIN_TOKEN)
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(authenticatedRequest)
            }.build()
    }

    @Provides
    @PayMockOkHttp
    fun providePayMockOkHttpClient(
        authInterceptor: PayMockAuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor { chain ->
                val request = chain.request()
                if (com.example.wearzone.BuildConfig.DEBUG) {
                    Log.d("PayMockRequest", "${request.method} ${request.url}")
                }
                chain.proceed(request)
            }.build()
    }

    @Provides
    @MapboxOkHttp
    fun provideMapboxOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().build()

    @Provides
    @MapboxAccessToken
    fun provideMapboxAccessToken(): String =
        com.example.wearzone.BuildConfig.MAPBOX_ACCESS_TOKEN

    @Provides
    @ShopifyRetrofit
    fun provideRetrofit(
        @ShopifyOkHttp okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @PayMockRetrofit
    fun providePayMockRetrofit(
        @PayMockOkHttp okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(PAYMOCK_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @MapboxRetrofit
    fun provideMapboxRetrofit(
        @MapboxOkHttp okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(MAPBOX_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @CurrencyOkHttp
    fun provideCurrencyOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder().build()

    @Provides
    @CurrencyRetrofit
    fun provideCurrencyRetrofit(
        @CurrencyOkHttp okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(CURRENCY_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
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
    fun provideAuthService(@ShopifyRetrofit retrofit: Retrofit) : AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    fun provideAddressApiService(@ShopifyRetrofit retrofit: Retrofit): AddressApiService {
        return retrofit.create(AddressApiService::class.java)
    }

    @Provides
    fun provideMapboxApiService(@MapboxRetrofit retrofit: Retrofit): MapboxApiService {
        return retrofit.create(MapboxApiService::class.java)
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
    fun providePayMockApiService(
        @PayMockRetrofit retrofit: Retrofit,
    ): PayMockApiService {
        return retrofit.create(PayMockApiService::class.java)
    }

    @Provides
    fun provideCurrencyApiService(
        @CurrencyRetrofit retrofit: Retrofit,
    ): CurrencyApiService {
        return retrofit.create(CurrencyApiService::class.java)
    }
}
