package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AdminForgotPasswordController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private JavaMailSender mailSender;

    // Bước 1: Gửi mã OTP
    @PostMapping("/send-otp")
    public Map<String, Object> sendOtp(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không tồn tại trong hệ thống");
                return response;
            }

            String docId = documents.get(0).getId();
            String name = documents.get(0).getString("name");
            
            // Tạo mã OTP 6 số
            String otp = String.format("%06d", new Random().nextInt(1000000));

            // Lưu OTP vào Firestore (có thể thêm thời gian hết hạn)
            Map<String, Object> otpData = new HashMap<>();
            otpData.put("reset_otp", otp);
            otpData.put("otp_timestamp", Timestamp.now());
            firestore.collection("users").document(docId).update(otpData).get();

            // Gửi email chứa mã OTP
            sendOtpEmail(email, name, otp);

            response.put("success", true);
            response.put("message", "Mã xác thực đã được gửi về email của bạn");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    // Bước 2: Xác nhận OTP và đổi mật khẩu
    @PostMapping("/reset-password-with-otp")
    public Map<String, Object> resetPassword(@RequestParam String email, 
                                            @RequestParam String otp, 
                                            @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (documents.isEmpty()) {
                response.put("success", false);
                response.put("message", "Email không hợp lệ");
                return response;
            }

            QueryDocumentSnapshot doc = documents.get(0);
            String savedOtp = doc.getString("reset_otp");

            if (savedOtp != null && savedOtp.equals(otp)) {
                // OTP đúng -> Cập nhật mật khẩu mới và xóa OTP
                Map<String, Object> updates = new HashMap<>();
                updates.put("password", newPassword);
                updates.put("reset_otp", null); // Xóa mã sau khi dùng
                firestore.collection("users").document(doc.getId()).update(updates).get();

                response.put("success", true);
                response.put("message", "Đổi mật khẩu thành công");
            } else {
                response.put("success", false);
                response.put("message", "Mã xác thực không chính xác");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    private void sendOtpEmail(String to, String name, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px;'>" +
                    "<h2 style='color: #2563eb; text-align: center;'>Mã xác thực khôi phục mật khẩu</h2>" +
                    "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                    "<p>Bạn đã yêu cầu khôi phục mật khẩu cho tài khoản WIMD Travel. Mã xác thực (OTP) của bạn là:</p>" +
                    "<div style='background-color: #f1f5f9; padding: 16px; border-radius: 8px; text-align: center; margin: 24px 0;'>" +
                    "<span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #2563eb;'>" + otp + "</span>" +
                    "</div>" +
                    "<p>Mã này có hiệu lực trong vòng 10 phút. Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>" +
                    "<p>Trân trọng,<br>Đội ngũ WIMD Travel</p>" +
                    "</div>";

            helper.setText(htmlMsg, true);
            helper.setTo(to);
            helper.setSubject("[WIMD Travel] Mã xác thực khôi phục mật khẩu");
            helper.setFrom("minhminh778894@gmail.com");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
