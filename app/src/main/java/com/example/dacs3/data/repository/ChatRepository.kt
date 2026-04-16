package com.example.dacs3.data.repository

import android.util.Log
import com.example.dacs3.BuildConfig
import com.example.dacs3.data.model.Tour
import com.example.dacs3.data.remote.ChatApiService
import com.example.dacs3.data.remote.ChatMessage
import com.example.dacs3.data.remote.ChatRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonParser
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

class ChatRepository {
    private val db = FirebaseFirestore.getInstance()
    private val groqApiKey = BuildConfig.GROQ_API_KEY

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.groq.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val chatApiService = retrofit.create(ChatApiService::class.java)

    private fun sanitize(text: String?): String {
        return text?.replace(Regex("[\\p{Cntrl}&&[^\r\n\t]]"), "")?.trim() ?: ""
    }

    suspend fun getAiResponse(userQuestion: String): Map<String, Any> {
        try {
            if (groqApiKey.isBlank()) {
                return mapOf("answer" to "Lỗi: API Key chưa được thiết lập.", "tours" to emptyList<Tour>(), "articles" to emptyList<ArticleEntity>())
            }

            // Tăng limit để AI có nhiều dữ liệu tham khảo hơn
            val tours = try {
                db.collection("tours").whereEqualTo("trang_thai", "active").limit(10).get().await().toObjects(Tour::class.java)
            } catch (e: Exception) { emptyList() }

            val articles = try {
                db.collection("articles").whereEqualTo("trang_thai", 1).limit(10).get().await().toObjects(ArticleEntity::class.java)
            } catch (e: Exception) { emptyList() }

            val tourContext = tours.joinToString("\n") { "- ${sanitize(it.title)} | ${sanitize(it.location)}: ${sanitize(it.traiNghiem).take(150)}..." }
            val articleContext = articles.joinToString("\n") { "- ${sanitize(it.tieu_de)}: ${sanitize(it.sections.firstOrNull()?.get("content")?.toString()).take(150)}..." }

            val fullContext = "DANH SÁCH TOUR CỦA WIND:\n$tourContext\n\nDANH SÁCH BÀI VIẾT CỦA WIND:\n$articleContext"

            val messages = listOf(
                ChatMessage(role = "system", content = """
                    Bạn là trợ lý du lịch WIND thông minh và am hiểu sâu sắc về du lịch, văn hóa, ẩm thực Việt Nam.
                    Nhiệm vụ của bạn:
                    1. Sử dụng kiến thức rộng lớn của bạn về Việt Nam để trả lời mọi câu hỏi của người dùng (về địa danh, món ăn, lịch sử...).
                    2. NẾU trong 'Dữ liệu từ WIND' có Tour hoặc Bài viết liên quan trực tiếp đến câu hỏi, hãy ưu tiên giới thiệu và cung cấp tên chính xác của chúng.
                    3. Trả lời bằng tiếng Việt, giọng điệu hào hứng, thân thiện và chuyên nghiệp.
                    4. Nếu người dùng hỏi về món ăn (như Cao lầu), hãy tả về nó dựa trên kiến thức của bạn và gợi ý nếu WIND có tour đến địa điểm đó.
                """.trimIndent()),
                ChatMessage(role = "user", content = "Dữ liệu từ WIND:\n$fullContext\n\nCâu hỏi từ khách hàng: ${sanitize(userQuestion)}")
            )

            val request = ChatRequest(
                model = "llama-3.3-70b-versatile",
                messages = messages,
                max_tokens = 1500, // Tăng token để AI trả lời chi tiết hơn
                temperature = 0.7f
            )

            val response = chatApiService.getChatCompletion("Bearer $groqApiKey", request)
            val aiText = response.choices.firstOrNull()?.message?.content ?: "WIND hiện đang bận một chút, bạn thử lại sau nhé!"

            // Logic lọc kết quả liên quan chính xác hơn
            val relatedTours = tours.filter { tour ->
                userQuestion.contains(tour.location, ignoreCase = true) || 
                aiText.contains(tour.title, ignoreCase = true) ||
                aiText.contains(tour.location, ignoreCase = true)
            }

            return mapOf(
                "answer" to aiText,
                "tours" to relatedTours,
                "articles" to articles.filter { aiText.contains(it.tieu_de, ignoreCase = true) }
            )

        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            Log.e("ChatRepository", "HTTP ERROR: $errorBody")
            return mapOf("answer" to "WIND gặp lỗi kỹ thuật (${e.code()}). Vui lòng thử lại!", "tours" to emptyList<Tour>(), "articles" to emptyList<ArticleEntity>())
        } catch (e: Exception) {
            return mapOf("answer" to "Lỗi kết nối: ${e.message}", "tours" to emptyList<Tour>(), "articles" to emptyList<ArticleEntity>())
        }
    }
}
