package com.example.dacs3.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class ChatRequest(
    @SerializedName("model")
    val model: String = "llama-3.3-70b-versatile", // Cập nhật model mới tại đây

    @SerializedName("messages")
    val messages: List<ChatMessage>,

    @SerializedName("max_tokens")
    val max_tokens: Int = 1024,

    @SerializedName("temperature")
    val temperature: Float = 0.7f
)

data class ChatResponse(
    @SerializedName("choices")
    val choices: List<ChatChoice>
)

data class ChatChoice(
    @SerializedName("message")
    val message: ChatMessage
)

data class ChatMessage(
    @SerializedName("role")
    val role: String,

    @SerializedName("content")
    val content: String
)

interface ChatApiService {
    @POST("openai/v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authHeader: String,
        @Body request: ChatRequest
    ): ChatResponse
}
