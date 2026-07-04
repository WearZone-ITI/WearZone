package com.example.wearzone.data.remote.ai.chat.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val tools: List<Tool>? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val functionCall: FunctionCall? = null,
    val functionResponse: FunctionResponse? = null
)

@Serializable
data class FunctionCall(
    val name: String,
    val args: JsonObject
)

@Serializable
data class FunctionResponse(
    val name: String,
    val response: JsonObject
)

@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>
)

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: Schema
)

@Serializable
data class Schema(
    val type: String,
    val properties: Map<String, Schema>? = null,
    val required: List<String>? = null,
    val description: String? = null,
    val items: Schema? = null
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null
)
