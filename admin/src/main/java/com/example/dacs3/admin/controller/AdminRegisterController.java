package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AdminRegisterController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private JavaMailSender mailSender;

    // Bộ nhớ tạm để lưu OTP đăng ký (trong thực tế nên dùng Redis hoặc DB có TTL)
    private Map<String, String> registrationOtps = new ConcurrentHashMap<>();

    @PostMapping("/send-registration-otp")
    public Map<String, Object> sendRegistrationOtp(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Kiểm tra email đã tồn tại chưa
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get();
            if (!future.get().getDocuments().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email này đã được đăng ký tài khoản");
                return response;
            }

            // Tạo mã OTP 6 số
            String otp = String.format("%06d", new Random().nextInt(1000000));
            registrationOtps.put(email, otp);

            // Gửi email
            sendOtpEmail(email, otp);

            response.put("success", true);
            response.put("message", "Mã xác thực đã được gửi đến email của bạn");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi gửi mail: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/verify-registration-otp")
    public Map<String, Object> verifyRegistrationOtp(@RequestParam String email, @RequestParam String otp) {
        Map<String, Object> response = new HashMap<>();
        String savedOtp = registrationOtps.get(email);
        if (savedOtp != null && savedOtp.equals(otp)) {
            // Xác thực thành công, xóa mã OTP khỏi bộ nhớ tạm
            registrationOtps.remove(email);
            response.put("success", true);
            response.put("message", "Xác thực email thành công");
        } else {
            response.put("success", false);
            response.put("message", "Mã xác thực không chính xác hoặc đã hết hạn");
        }
        return response;
    }

    private void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 24px;'>" +
                    "<h2 style='color: #2563eb; text-align: center;'>Xác thực đăng ký tài khoản</h2>" +
                    "<p>Chào bạn,</p>" +
                    "<p>Bạn đang thực hiện đăng ký tài khoản tại WIMD Travel. Mã xác thực (OTP) của bạn là:</p>" +
                    "<div style='background-color: #f1f5f9; padding: 16px; border-radius: 8px; text-align: center; margin: 24px 0;'>" +
                    "<span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #2563eb;'>" + otp + "</span>" +
                    "</div>" +
                    "<p>Mã này dùng để xác nhận địa chỉ email của bạn. Vui lòng không chia sẻ mã này cho bất kỳ ai.</p>" +
                    "<p>Trân trọng,<br>Đội ngũ WIMD Travel</p>" +
                    "</div>";

            helper.setText(htmlMsg, true);
            helper.setTo(to);
            helper.setSubject("[WIMD Travel] Mã xác thực đăng ký tài khoản");
            helper.setFrom("minhminh778894@gmail.com");

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
