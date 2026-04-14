package com.example.dacs3.data.repository

import com.example.dacs3.data.model.Comment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ArticleEntity(
    val id: String = "",
    val tieu_de: String = "",
    val sections: List<Map<String, String>> = emptyList(),
    val loai_id: Int = 1,
    val ngay_tao: String = "",
    val tour_id: String? = null,
    val trang_thai: Int = 1, // 0: Đang duyệt, 1: Đã duyệt, 2: Bị từ chối
    val so_muc: Int = 0,
    val nguon_goc: String = "admin", // admin hoặc user
    val tac_gia: String = "Admin",
    val tac_gia_id: String = "", // ID của user nếu nguon_goc là user
    val is_edited: Boolean = false // Đánh dấu bài viết đã được chỉnh sửa
)

class ArticleRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun getHomeArticles(limit: Long = 3): List<ArticleEntity> {
        return try {
            val snapshot = db.collection("articles")
                .whereEqualTo("trang_thai", 1)
                .limit(limit)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ArticleEntity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getAllArticles(): List<ArticleEntity> {
        return try {
            val snapshot = db.collection("articles")
                .whereEqualTo("trang_thai", 1)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ArticleEntity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getArticlesByUser(userId: String): List<ArticleEntity> {
        return try {
            val snapshot = db.collection("articles")
                .whereEqualTo("tac_gia_id", userId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ArticleEntity::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.ngay_tao }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun createArticle(article: ArticleEntity): Boolean {
        return try {
            val articleData = hashMapOf(
                "tieu_de" to article.tieu_de,
                "loai_id" to article.loai_id,
                "sections" to article.sections,
                "ngay_tao" to article.ngay_tao,
                "trang_thai" to article.trang_thai,
                "so_muc" to article.sections.size,
                "nguon_goc" to article.nguon_goc,
                "tac_gia" to article.tac_gia,
                "tac_gia_id" to article.tac_gia_id,
                "is_edited" to false
            )
            db.collection("articles").add(articleData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateArticle(article: ArticleEntity): Boolean {
        return try {
            val updateData = hashMapOf(
                "tieu_de" to article.tieu_de,
                "loai_id" to article.loai_id,
                "sections" to article.sections,
                "so_muc" to article.sections.size,
                "is_edited" to true // Giữ nguyên trạng thái cũ, chỉ đánh dấu đã sửa
            )
            db.collection("articles").document(article.id)
                .update(updateData as Map<String, Any>)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Comment Section ---

    suspend fun getComments(articleId: String): List<Comment> {
        return try {
            val snapshot = db.collection("articles").document(articleId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addComment(comment: Comment): Boolean {
        return try {
            val commentData = mapOf(
                "articleId" to comment.articleId,
                "userId" to comment.userId,
                "userName" to comment.userName,
                "userAvatar" to comment.userAvatar,
                "content" to comment.content,
                "createdAt" to (comment.createdAt ?: Timestamp.now()),
                "likes" to 0,
                "likedBy" to emptyList<String>()
            )
            db.collection("articles").document(comment.articleId)
                .collection("comments")
                .add(commentData)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteComment(articleId: String, commentId: String): Boolean {
        return try {
            db.collection("articles").document(articleId)
                .collection("comments").document(commentId)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun toggleLikeComment(articleId: String, commentId: String, userId: String): Boolean {
        return try {
            val commentRef = db.collection("articles").document(articleId)
                .collection("comments").document(commentId)
            
            db.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                @Suppress("UNCHECKED_CAST")
                val likedBy = snapshot.get("likedBy") as? List<String> ?: emptyList()
                val isLiked = likedBy.contains(userId)
                
                if (isLiked) {
                    transaction.update(commentRef, "likes", FieldValue.increment(-1))
                    transaction.update(commentRef, "likedBy", FieldValue.arrayRemove(userId))
                } else {
                    transaction.update(commentRef, "likes", FieldValue.increment(1))
                    transaction.update(commentRef, "likedBy", FieldValue.arrayUnion(userId))
                }
            }.await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
