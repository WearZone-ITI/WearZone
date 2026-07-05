package com.example.wearzone.di

import com.example.wearzone.data.remote.api.AddressApiService
import com.example.wearzone.data.remote.api.AuthApiService
import com.example.wearzone.data.remote.api.CartApiService
import com.example.wearzone.data.remote.api.DiscountApiService
import com.example.wearzone.data.remote.api.OrderApiService
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

    @Provides
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder().addHeader(
                "X-Shopify-Access-Token", com.example.wearzone.BuildConfig.SHOPIFY_ADMIN_TOKEN
            ).addHeader("Content-Type", "application/json").build()
            chain.proceed(request)
        }.build()
    }

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL).client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
    }

    @Provides
    fun provideProductApiService(retrofit: Retrofit): ProductApiService {
        return retrofit.create(ProductApiService::class.java)
    }

    @Provides
    fun provideCartApiService(retrofit: Retrofit): CartApiService {
        return retrofit.create(CartApiService::class.java)
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    fun provideFirestore() = FirebaseFirestore.getInstance()

    @Provides
    fun provideAuthService(retrofit: Retrofit) : AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    fun provideAddressApiService(retrofit: Retrofit): AddressApiService {
        return retrofit.create(AddressApiService::class.java)
    }

    @Provides
    fun provideOrderApiService(
        retrofit: Retrofit,
    ): OrderApiService {
        return retrofit.create(OrderApiService::class.java)
    }

    @Provides
    fun provideDiscountApiService(
        retrofit: Retrofit,
    ): DiscountApiService {
        return retrofit.create(DiscountApiService::class.java)
    }

    @Provides
    fun provideGroqApiService(): com.example.wearzone.data.remote.ai.chat.api.GroqApiService {
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.groq.com/openai/v1/")
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        return retrofit.create(com.example.wearzone.data.remote.ai.chat.api.GroqApiService::class.java)
    }
}