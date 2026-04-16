package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    private int getAdminLevel(HttpSession session) {
        Map<String, Object> admin = (Map<String, Object>) session.getAttribute("adminUser");
        if (admin == null) return 2;
        Object levelObj = admin.get("admin_level");
        if (levelObj instanceof Long) return ((Long) levelObj).intValue();
        if (levelObj instanceof Integer) return (Integer) levelObj;
        return 2;
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public String listUsers(Model model, 
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String role,
                            @RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "1") int page,
                            HttpSession session) {
        
        if (session.getAttribute("adminUser") == null) return "redirect:/admin/login";

        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            model.addAttribute("total", documents.size());
            model.addAttribute("adminCount", documents.stream().filter(d -> "admin".equals(d.getString("role"))).count());
            model.addAttribute("guideCount", documents.stream().filter(d -> "guide".equals(d.getString("role"))).count());
            model.addAttribute("activeCount", documents.stream().filter(d -> "active".equals(d.getString("trang_thai"))).count());
            
            model.addAttribute("currentAdminLevel", getAdminLevel(session));

            List<QueryDocumentSnapshot> filteredUsers = documents;
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredUsers = filteredUsers.stream()
                        .filter(d -> (d.getString("name") != null && d.getString("name").toLowerCase().contains(searchLower)) ||
                                     (d.getString("email") != null && d.getString("email").toLowerCase().contains(searchLower)) ||
                                     (d.getString("sdt") != null && d.getString("sdt").contains(searchLower)))
                        .collect(Collectors.toList());
            }
            if (role != null && !role.isEmpty()) filteredUsers = filteredUsers.stream().filter(d -> role.equals(d.getString("role"))).collect(Collectors.toList());
            if (status != null && !status.isEmpty()) filteredUsers = filteredUsers.stream().filter(d -> status.equals(d.getString("trang_thai"))).collect(Collectors.toList());

            int pageSize = 10;
            int totalFiltered = filteredUsers.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            if (page < 1) page = 1;
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);
            List<QueryDocumentSnapshot> pagedUsers = start < totalFiltered ? filteredUsers.subList(start, end) : List.of();

            model.addAttribute("users", pagedUsers);
            model.addAttribute("search", search);
            model.addAttribute("roleFilter", role);
            model.addAttribute("statusFilter", status);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalFiltered", totalFiltered);

        } catch (InterruptedException | ExecutionException e) { e.printStackTrace(); }
        return "users/list";
    }

    @GetMapping("/add")
    public String addUserPage(HttpSession session) {
        if (session.getAttribute("adminUser") == null) return "redirect:/admin/login";
        return "users/add";
    }

    @PostMapping("/add")
    public String addUser(@RequestParam String ho_ten,
                          @RequestParam String email,
                          @RequestParam(required = false) String sdt,
                          @RequestParam String password,
                          @RequestParam String role,
                          @RequestParam(required = false) String gioi_tinh,
                          @RequestParam(required = false) String ngay_sinh,
                          @RequestParam(required = false) String dia_chi,
                          @RequestParam(defaultValue = "off") String trang_thai,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        if (session.getAttribute("adminUser") == null) return "redirect:/admin/login";

        // Validate role selection if needed or admin level check
        int currentLevel = getAdminLevel(session);
        if ("admin".equals(role) && currentLevel != 1) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: Chỉ Admin Cấp Cao mới có thể tạo tài khoản Admin!");
            return "redirect:/admin/users/add";
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("name", ho_ten);
        userData.put("email", email);
        userData.put("sdt", sdt);
        userData.put("password", password);
        userData.put("role", role);
        userData.put("gioi_tinh", gioi_tinh);
        userData.put("ngay_sinh", ngay_sinh);
        userData.put("dia_chi", dia_chi);
        userData.put("trang_thai", "on".equals(trang_thai) ? "active" : "blocked");
        
        // Mặc định admin_level là 2 (thấp) nếu là admin
        if ("admin".equals(role)) {
            userData.put("admin_level", 2);
        }

        firestore.collection("users").add(userData).get();
        redirectAttributes.addFlashAttribute("success", "Thêm người dùng thành công!");
        return "redirect:/admin/users";
    }

    @PostMapping("/change-role")
    public String changeRole(@RequestParam String id, 
                             @RequestParam String role, 
                             HttpSession session,
                             RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        
        int currentLevel = getAdminLevel(session);

        // Chặn nếu admin cấp thấp cố tình set quyền admin
        if ("admin".equals(role) && currentLevel != 1) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: Chỉ Admin Cấp Cao mới có thể chỉ định người khác làm Admin!");
            return "redirect:/admin/users";
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("role", role);
        if ("admin".equals(role)) {
            updates.put("admin_level", 2); 
        } else {
            updates.put("admin_level", null);
        }

        firestore.collection("users").document(id).update(updates).get();
        redirectAttributes.addFlashAttribute("success", "Cập nhật vai trò thành công!");
        return "redirect:/admin/users";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection("users").document(id);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            String currentStatus = snapshot.getString("trang_thai");
            docRef.update("trang_thai", "active".equals(currentStatus) ? "blocked" : "active").get();
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable String id, RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {
        firestore.collection("users").document(id).delete().get();
        redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng.");
        return "redirect:/admin/users";
    }
}
