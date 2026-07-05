package com.example.wearzone.data.remote.ai.chat.dto

import com.google.gson.annotations.SerializedName

data class GroqRequest(
    @SerializedName("model") val model: String = "llama-3.3-70b-versatile",
    @SerializedName("messages") val messages: List<GroqMessage>,
    @SerializedName("tools") val tools: List<GroqTool>? = null
)

data class GroqMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String? = null,
    @SerializedName("tool_calls") val toolCalls: List<GroqToolCall>? = null,
    @SerializedName("tool_call_id") val toolCallId: String? = null,
    @SerializedName("name") val name: String? = null
)

data class GroqToolCall(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String = "function",
    @SerializedName("function") val function: GroqFunctionCall
)

data class GroqFunctionCall(
    @SerializedName("name") val name: String,
    @SerializedName("arguments") val arguments: String // JSON string
)

data class GroqTool(
    @SerializedName("type") val type: String = "function",
    @SerializedName("function") val function: GroqFunctionDeclaration
)

data class GroqFunctionDeclaration(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("parameters") val parameters: GroqSchema
)

data class GroqSchema(
    @SerializedName("type") val type: String,
    @SerializedName("properties") val properties: Map<String, GroqSchema>? = null,
    @SerializedName("required") val required: List<String>? = null,
    @SerializedName("description") val description: String? = null
)

data class GroqResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("object") val obj: String? = null,
    @SerializedName("created") val created: Long? = null,
    @SerializedName("model") val model: String? = null,
    @SerializedName("choices") val choices: List<GroqChoice>? = null,
    @SerializedName("usage") val usage: GroqUsage? = null
)

data class GroqChoice(
    @SerializedName("index") val index: Int? = null,
    @SerializedName("message") val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String? = null
)

data class GroqUsage(
    @SerializedName("prompt_tokens") val promptTokens: Int? = null,
    @SerializedName("completion_tokens") val completionTokens: Int? = null,
    @SerializedName("total_tokens") val totalTokens: Int? = null
)
