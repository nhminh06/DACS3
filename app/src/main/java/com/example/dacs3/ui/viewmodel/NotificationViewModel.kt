package com.example.dacs3.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var systemListener: ListenerRegistration? = null
    private var reportListener: ListenerRegistration? = null
    private var contactListener: ListenerRegistration? = null

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount

    private val _systemMeta = MutableStateFlow(Pair(0, 0L))
    private val _reportMeta = MutableStateFlow(Pair(0, 0L))
    private val _contactMeta = MutableStateFlow(Pair(0, 0L))

    val systemMeta: StateFlow<Pair<Int, Long>> = _systemMeta
    val reportMeta: StateFlow<Pair<Int, Long>> = _reportMeta
    val contactMeta: StateFlow<Pair<Int, Long>> = _contactMeta

    fun startListening(userId: String) {
        stopListening()

        // 1. Hệ thống (Tour)
        systemListener = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, _ ->
                val count = snapshot?.size() ?: 0
                val latest = snapshot?.documents?.maxOfOrNull { (it.get("timestamp") as? Timestamp)?.seconds ?: 0L } ?: 0L
                _systemMeta.value = Pair(count, latest)
                updateTotalCount()
            }

        // 2. Báo cáo (Reports)
        reportListener = firestore.collection("reports")
            .whereEqualTo("reporterId", userId)
            .addSnapshotListener { snapshot, _ ->
                val unreadDocs = snapshot?.documents?.filter { 
                    it.get("reply") != null && it.getBoolean("isSeen") != true 
                } ?: emptyList()
                val count = unreadDocs.size
                val latest = unreadDocs.maxOfOrNull { (it.get("createdAt") as? Timestamp)?.seconds ?: 0L } ?: 0L
                _reportMeta.value = Pair(count, latest)
                updateTotalCount()
            }

        // 3. Liên hệ (Contacts)
        contactListener = firestore.collection("contacts")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                val unreadDocs = snapshot?.documents?.filter { 
                    it.get("reply") != null && it.getBoolean("isSeen") != true 
                } ?: emptyList()
                val count = unreadDocs.size
                val latest = unreadDocs.maxOfOrNull { (it.get("replyAt") as? Timestamp)?.seconds ?: (it.get("timestamp") as? Timestamp)?.seconds ?: 0L } ?: 0L
                _contactMeta.value = Pair(count, latest)
                updateTotalCount()
            }
    }

    private fun updateTotalCount() {
        _unreadCount.value = _systemMeta.value.first + _reportMeta.value.first + _contactMeta.value.first
    }

    fun stopListening() {
        systemListener?.remove()
        reportListener?.remove()
        contactListener?.remove()
    }

    fun markAllAsRead(userId: String) {
        viewModelScope.launch {
            try {
                val batch = firestore.batch()

                // Mark system notifications as read
                val systemNotifs = firestore.collection("notifications")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isRead", false)
                    .get().await()
                for (doc in systemNotifs) batch.update(doc.reference, "isRead", true)

                // Mark reports as seen
                val reports = firestore.collection("reports")
                    .whereEqualTo("reporterId", userId)
                    .get().await()
                for (doc in reports) {
                    if (doc.get("reply") != null && doc.getBoolean("isSeen") != true) {
                        batch.update(doc.reference, "isSeen", true)
                    }
                }

                // Mark contacts as seen
                val contacts = firestore.collection("contacts")
                    .whereEqualTo("userId", userId)
                    .get().await()
                for (doc in contacts) {
                    if (doc.get("reply") != null && doc.getBoolean("isSeen") != true) {
                        batch.update(doc.reference, "isSeen", true)
                    }
                }

                batch.commit().await()
                
                // Cập nhật UI ngay lập tức
                _unreadCount.value = 0
                _systemMeta.value = Pair(0, _systemMeta.value.second)
                _reportMeta.value = Pair(0, _reportMeta.value.second)
                _contactMeta.value = Pair(0, _contactMeta.value.second)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
