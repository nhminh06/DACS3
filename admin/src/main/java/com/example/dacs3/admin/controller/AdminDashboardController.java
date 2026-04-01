package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private Firestore firestore;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        int totalUsers = 0;
        int totalTours = 0;
        int totalBookings = 0;

        try {
            // Thống kê sơ bộ với xử lý lỗi
            ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users").get();
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            ApiFuture<QuerySnapshot> bookingsFuture = firestore.collection("bookings").get();

            totalUsers = usersFuture.get().size();
            totalTours = toursFuture.get().size();
            totalBookings = bookingsFuture.get().size();
            
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Error fetching dashboard stats: " + e.getMessage());
            // Không ném exception để tránh lỗi 500, giữ giá trị mặc định là 0
        }

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTours", totalTours);
        model.addAttribute("totalBookings", totalBookings);

        return "dashboard";
    }
}
