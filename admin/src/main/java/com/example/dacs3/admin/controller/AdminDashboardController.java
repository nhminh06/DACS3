package com.example.dacs3.admin.controller;

import com.example.dacs3.admin.model.Booking;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private Firestore firestore;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            // 1. Lấy tất cả dữ liệu để thống kê
            ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users").get();
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            ApiFuture<QuerySnapshot> bookingsFuture = firestore.collection("bookings").get();
            ApiFuture<QuerySnapshot> articlesFuture = firestore.collection("articles").get();
            ApiFuture<QuerySnapshot> reviewsFuture = firestore.collection("reviews").get();

            List<QueryDocumentSnapshot> userDocs = usersFuture.get().getDocuments();
            List<QueryDocumentSnapshot> bookingDocs = bookingsFuture.get().getDocuments();
            
            // Lọc số lượng khách hàng (role = user)
            long totalUsers = userDocs.stream()
                    .filter(doc -> "user".equals(doc.getString("role")))
                    .count();
            
            long totalRevenue = 0;
            int confirmedBookings = 0;
            int pendingBookings = 0;
            int startedTrips = 0;

            for (QueryDocumentSnapshot doc : bookingDocs) {
                String status = doc.getString("status");
                String tripStatus = doc.getString("tripStatus");
                Long price = doc.getLong("totalPrice");

                // Thống kê đơn xác nhận và doanh thu
                if ("CONFIRMED".equals(status)) {
                    confirmedBookings++;
                    if (price != null) totalRevenue += price;
                } else if ("PENDING".equals(status)) {
                    pendingBookings++;
                }

                // Thống kê chuyến đang chạy
                if ("started".equals(tripStatus)) {
                    startedTrips++;
                }
            }

            // 2. Lấy 5 đơn đặt gần nhất (sắp xếp theo thời gian)
            // Lưu ý: Nếu không có field createdAt hoặc chưa index, có thể gây lỗi. 
            // Ở đây tôi dùng doc.toObject an toàn hơn.
            List<Booking> recentBookings = new ArrayList<>();
            ApiFuture<QuerySnapshot> recentFuture = firestore.collection("bookings")
                    .limit(10) // Lấy nhiều hơn một chút để lọc tay nếu cần
                    .get();
            
            List<QueryDocumentSnapshot> recentDocs = recentFuture.get().getDocuments();
            for (QueryDocumentSnapshot doc : recentDocs) {
                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());
                recentBookings.add(b);
                if (recentBookings.size() >= 5) break;
            }

            // 3. Lấy 4 đánh giá mới nhất
            List<Map<String, Object>> recentReviews = new ArrayList<>();
            ApiFuture<QuerySnapshot> recentReviewsFuture = firestore.collection("reviews")
                    .limit(4)
                    .get();
            
            for (QueryDocumentSnapshot doc : recentReviewsFuture.get().getDocuments()) {
                recentReviews.add(doc.getData());
            }

            // Gửi dữ liệu ra view
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalTours", toursFuture.get().size());
            model.addAttribute("totalBookings", bookingDocs.size());
            model.addAttribute("totalArticles", articlesFuture.get().size());
            model.addAttribute("totalReviews", reviewsFuture.get().size());
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("startedTrips", startedTrips);
            model.addAttribute("recentBookings", recentBookings);
            model.addAttribute("recentReviews", recentReviews);

        } catch (InterruptedException | ExecutionException e) {
            System.err.println("Dashboard Error: " + e.getMessage());
            // Trả về giá trị mặc định nếu lỗi để giao diện không bị trắng
            model.addAttribute("totalUsers", 0);
            model.addAttribute("totalTours", 0);
            model.addAttribute("totalBookings", 0);
            model.addAttribute("totalRevenue", 0);
        }

        return "dashboard";
    }
}
