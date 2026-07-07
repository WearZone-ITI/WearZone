package com.example.wearzone.presentation.checkout

import android.content.Context
import com.example.presentation.BuildConfig
import com.paymob.paymob_sdk.PaymobSdk
import com.paymob.paymob_sdk.ui.PaymobSdkListener

fun launchPaymobSdk(
    context: Context,
    paymobSdkListener: PaymobSdkListener,
    clientSecret: String,
) {

    PaymobSdk.Builder(
        context = context,
        clientSecret = clientSecret,
        publicKey = BuildConfig.PAYMOB_PUBLIC_KEY,
        paymobSdkListener = paymobSdkListener
    ).build().start()
}