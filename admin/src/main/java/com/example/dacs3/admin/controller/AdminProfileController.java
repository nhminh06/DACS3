package com.example.dacs3.admin.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.cloud.firestore.Firestore;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin/profile")
public class AdminProfileController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping
    public String profilePage(HttpSession session, Model model) {
        Object adminUser = session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/admin/login";
        }
        
        model.addAttribute("admin", adminUser);
        return "profile";
    }

    @PostMapping("/update-info")
    @SuppressWarnings("unchecked")
    public String updateInfo(@RequestParam String name,
                             @RequestParam(value = "gioi_tinh", required = false) String gender,
                             @RequestParam(value = "ngay_sinh", required = false) String birthday,
                             @RequestParam(value = "dia_chi", required = false) String address,
                             @RequestParam(value = "sdt", required = false) String phone,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        try {
            Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
            if (adminUser == null) return "redirect:/admin/login";

            String email = (String) adminUser.get("email");
            String docId = (String) adminUser.getOrDefault("docId", email);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("gioi_tinh", gender);
            updates.put("ngay_sinh", birthday);
            updates.put("dia_chi", address);
            updates.put("sdt", phone);

            // Update Firestore
            firestore.collection("users").document(docId).update(updates).get();

            // Update Session
            adminUser.putAll(updates);
            session.setAttribute("adminUser", adminUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @PostMapping("/update-avatar")
    @SuppressWarnings("unchecked")
    public String updateAvatar(@RequestParam("avatar") MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ảnh!");
            return "redirect:/admin/profile";
        }

        try {
            Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
            if (adminUser == null) return "redirect:/admin/login";
            
            String email = (String) adminUser.get("email");
            String docId = (String) adminUser.getOrDefault("docId", email);

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "admin_avatars",
                    "public_id", "avatar_" + email.replace("@", "_").replace(".", "_")
            ));
            
            String avatarUrl = (String) uploadResult.get("secure_url");

            // Update Firestore
            firestore.collection("users").document(docId).update("avatar", avatarUrl).get();
            
            // Update Session
            adminUser.put("avatar", avatarUrl);
            session.setAttribute("adminUser", adminUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật ảnh đại diện thành công!");
        } catch (IOException | InterruptedException | ExecutionException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi upload: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }

    @PostMapping("/change-password")
    @SuppressWarnings("unchecked")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            Map<String, Object> adminUser = (Map<String, Object>) session.getAttribute("adminUser");
            if (adminUser == null) return "redirect:/admin/login";

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
                return "redirect:/admin/profile";
            }

            String email = (String) adminUser.get("email");
            String docId = (String) adminUser.getOrDefault("docId", email);
            String dbPassword = (String) adminUser.get("password");

            if (!currentPassword.equals(dbPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
                return "redirect:/admin/profile";
            }

            // Update Firestore
            firestore.collection("users").document(docId).update("password", newPassword).get();
            
            // Update Session
            adminUser.put("password", newPassword);
            session.setAttribute("adminUser", adminUser);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đổi mật khẩu: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }
}
