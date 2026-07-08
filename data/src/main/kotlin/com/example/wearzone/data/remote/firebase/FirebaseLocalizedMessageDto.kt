package com.example.wearzone.data.remote.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.firestore.DocumentSnapshot

data class FirebaseLocalizedMessageDto(
    val titleEn: String = "",
    val titleAr: String = "",
    val bodyEn: String = "",
    val bodyAr: String = "",
    val messageEn: String = "",
    val messageAr: String = "",
) {
    fun localizedTitle(languageCode: String): String = localizedValue(languageCode, titleAr, titleEn)
    fun localizedBody(languageCode: String): String = localizedValue(languageCode, bodyAr, bodyEn)
    fun localizedMessage(languageCode: String): String = localizedValue(languageCode, messageAr, messageEn)

    private fun localizedValue(languageCode: String, arabic: String, english: String): String {
        return if (languageCode.equals("ar", ignoreCase = true)) {
            arabic.takeIf { it.isNotBlank() } ?: english
        } else {
            english.takeIf { it.isNotBlank() } ?: arabic
        }
    }
}

fun DocumentSnapshot.toFirebaseLocalizedMessageDto(): FirebaseLocalizedMessageDto =
    FirebaseLocalizedMessageDto(
        titleEn = getString("title_en").orEmpty(),
        titleAr = getString("title_ar").orEmpty(),
        bodyEn = getString("body_en").orEmpty(),
        bodyAr = getString("body_ar").orEmpty(),
        messageEn = getString("message_en").orEmpty(),
        messageAr = getString("message_ar").orEmpty(),
    )

fun DataSnapshot.toFirebaseLocalizedMessageDto(): FirebaseLocalizedMessageDto =
    FirebaseLocalizedMessageDto(
        titleEn = child("title_en").getValue(String::class.java).orEmpty(),
        titleAr = child("title_ar").getValue(String::class.java).orEmpty(),
        bodyEn = child("body_en").getValue(String::class.java).orEmpty(),
        bodyAr = child("body_ar").getValue(String::class.java).orEmpty(),
        messageEn = child("message_en").getValue(String::class.java).orEmpty(),
        messageAr = child("message_ar").getValue(String::class.java).orEmpty(),
    )
