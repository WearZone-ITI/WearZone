package com.example.wearzone.data.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShopifyRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PaymobRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ShopifyOkHttp

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PaymobOkHttp
