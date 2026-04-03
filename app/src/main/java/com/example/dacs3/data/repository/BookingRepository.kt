package com.example.dacs3.data.repository

import android.util.Log
import com.example.dacs3.data.model.Booking
import com.example.dacs3.data.model.BookingStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BookingRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val bookingsCollection = firestore.collection("bookings")

    /**
     * Tạo một đặt chỗ mới
     */
    suspend fun createBooking(booking: Booking): Boolean {
        return try {
            val bookingData = hashMapOf(
                "id" to booking.id,
                "tourId" to booking.tour.id,
                "status" to booking.status.name,
                "startDate" to booking.startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                "adults" to booking.adults,
                "children" to booking.children,
                "infants" to booking.infants,
                "totalPrice" to booking.totalPrice,
                "note" to booking.note,
                "customerName" to booking.customerName,
                "email" to booking.email,
                "phone" to booking.phone,
                "address" to booking.address,
                "paymentMethod" to booking.paymentMethod,
                "receiptUrl" to booking.receiptUrl,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "tripStatus" to booking.tripStatus
            )
            
            bookingsCollection.document(booking.id).set(bookingData).await()
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Lỗi tạo đặt chỗ: ${e.message}")
            false
        }
    }

    /**
     * Lấy danh sách đặt chỗ của người dùng (theo email)
     */
    suspend fun getBookingsByUser(email: String): List<Booking> {
        return try {
            val querySnapshot = bookingsCollection
                .whereEqualTo("email", email)
                .get()
                .await()
            
            val tourRepository = TourRepository()
            
            querySnapshot.documents.mapNotNull { doc ->
                val tourId = doc.getString("tourId") ?: return@mapNotNull null
                val tour = tourRepository.getTourById(tourId) ?: return@mapNotNull null
                
                Booking(
                    id = doc.id,
                    tour = tour,
                    status = BookingStatus.valueOf(doc.getString("status") ?: "PENDING"),
                    startDate = LocalDate.parse(doc.getString("startDate"), DateTimeFormatter.ISO_LOCAL_DATE),
                    adults = doc.getLong("adults")?.toInt() ?: 0,
                    children = doc.getLong("children")?.toInt() ?: 0,
                    infants = doc.getLong("infants")?.toInt() ?: 0,
                    totalPrice = doc.getLong("totalPrice") ?: 0L,
                    note = doc.getString("note"),
                    customerName = doc.getString("customerName") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    address = doc.getString("address") ?: "",
                    paymentMethod = doc.getString("phone") ?: "CASH",
                    receiptUrl = doc.getString("receiptUrl"),
                    createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                    tripStatus = doc.getString("tripStatus") ?: "preparing"
                )
            }.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            Log.e("BookingRepository", "Lỗi lấy danh sách đặt chỗ: ${e.message}")
            emptyList()
        }
    }

    /**
     * Lấy chi tiết một đặt chỗ theo ID
     */
    suspend fun getBookingById(bookingId: String): Booking? {
        return try {
            val doc = bookingsCollection.document(bookingId).get().await()
            if (!doc.exists()) return null
            
            val tourId = doc.getString("tourId") ?: return null
            val tour = TourRepository().getTourById(tourId) ?: return null
            
            Booking(
                id = doc.id,
                tour = tour,
                status = BookingStatus.valueOf(doc.getString("status") ?: "PENDING"),
                startDate = LocalDate.parse(doc.getString("startDate"), DateTimeFormatter.ISO_LOCAL_DATE),
                adults = doc.getLong("adults")?.toInt() ?: 0,
                children = doc.getLong("children")?.toInt() ?: 0,
                infants = doc.getLong("infants")?.toInt() ?: 0,
                totalPrice = doc.getLong("totalPrice") ?: 0L,
                note = doc.getString("note"),
                customerName = doc.getString("customerName") ?: "",
                email = doc.getString("email") ?: "",
                phone = doc.getString("phone") ?: "",
                address = doc.getString("address") ?: "",
                paymentMethod = doc.getString("paymentMethod") ?: "CASH",
                receiptUrl = doc.getString("receiptUrl"),
                createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis(),
                tripStatus = doc.getString("tripStatus") ?: "preparing"
            )
        } catch (e: Exception) {
            Log.e("BookingRepository", "Lỗi lấy chi tiết đặt chỗ: ${e.message}")
            null
        }
    }

    /**
     * Hủy đặt chỗ hoặc cập nhật trạng thái
     */
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Boolean {
        return try {
            bookingsCollection.document(bookingId)
                .update("status", status.name)
                .await()
            true
        } catch (e: Exception) {
            Log.e("BookingRepository", "Lỗi cập nhật trạng thái: ${e.message}")
            false
        }
    }
}
