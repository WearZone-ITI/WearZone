package com.example.wearzone.presentation.checkout

import android.content.Context
import android.util.Log

/**
 * A helper object to encapsulate the Paymob SDK launch logic using the Builder pattern 
 * required by version 1.8.1.
 */
object PaymobSdkLauncher {

    fun launch(
        context: Context,
        publicKey: String,
        paymentToken: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        try {
            // Using reflection to maintain build stability until .aar is synced and classes are recognized
            val sdkClass = Class.forName("com.paymob.paymob_sdk.PaymobSdk")
            val listenerClass = Class.forName("com.paymob.paymob_sdk.ui.PaymobSdkListener")
            val builderClass = Class.forName("com.paymob.paymob_sdk.PaymobSdk\$Builder")

            // Create a dynamic proxy for the listener to handle callbacks natively
            val listenerInstance = java.lang.reflect.Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass)
            ) { _, method, args ->
                when (method.name) {
                    "onPaymentSuccess" -> {
                        Log.d("PaymobSdkLauncher", "Payment Successful Callback")
                        onSuccess()
                    }
                    "onPaymentFailure" -> {
                        val message = args?.get(0) as? String ?: "Unknown error"
                        Log.e("PaymobSdkLauncher", "Payment Failed Callback: $message")
                        onFailure(message)
                    }
                }
                null
            }

            // PaymobSdk.Builder(context, clientSecret, publicKey, listener).build().start()
            val builderConstructor = builderClass.getConstructor(
                Context::class.java,
                String::class.java,
                String::class.java,
                listenerClass
            )
            
            val builderInstance = builderConstructor.newInstance(
                context,
                paymentToken,
                publicKey,
                listenerInstance
            )
            
            val buildMethod = builderClass.getMethod("build")
            val sdkInstance = buildMethod.invoke(builderInstance)
            
            val startMethod = sdkInstance.javaClass.getMethod("start")
            startMethod.invoke(sdkInstance)

            Log.d("PaymobSdkLauncher", "Paymob SDK UI started successfully")
            
        } catch (e: ClassNotFoundException) {
            Log.e("PaymobSdkLauncher", "Paymob SDK classes not found. Ensure the AAR is in the libs folder.", e)
        } catch (e: Exception) {
            Log.e("PaymobSdkLauncher", "Failed to launch Paymob SDK.", e)
        }
    }
}
