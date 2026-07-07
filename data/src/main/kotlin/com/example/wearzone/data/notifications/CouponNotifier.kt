package com.example.wearzone.data.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.data.R
import com.example.wearzone.domain.notifications.model.CouponOffer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CouponNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun notify(offer: CouponOffer) {
        if (!hasNotificationPermission()) return

        ensureChannelExists()

        val localizedMessage = context.getString(
            R.string.coupon_notification_message,
            offer.discountPercentage,
            offer.code
        )

        val receiverIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.KEY_COUPON_CODE, offer.code)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            offer.code.hashCode(),
            receiverIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_coupon_notification)
            .setContentTitle(context.getString(R.string.coupon_notification_title))
            .setContentText(localizedMessage)
            .setStyle(NotificationCompat.BigTextStyle().bigText(localizedMessage))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }.onFailure {
            it.printStackTrace()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannelExists() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.coupon_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.coupon_notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "coupon_offers_channel"
        const val NOTIFICATION_ID = 1001
    }
}