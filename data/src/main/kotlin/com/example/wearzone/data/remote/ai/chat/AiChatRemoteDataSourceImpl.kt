package com.example.wearzone.data.remote.ai.chat

import android.util.Log
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.product.usecase.SearchProductsUseCase
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.search.model.SearchFilters
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.data.remote.ai.chat.api.GeminiApiService
import com.example.wearzone.data.remote.ai.chat.dto.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApiKey

class AiChatRemoteDataSourceImpl @Inject constructor(
    @GeminiApiKey private val apiKey: String,
    private val geminiApiService: GeminiApiService,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase
) : IAiChatRemoteDataSource {

    init {
        Log.d("GEMINI_INIT", "Target Key Length: ${apiKey.length}")
    }

    // ─── Tool definitions ───────────────────────────────────────────────────

    private val tools = listOf(
        Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "searchProducts",
                    description = "Search for products in the WearZone store by a keyword, e.g. 'shirts', 'sneakers', 'summer dresses'.",
                    parameters = Schema(
                        type = "OBJECT",
                        properties = mapOf(
                            "query" to Schema(
                                type = "STRING",
                                description = "The search keyword(s)."
                            )
                        ),
                        required = listOf("query")
                    )
                ),
                FunctionDeclaration(
                    name = "getProductDetail",
                    description = "Get full details (price, description, variants) of a specific product by its ID.",
                    parameters = Schema(
                        type = "OBJECT",
                        properties = mapOf(
                            "productId" to Schema(
                                type = "STRING",
                                description = "The numeric product ID as a string."
                            )
                        ),
                        required = listOf("productId")
                    )
                )
            )
        )
    )

    // systemInstruction must NOT have a role field per Gemini REST spec
    private val systemInstruction = Content(
        parts = listOf(
            Part(
                text = """You are WearZone AI — a friendly, knowledgeable shopping assistant for the WearZone fashion app. 
Help users discover products, compare options, and make purchase decisions.
When a user asks about products, always call the searchProducts function to get real data from the store.
Present results in a clear, scannable format with bullet points.
Be concise but enthusiastic about fashion."""
            )
        )
    )

    // ─── Core entry point ────────────────────────────────────────────────────

    override suspend fun sendMessage(message: String, history: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            try {
                // Build chat history, mapping domain → REST DTOs
                // Exclude system instruction turns and filter to valid roles only
                val chatHistory = history
                    .filter { !it.isPending }
                    .map { msg ->
                        Content(
                            role = if (msg.role == ChatRole.USER) "user" else "model",
                            parts = listOf(Part(text = msg.content))
                        )
                    }
                    .toMutableList()

                // Append the new user message
                chatHistory.add(Content(role = "user", parts = listOf(Part(text = message))))

                val url = "v1beta/models/gemini-3.5-flash:generateContent"

                val request = GeminiRequest(
                    contents = chatHistory,
                    tools = tools,
                    systemInstruction = systemInstruction
                )

                val response = geminiApiService.generateContent(url, apiKey, request)
                val candidate = response.candidates?.firstOrNull()

                // ─── Function call dispatch ───────────────────────────────────
                val functionCall = candidate?.content?.parts
                    ?.firstOrNull { it.functionCall != null }
                    ?.functionCall

                if (functionCall != null) {
                    return@withContext dispatchFunctionCall(
                        functionCall = functionCall,
                        modelContent = candidate.content,
                        chatHistory = chatHistory,
                        url = url
                    )
                }

                // ─── Plain text response ──────────────────────────────────────
                candidate?.content?.parts
                    ?.firstOrNull { it.text != null }
                    ?.text
                    ?: "I'm here to help you shop! Try asking me about specific products, styles, or brands."

            } catch (e: Exception) {
                Log.e("AiChat", "Error: ${e.message}", e)
                "Sorry, I ran into an issue. Please try again in a moment."
            }
        }

    // ─── Function call handler ───────────────────────────────────────────────

    private suspend fun dispatchFunctionCall(
        functionCall: FunctionCall,
        modelContent: Content,
        chatHistory: MutableList<Content>,
        url: String
    ): String {
        // Step 1: add model's function-call turn to history
        chatHistory.add(modelContent)

        return when (functionCall.name) {
            "searchProducts" -> {
                val query = functionCall.args["query"]?.jsonPrimitive?.content ?: ""
                handleSearchProducts(query, chatHistory, url)
            }

            "getProductDetail" -> {
                val productIdStr = functionCall.args["productId"]?.jsonPrimitive?.content ?: ""
                val productId = productIdStr.toLongOrNull() ?: 0L
                handleGetProductDetail(productId, chatHistory, url)
            }

            else -> {
                "I couldn't process that request. How else can I help you?"
            }
        }
    }

    // ─── Tool handlers ───────────────────────────────────────────────────────

    private suspend fun handleSearchProducts(
        query: String,
        chatHistory: MutableList<Content>,
        url: String
    ): String {
        val searchResult = searchProductsUseCase(SearchFilters(query = query))
        val functionResultJson = when (searchResult) {
            is DataResult.Success -> {
                val products = searchResult.data
                if (products.isEmpty()) {
                    JsonObject(mapOf("result" to JsonPrimitive("No products found for '$query'.")))
                } else {
                    val list = products.take(8).joinToString(", ") { "${it.title} (ID: ${it.id})" }
                    JsonObject(mapOf("products" to JsonPrimitive(list)))
                }
            }
            is DataResult.Error -> JsonObject(mapOf("error" to JsonPrimitive("Search failed.")))
        }

        // Step 2: add tool result as a "user" role turn (Gemini REST requirement)
        chatHistory.add(
            Content(
                role = "user",
                parts = listOf(
                    Part(
                        functionResponse = FunctionResponse(
                            name = "searchProducts",
                            response = functionResultJson
                        )
                    )
                )
            )
        )

        // Step 3: follow-up call for final natural-language answer
        val followUp = geminiApiService.generateContent(
            url = url,
            apiKey = apiKey,
            request = GeminiRequest(contents = chatHistory, tools = tools, systemInstruction = systemInstruction)
        )
        return followUp.candidates?.firstOrNull()?.content?.parts
            ?.firstOrNull { it.text != null }?.text
            ?: "I found some products that might interest you!"
    }

    private suspend fun handleGetProductDetail(
        productId: Long,
        chatHistory: MutableList<Content>,
        url: String
    ): String {
        val detailResult = getProductDetailUseCase(productId)
        val functionResultJson = when (detailResult) {
            is DataResult.Success -> {
                val d = detailResult.data
                JsonObject(
                    mapOf(
                        "title" to JsonPrimitive(d.title),
                        "price" to JsonPrimitive(d.price),
                        "description" to JsonPrimitive(d.descriptionHtml)
                    )
                )
            }
            is DataResult.Error -> JsonObject(mapOf("error" to JsonPrimitive("Could not fetch product details.")))
        }

        // Step 2: add tool result as "user" role (Gemini REST requirement)
        chatHistory.add(
            Content(
                role = "user",
                parts = listOf(
                    Part(
                        functionResponse = FunctionResponse(
                            name = "getProductDetail",
                            response = functionResultJson
                        )
                    )
                )
            )
        )

        // Step 3: follow-up call
        val followUp = geminiApiService.generateContent(
            url = url,
            apiKey = apiKey,
            request = GeminiRequest(contents = chatHistory, tools = tools, systemInstruction = systemInstruction)
        )
        return followUp.candidates?.firstOrNull()?.content?.parts
            ?.firstOrNull { it.text != null }?.text
            ?: "Here are the details for that product!"
    }
}
