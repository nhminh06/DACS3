package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private Firestore firestore;

    @GetMapping
    public String listUsers(Model model, 
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            long total = documents.size();
            long adminCount = documents.stream().filter(d -> "admin".equals(d.getString("role"))).count();
            long staffCount = documents.stream().filter(d -> "staff".equals(d.getString("role"))).count();
            long activeCount = documents.stream().filter(d -> "active".equals(d.getString("trang_thai"))).count();

            model.addAttribute("total", total);
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("staffCount", staffCount);
            model.addAttribute("activeCount", activeCount);

            List<QueryDocumentSnapshot> filteredUsers = documents;
            
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredUsers = filteredUsers.stream()
                        .filter(d -> (d.getString("name") != null && d.getString("name").toLowerCase().contains(searchLower)) ||
                                     (d.getString("email") != null && d.getString("email").toLowerCase().contains(searchLower)) ||
                                     (d.getString("sdt") != null && d.getString("sdt").contains(searchLower)))
                        .collect(Collectors.toList());
            }
            
            if (role != null && !role.isEmpty()) {
                filteredUsers = filteredUsers.stream()
                        .filter(d -> role.equals(d.getString("role")))
                        .collect(Collectors.toList());
            }
            
            if (status != null && !status.isEmpty()) {
                filteredUsers = filteredUsers.stream()
                        .filter(d -> status.equals(d.getString("trang_thai")))
                        .collect(Collectors.toList());
            }

            model.addAttribute("users", filteredUsers);
            model.addAttribute("search", search);
            model.addAttribute("roleFilter", role);
            model.addAttribute("statusFilter", status);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "users/list";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection("users").document(id);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            String currentStatus = snapshot.getString("trang_thai");
            String newStatus = "active".equals(currentStatus) ? "blocked" : "active";
            // Đợi Firestore cập nhật xong mới Redirect
            docRef.update("trang_thai", newStatus).get();
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/change-role")
    public String changeRole(@RequestParam String id, @RequestParam String role) throws ExecutionException, InterruptedException {
        // Đợi Firestore cập nhật xong mới Redirect
        firestore.collection("users").document(id).update("role", role).get();
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) throws ExecutionException, InterruptedException {
        // Đợi Firestore xóa xong mới Redirect
        firestore.collection("users").document(id).delete().get();
        return "redirect:/admin/users";
    }
}
