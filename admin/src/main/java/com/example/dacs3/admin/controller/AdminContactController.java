package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/contacts")
public class AdminContactController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private JavaMailSender mailSender;

    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    @GetMapping
    public String listContacts(Model model,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String status) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("contacts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get();
            
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            // Statistics
            long total = documents.size();
            long gopY = documents.stream().filter(d -> "Góp ý".equals(d.getString("type"))).count();
            long khieuNai = documents.stream().filter(d -> "Khiếu nại".equals(d.getString("type"))).count();
            long pending = documents.stream().filter(d -> !"processed".equals(d.getString("status"))).count();

            model.addAttribute("total", total);
            model.addAttribute("gopYCount", gopY);
            model.addAttribute("khieuNaiCount", khieuNai);
            model.addAttribute("pendingCount", pending);

            List<Map<String, Object>> contactList = documents.stream().map(d -> {
                Map<String, Object> map = new HashMap<>(d.getData());
                map.put("id", d.getId());
                
                String userId = d.getString("userId");
                if (userId != null && !userId.isEmpty()) {
                    try {
                        DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
                        if (userDoc.exists()) {
                            map.put("userPhone", userDoc.getString("sdt"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                map.put("formattedDate", formatTimestamp(d.get("timestamp")));
                if (d.contains("replyAt")) {
                    map.put("formattedReplyDate", formatTimestamp(d.get("replyAt")));
                }

                return map;
            }).collect(Collectors.toList());

            if (type != null && !type.isEmpty()) {
                contactList = contactList.stream().filter(m -> type.equals(m.get("type"))).collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                contactList = contactList.stream().filter(m -> status.equals(m.get("status"))).collect(Collectors.toList());
            }

            model.addAttribute("contacts", contactList);
            model.addAttribute("selectedType", type);
            model.addAttribute("selectedStatus", status);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "contacts/list";
    }

    private String formatTimestamp(Object ts) {
        if (ts == null) return "N/A";
        if (ts instanceof Timestamp) return sdf.format(((Timestamp) ts).toDate());
        if (ts instanceof com.google.cloud.Timestamp) return sdf.format(((com.google.cloud.Timestamp) ts).toDate());
        if (ts instanceof Date) return sdf.format((Date) ts);
        return "N/A";
    }

    @PostMapping("/reply/{id}")
    @ResponseBody
    public Map<String, Object> replyContact(@PathVariable String id, @RequestParam String replyMessage) {
        Map<String, Object> result = new HashMap<>();
        try {
            DocumentSnapshot contactDoc = firestore.collection("contacts").document(id).get().get();
            if (!contactDoc.exists()) {
                result.put("success", false);
                result.put("message", "Liên hệ không tồn tại");
                return result;
            }
            
            String userEmail = contactDoc.getString("email");
            String userName = contactDoc.getString("name");
            String originalContent = contactDoc.getString("content");
            String contactType = contactDoc.getString("type");

            Map<String, Object> updates = new HashMap<>();
            updates.put("reply", replyMessage);
            updates.put("status", "processed");
            updates.put("replyAt", Timestamp.now());
            firestore.collection("contacts").document(id).update(updates).get();

            if (userEmail != null && !userEmail.isEmpty()) {
                sendHtmlEmail(userEmail, userName, contactType, originalContent, replyMessage);
            }

            result.put("success", true);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/send-guide-notification")
    @ResponseBody
    public Map<String, Object> sendGuideNotification(@RequestParam String title, @RequestParam String message) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Lấy danh sách tất cả hướng dẫn viên
            ApiFuture<QuerySnapshot> future = firestore.collection("users").whereEqualTo("role", "guide").get();
            List<QueryDocumentSnapshot> guides = future.get().getDocuments();

            for (QueryDocumentSnapshot guide : guides) {
                Map<String, Object> notification = new HashMap<>();
                notification.put("userId", guide.getId());
                notification.put("title", title);
                notification.put("message", message);
                notification.put("timestamp", Timestamp.now());
                notification.put("isRead", false);
                notification.put("type", "SYSTEM_ANNOUNCEMENT");
                firestore.collection("notifications").add(notification);
            }

            result.put("success", true);
            result.put("message", "Đã gửi thông báo tới " + guides.size() + " hướng dẫn viên");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    private void sendHtmlEmail(String to, String name, String type, String content, String reply) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;'>" +
                    "<div style='background-color: #2563eb; padding: 20px; text-align: center; color: white;'>" +
                    "<h2>WIMD Travel - Phản hồi hỗ trợ</h2>" +
                    "</div>" +
                    "<div style='padding: 24px; color: #1e293b;'>" +
                    "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                    "<p>Chúng tôi đã xử lý yêu cầu <strong>" + type + "</strong> của bạn gửi đến <strong>WIMD Travel</strong>.</p>" +
                    "<div style='background-color: #f8fafc; padding: 16px; border-radius: 8px; margin: 16px 0;'>" +
                    "<p style='margin-top: 0; font-weight: bold; color: #64748b;'>Nội dung yêu cầu của bạn:</p>" +
                    "<p style='font-style: italic;'>" + content + "</p>" +
                    "</div>" +
                    "<div style='background-color: #f0fdf4; padding: 16px; border-radius: 8px; border-left: 4px solid #10b981;'>" +
                    "<p style='margin-top: 0; font-weight: bold; color: #10b981;'>Phản hồi từ WIMD Travel:</p>" +
                    "<p>" + reply + "</p>" +
                    "</div>" +
                    "<p style='margin-top: 24px;'>Cảm ơn bạn đã tin tưởng sử dụng dịch vụ của WIMD Travel!</p>" +
                    "<p>Trân trọng,<br><strong>Đội ngũ hỗ trợ WIMD Travel</strong></p>" +
                    "</div>" +
                    "<div style='background-color: #f1f5f9; padding: 12px; text-align: center; font-size: 12px; color: #94a3b8;'>" +
                    "Đây là email tự động từ hệ thống WIMD Travel, vui lòng không phản hồi email này." +
                    "</div>" +
                    "</div>";

            helper.setText(htmlMsg, true);
            helper.setTo(to);
            helper.setSubject("[WIMD Travel] Phản hồi yêu cầu " + type);
            helper.setFrom("minhminh778894@gmail.com");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email HTML: " + e.getMessage());
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteContact(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("contacts").document(id).delete().get();
        return "redirect:/admin/contacts";
    }
}
