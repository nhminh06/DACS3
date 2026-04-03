package com.example.dacs3.admin.config;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FirebaseDataInitializer {

    @Autowired
    private Firestore firestore;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeData() {
        System.out.println("---- Check Firebase data ----");
        
        createCollectionIfEmpty("users", "sample_user", Map.of(
            "name", "Quản trị viên",
            "email", "admin@example.com",
            "role", "admin",
            "trang_thai", "active"
        ));

        createCollectionIfEmpty("tours", "sample_tour", Map.of(
            "maTour", "TOUR001",
            "title", "Tour mẫu Đà Nẵng",
            "price", 1000000L,
            "location", "Đà Nẵng",
            "trang_thai", "active"
        ));

        createCollectionIfEmpty("bookings", "sample_booking", Map.of(
            "userName", "Khách hàng mẫu",
            "tourTitle", "Tour mẫu Đà Nẵng",
            "totalPrice", 1000000L,
            "status", "PENDING"
        ));

        System.out.println("---- done:) ----");
    }

    private void createCollectionIfEmpty(String collectionName, String sampleId, Map<String, Object> data) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName).limit(1).get();
            if (future.get().isEmpty()) {
                System.out.println("Hệ thống: Bảng [" + collectionName + "] chưa có dữ liệu. Đang tạo dữ liệu mẫu...");
                firestore.collection(collectionName).document(sampleId).set(data);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tạo bảng " + collectionName + ": " + e.getMessage());
        }
    }
}
