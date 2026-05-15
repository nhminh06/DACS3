package com.example.dacs3.admin.controller;

import com.example.dacs3.admin.model.SupportMessage;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/support")
public class AdminSupportController {

    @Autowired
    private Firestore firestore;

    @GetMapping
    public String supportDashboard(Model model, @RequestParam(required = false) String userId) {
        try {
            // 1. Lấy danh sách các user đã từng nhắn tin (Unique UserIds)
            // Trong Firestore, tốt nhất là query từ collection messages và group ở code (hoặc có bảng chat_rooms)
            // Ở đây ta lấy 100 tin nhắn gần nhất để tìm các user đang active
            ApiFuture<QuerySnapshot> future = firestore.collection("support_messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(200)
                    .get();
            
            List<QueryDocumentSnapshot> allDocs = future.get().getDocuments();
            Set<String> activeUserIds = allDocs.stream()
                    .map(d -> d.getString("userId"))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            List<Map<String, Object>> chatUsers = new ArrayList<>();
            for (String uid : activeUserIds) {
                DocumentSnapshot userDoc = firestore.collection("users").document(uid).get().get();
                if (userDoc.exists()) {
                    Map<String, Object> userData = new HashMap<>(userDoc.getData());
                    userData.put("id", uid);
                    
                    // Lấy tin nhắn cuối cùng để hiển thị preview
                    Optional<QueryDocumentSnapshot> lastMsg = allDocs.stream()
                            .filter(d -> uid.equals(d.getString("userId")))
                            .findFirst();
                    
                    lastMsg.ifPresent(m -> {
                        userData.put("lastMessage", m.getString("text"));
                        userData.put("lastTime", m.getTimestamp("timestamp"));
                    });
                    
                    chatUsers.add(userData);
                }
            }

            model.addAttribute("chatUsers", chatUsers);
            model.addAttribute("selectedUserId", userId);

            if (userId != null && !userId.isEmpty()) {
                // 2. Lấy lịch sử chat với user được chọn
                // Bỏ orderBy để tránh lỗi thiếu Index Firestore (FAILED_PRECONDITION)
                // Sắp xếp dữ liệu ở phía server (in-memory)
                ApiFuture<QuerySnapshot> msgFuture = firestore.collection("support_messages")
                        .whereEqualTo("userId", userId)
                        .get();
                
                List<SupportMessage> messages = msgFuture.get().getDocuments().stream()
                        .map(d -> {
                            SupportMessage m = d.toObject(SupportMessage.class);
                            m.setId(d.getId());
                            return m;
                        })
                        .sorted(Comparator.comparing(SupportMessage::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())))
                        .collect(Collectors.toList());
                
                model.addAttribute("messages", messages);
                
                DocumentSnapshot selUser = firestore.collection("users").document(userId).get().get();
                model.addAttribute("selectedUser", selUser.getData());
            }

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "support/chat";
    }

    @PostMapping("/send")
    @ResponseBody
    public Map<String, Object> sendMessage(@RequestParam String userId, @RequestParam String text) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("userId", userId);
            message.put("text", text);
            message.put("senderRole", "admin");
            message.put("timestamp", Timestamp.now());

            firestore.collection("support_messages").add(message).get();
            
            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
