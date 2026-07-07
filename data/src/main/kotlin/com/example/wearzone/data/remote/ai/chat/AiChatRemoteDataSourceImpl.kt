package com.example.wearzone.data.remote.ai.chat

import android.util.Log
import com.example.wearzone.data.remote.ai.chat.api.GroqApiService
import com.example.wearzone.data.remote.ai.chat.dto.GroqMessage
import com.example.wearzone.data.remote.ai.chat.dto.GroqRequest
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GroqApiKey

class AiChatRemoteDataSourceImpl @Inject constructor(
    @GroqApiKey private val apiKey: String,
    private val groqApiService: GroqApiService,
) : IAiChatRemoteDataSource {

    init {
        Log.d("GROQ_CHAT_DEBUG", "Target Key Length: ${apiKey.length}")
    }

    private val systemPrompt = """
        You are the WearZone AI shopping assistant.
        You must answer using ONLY the products explicitly provided in CATALOG_CONTEXT.
        Never invent product names, IDs, prices, brands, images, sizes, colors, variants, or availability.
        Respect the retrieval labels exactly: use "Exact matches" only for products listed under Exact matches, and use "Closest alternatives" only for fallback products.
        If CATALOG_CONTEXT says "No exact matches found", say that clearly before any alternatives.
        Never claim a fallback item matches the full query.
        Never suggest products that violate hard filters such as price, gender, product type, color, or material.
        If CATALOG_CONTEXT says a field is unavailable, say it is unavailable.
        If there are no matching products, say that clearly and do not suggest fake products.
        Keep responses concise, practical, and shopping-focused.
        Do not output productId, imageUrl, raw product payloads, JSON, XML, function calls, or hidden metadata.
        Product cards are rendered by Kotlin structured state; your job is wording only.
        Limit the answer to about 120 words unless the user asks for detailed comparison.
    """.trimIndent()

    override suspend fun sendMessage(
        message: String,
        history: List<ChatMessage>,
        intent: String,
        catalogContext: String,
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw IllegalStateException("Groq API key is missing.")
        }

        val historyMessages = history
            .filter { !it.isPending }
            .takeLast(8)
            .map { msg ->
                GroqMessage(
                    role = if (msg.role == ChatRole.USER) "user" else "assistant",
                    content = msg.content,
                )
            }

        val groundedUserMessage = """
            INTENT: $intent

            CATALOG_CONTEXT:
            $catalogContext

            USER_QUERY:
            $message
        """.trimIndent()

        val request = GroqRequest(
            model = "llama-3.1-8b-instant",
            messages = listOf(GroqMessage(role = "system", content = systemPrompt)) +
                    historyMessages +
                    GroqMessage(role = "user", content = groundedUserMessage),
            tools = null,
            maxTokens = 380,
            temperature = 0.2,
        )

        Log.d(
            "GROQ_CHAT_DEBUG",
            "Outgoing grounded request intent=$intent history=${historyMessages.size} messageChars=${message.length} contextChars=${catalogContext.length}",
        )

        val response = try {
            groqApiService.chatCompletions("Bearer $apiKey", request)
        } catch (e: Exception) {
            Log.e("GROQ_CHAT_DEBUG", "Network execution failed", e)
            throw mapErrorToException(e)
        }

        val rawCode = response.code()
        val responseText = if (response.isSuccessful) {
            response.body()
                ?.choices
                ?.firstOrNull()
                ?.message
                ?.content
                ?.trim()
        } else {
            response.errorBody()?.string().orEmpty()
        }
        Log.d("GROQ_CHAT_DEBUG", "Incoming response code=$rawCode bodyChars=${responseText.orEmpty().length}")

        if (!response.isSuccessful) {
            throw mapHttpErrorToException(rawCode)
        }

        responseText
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Stylist response empty.")
    }

    private fun mapErrorToException(e: Exception): Exception = Exception(
        "Unable to reach the fashion assistant right now. Please check your internet connection.",
        e,
    )

    private fun mapHttpErrorToException(code: Int): Exception {
        val message = when (code) {
            429 -> "The fashion assistant is receiving too many requests. Please try again shortly."
            401, 403 -> "Stylist connection error. There's a credential mismatch behind the scenes."
            else -> "Unable to reach the fashion assistant right now. Please check your internet connection."
        }
        return Exception(message)
    }
}
