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
    val trang_thai: Int = 1,
    val so_muc: Int = 0
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

    suspend fun toggleLikeComment(articleId: String, commentId: String, userId: String, isLiked: Boolean): Boolean {
        return try {
            val commentRef = db.collection("articles").document(articleId)
                .collection("comments").document(commentId)
            
            if (isLiked) {
                // Unlike: Remove userId and decrement count
                commentRef.update(
                    "likes", FieldValue.increment(-1),
                    "likedBy", FieldValue.arrayRemove(userId)
                ).await()
            } else {
                // Like: Add userId and increment count
                commentRef.update(
                    "likes", FieldValue.increment(1),
                    "likedBy", FieldValue.arrayUnion(userId)
                ).await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
