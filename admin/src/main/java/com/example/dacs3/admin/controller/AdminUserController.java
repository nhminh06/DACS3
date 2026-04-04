package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                            @RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "1") int page) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            long totalAll = documents.size();
            long adminCount = documents.stream().filter(d -> "admin".equals(d.getString("role"))).count();
            long guideCount = documents.stream().filter(d -> "guide".equals(d.getString("role"))).count();
            long activeCount = documents.stream().filter(d -> "active".equals(d.getString("trang_thai"))).count();

            model.addAttribute("total", totalAll);
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("guideCount", guideCount);
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

            // Pagination logic
            int pageSize = 10;
            int totalFiltered = filteredUsers.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            
            // Ensure page is within valid range
            if (page < 1) page = 1;
            if (totalPages > 0 && page > totalPages) page = totalPages;

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);

            List<QueryDocumentSnapshot> pagedUsers = List.of();
            if (start < totalFiltered) {
                pagedUsers = filteredUsers.subList(start, end);
            }

            model.addAttribute("users", pagedUsers);
            model.addAttribute("search", search);
            model.addAttribute("roleFilter", role);
            model.addAttribute("statusFilter", status);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalFiltered", totalFiltered);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "users/list";
    }

    @GetMapping("/add")
    public String showAddUserForm() {
        return "users/add";
    }

    @PostMapping("/add")
    public String processAddUser(@RequestParam String ho_ten,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String sdt,
                                 @RequestParam String password,
                                 @RequestParam String role,
                                 @RequestParam(required = false) String gioi_tinh,
                                 @RequestParam(required = false) String ngay_sinh,
                                 @RequestParam(required = false) String dia_chi,
                                 @RequestParam(required = false, defaultValue = "blocked") String trang_thai) throws ExecutionException, InterruptedException {
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", ho_ten);
        userData.put("email", email);
        userData.put("sdt", sdt);
        userData.put("password", password);
        userData.put("role", role);
        userData.put("gioi_tinh", gioi_tinh);
        userData.put("ngay_sinh", ngay_sinh);
        userData.put("dia_chi", dia_chi);
        userData.put("trang_thai", trang_thai.equals("on") ? "active" : "blocked");

        firestore.collection("users").add(userData).get();
        return "redirect:/admin/users";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection("users").document(id);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            String currentStatus = snapshot.getString("trang_thai");
            String newStatus = "active".equals(currentStatus) ? "blocked" : "active";
            docRef.update("trang_thai", newStatus).get();
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/change-role")
    public String changeRole(@RequestParam String id, @RequestParam String role) throws ExecutionException, InterruptedException {
        firestore.collection("users").document(id).update("role", role).get();
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("users").document(id).delete().get();
        return "redirect:/admin/users";
    }
}
