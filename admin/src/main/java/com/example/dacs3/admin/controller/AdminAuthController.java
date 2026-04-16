package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Autowired
    private Firestore firestore;

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("adminUser") != null) {
            return "redirect:/admin/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, 
                        @RequestParam String password, 
                        HttpSession session, 
                        Model model) throws ExecutionException, InterruptedException {
        
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .whereEqualTo("email", email)
                .whereEqualTo("role", "admin")
                .get();
        
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        if (!documents.isEmpty()) {
            QueryDocumentSnapshot userDoc = documents.get(0);
            String dbPassword = userDoc.getString("password");
            
            if (password.equals(dbPassword)) {
                Map<String, Object> userData = userDoc.getData();
                userData.put("docId", userDoc.getId());
                
                // Đảm bảo admin_level luôn có giá trị trong session
                if (!userData.containsKey("admin_level") || userData.get("admin_level") == null) {
                    userData.put("admin_level", 2); // Mặc định là cấp thấp nếu chưa có
                }

                session.setAttribute("adminUser", userData);
                session.setAttribute("adminEmail", email);
                return "redirect:/admin/dashboard";
            }
        }
        
        model.addAttribute("error", "Email hoặc mật khẩu không chính xác, hoặc bạn không có quyền truy cập!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}
