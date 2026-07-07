package com.example.wearzone.data.notifications

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.data.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val couponCode = intent.getStringExtra(KEY_COUPON_CODE) ?: return

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Coupon Code", couponCode)
        clipboard.setPrimaryClip(clip)

        val rawMessage = context.getString(R.string.coupon_copied_toast, couponCode)
        val spannableMessage = SpannableString("   $rawMessage")

        val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_coupon_notification)

        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

            val imageSpan = ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM)
            spannableMessage.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }

        Toast.makeText(context, spannableMessage, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val KEY_COUPON_CODE = "key_coupon_code"
    }
}