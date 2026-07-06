package com.example.wearzone.data.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShopifyRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PayMockRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShopifyOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PayMockOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MapboxRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MapboxOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MapboxAccessToken

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrencyOkHttp
