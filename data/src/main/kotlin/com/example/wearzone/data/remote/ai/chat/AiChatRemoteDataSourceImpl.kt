package com.example.wearzone.data.remote.ai.chat

import android.util.Log
import com.example.wearzone.data.remote.ai.chat.api.GroqApiService
import com.example.wearzone.data.remote.ai.chat.dto.*
import com.example.wearzone.domain.ai.chat.model.ChatMessage
import com.example.wearzone.domain.ai.chat.model.ChatRole
import com.example.wearzone.domain.common.DataResult
import com.example.wearzone.domain.product.usecase.GetProductDetailUseCase
import com.example.wearzone.domain.product.usecase.SearchProductsUseCase
import com.example.wearzone.domain.search.model.SearchFilters
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
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getProductDetailUseCase: GetProductDetailUseCase
) : IAiChatRemoteDataSource {

    init {
        Log.d("GROQ_CHAT_DEBUG", "Target Key Length: ${apiKey.length}")
    }

    // ─── Tool definitions ───────────────────────────────────────────────────

    private val tools = listOf(
        GroqTool(
            type = "function",
            function = GroqFunctionDeclaration(
                name = "searchProducts",
                description = "Search the WearZone database. If the user asks for specific items, use that keyword (e.g., 'shirts'). If the user asks broad questions like 'what do you sell' or 'show me clothes', use the query 'all' or 'clothing'.",
                parameters = GroqSchema(
                    type = "object",
                    properties = mapOf(
                        "query" to GroqSchema(
                            type = "string",
                            description = "The search keyword(s)."
                        )
                    ),
                    required = listOf("query")
                )
            )
        ),
        GroqTool(
            type = "function",
            function = GroqFunctionDeclaration(
                name = "getProductDetail",
                description = "Get full details (price, description, variants) of a specific product by its ID.",
                parameters = GroqSchema(
                    type = "object",
                    properties = mapOf(
                        "productId" to GroqSchema(
                            type = "string",
                            description = "The numeric product ID as a string."
                        )
                    ),
                    required = listOf("productId")
                )
            )
        )
    )

    private val systemPrompt = "You are the WearZone AI Stylist. Your SOLE purpose is to assist users with finding clothing, translating their fashion needs into specific products, and comparing items. You MUST strictly refuse to answer any questions outside of fashion, e-commerce, and WearZone products. Keep answers concise. When users ask for products or comparisons, ALWAYS use your tools to fetch real data and include the Product IDs in your final response. CRITICAL RULES: 1. If a tool returns an empty result, 'No products found', or an error, you MUST NOT call the tool again with new keywords. Accept the defeat immediately and politely tell the user you couldn't find matching items. 2. NEVER output raw XML or <function> tags. Only use the native JSON tool calling schema."

    // ─── Core entry point ────────────────────────────────────────────────────

    override suspend fun sendMessage(message: String, history: List<ChatMessage>): String =
        withContext(Dispatchers.IO) {
            try {
                // Build the messages list
                val messagesList = mutableListOf<GroqMessage>()

                // Add system prompt first
                messagesList.add(GroqMessage(role = "system", content = systemPrompt))

                // Exclude pending messages and map history to GroqMessage
                val historyMessages = history.filter { !it.isPending }.map { msg ->
                    GroqMessage(
                        role = if (msg.role == ChatRole.USER) "user" else "assistant",
                        content = msg.content
                    )
                }

                val newMsg = GroqMessage(role = "user", content = message)
                val totalConversational = historyMessages + newMsg

                // Trim to keep at most last 10 entries of the conversation
                val trimmedConversational = if (totalConversational.size > 10) {
                    Log.d("GROQ_CHAT_DEBUG", "Trimming history: conversation size ${totalConversational.size} exceeds 10. Discarding oldest ${totalConversational.size - 10} messages.")
                    totalConversational.takeLast(10)
                } else {
                    totalConversational
                }

                messagesList.addAll(trimmedConversational)

                var currentMessages = messagesList.toMutableList()
                var loopCount = 0
                val maxLoops = 3

                while (loopCount < maxLoops) {
                    loopCount++

                    val request = GroqRequest(
                        model = "llama-3.1-8b-instant",
                        messages = currentMessages,
                        tools = tools
                    )

                    // Log the exact outgoing JSON payload string before network request
                    val requestJson = com.google.gson.Gson().toJson(request)
                    Log.d("GROQ_CHAT_DEBUG", "Outgoing JSON payload: $requestJson")

                    val response = try {
                        groqApiService.chatCompletions("Bearer $apiKey", request)
                    } catch (e: Exception) {
                        Log.e("GROQ_CHAT_DEBUG", "Network execution failed", e)
                        throw mapErrorToException(e)
                    }

                    // Log raw incoming HTTP response code, headers, and body string
                    val rawCode = response.code()
                    val rawHeaders = response.headers().toString()
                    val rawBodyString = if (response.isSuccessful) {
                        com.google.gson.Gson().toJson(response.body())
                    } else {
                        response.errorBody()?.string() ?: ""
                    }
                    Log.d("GROQ_CHAT_DEBUG", "Raw Incoming Response:\nCode: $rawCode\nHeaders:\n$rawHeaders\nBody:\n$rawBodyString")

                    if (!response.isSuccessful) {
                        if (rawCode == 400) {
                            Log.e("GROQ_CHAT_DEBUG", "Intercepted HTTP 400, returning graceful fallback.")
                            return@withContext "I'm having a little trouble parsing that request. Could you rephrase what you're looking for?"
                        }
                        val errorException = mapHttpErrorToException(rawCode, rawBodyString)
                        Log.e("GROQ_CHAT_DEBUG", "HTTP error exception: ${errorException.message}")
                        throw errorException
                    }

                    val groqResponseObj = response.body()
                    val choice = groqResponseObj?.choices?.firstOrNull()
                    val responseMsg = choice?.message

                    if (responseMsg == null) {
                        throw Exception("Stylist response empty.")
                    }

                    currentMessages.add(responseMsg)

                    val toolCalls = responseMsg.toolCalls
                    if (!toolCalls.isNullOrEmpty()) {
                        Log.d("GROQ_CHAT_DEBUG", "Tool call requested: ${toolCalls.map { it.function.name }}")
                        for (toolCall in toolCalls) {
                            val toolResult = executeTool(toolCall)
                            Log.d("GROQ_CHAT_DEBUG", "Tool execution result: $toolResult")

                            currentMessages.add(
                                GroqMessage(
                                    role = "tool",
                                    toolCallId = toolCall.id,
                                    name = toolCall.function.name,
                                    content = toolResult
                                )
                            )
                        }
                        continue
                    }

                    return@withContext responseMsg.content ?: "I'm here to help you shop! Try asking me about specific products, styles, or brands."
                }

                return@withContext "I checked our catalog but couldn't find exactly what you're looking for right now. Could you try a different style or keyword?"

            } catch (e: Exception) {
                Log.e("GROQ_CHAT_DEBUG", "Exception in sendMessage stack trace:", e)
                throw e
            }
        }

    private suspend fun executeTool(toolCall: GroqToolCall): String {
        return when (toolCall.function.name) {
            "searchProducts" -> {
                val argsJson = try {
                    com.google.gson.JsonParser.parseString(toolCall.function.arguments).asJsonObject
                } catch (e: Exception) {
                    null
                }
                val query = argsJson?.get("query")?.asString ?: ""
                handleSearchProducts(query)
            }
            "getProductDetail" -> {
                val argsJson = try {
                    com.google.gson.JsonParser.parseString(toolCall.function.arguments).asJsonObject
                } catch (e: Exception) {
                    null
                }
                val productIdStr = argsJson?.get("productId")?.asString ?: ""
                val productId = productIdStr.toLongOrNull() ?: 0L
                handleGetProductDetail(productId)
            }
            else -> "Error: Unknown tool function name ${toolCall.function.name}"
        }
    }

    private suspend fun handleSearchProducts(query: String): String {
        val searchResult = searchProductsUseCase(SearchFilters(query = query))
        return when (searchResult) {
            is DataResult.Success -> {
                val products = searchResult.data
                if (products.isEmpty()) {
                    "{\"status\": \"empty\", \"message\": \"Zero products found. Stop searching.\"}"
                } else {
                    val list = products.take(8).joinToString(", ") { 
                        val escapedTitle = it.title.replace("\"", "\\\"")
                        "{\"id\": \"${it.id}\", \"title\": \"$escapedTitle\", \"price\": \"${it.price}\"}" 
                    }
                    "{\"products\": [$list]}"
                }
            }
            is DataResult.Error -> "{\"status\": \"empty\", \"message\": \"Zero products found. Stop searching.\"}"
        }
    }

    private suspend fun handleGetProductDetail(productId: Long): String {
        val detailResult = getProductDetailUseCase(productId)
        return when (detailResult) {
            is DataResult.Success -> {
                val d = detailResult.data
                val escapedTitle = d.title.replace("\"", "\\\"")
                val escapedPrice = d.price.toString()
                val escapedDesc = d.descriptionHtml.take(500).replace("\"", "\\\"")
                "{\"id\": \"${d.id}\", \"title\": \"$escapedTitle\", \"price\": \"$escapedPrice\", \"description\": \"$escapedDesc\"}"
            }
            is DataResult.Error -> "{\"status\": \"empty\", \"message\": \"Zero products found. Stop searching.\"}"
        }
    }

    private fun mapErrorToException(e: Exception): Exception {
        return Exception("Unable to reach the fashion assistant right now. Please check your internet connection.", e)
    }

    private fun mapHttpErrorToException(code: Int, bodyString: String): Exception {
        val message = when (code) {
            429 -> "Whoops, you're browsing style advice faster than our servers can process! Please wait a few seconds and try again."
            401, 403 -> "Stylist connection error. There's a credential mismatch behind the scenes."
            else -> "Unable to reach the fashion assistant right now. Please check your internet connection."
        }
        return Exception(message)
    }
}
