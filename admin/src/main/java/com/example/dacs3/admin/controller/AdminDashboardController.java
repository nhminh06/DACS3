package com.example.dacs3.admin.controller;

import com.example.dacs3.admin.model.Booking;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private Firestore firestore;

    public static class ActivityLog {
        private String type; // BOOKING, USER, REVIEW, REPORT
        private String title;
        private String detail;
        private Date time;
        private String icon;
        private String color;

        public ActivityLog(String type, String title, String detail, Date time, String icon, String color) {
            this.type = type;
            this.title = title;
            this.detail = detail;
            this.time = time;
            this.icon = icon;
            this.color = color;
        }

        // Getters
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDetail() { return detail; }
        public Date getTime() { return time; }
        public String getIcon() { return icon; }
        public String getColor() { return color; }
    }

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        try {
            // Lấy dữ liệu từ các Collection chính
            ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users").get();
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            ApiFuture<QuerySnapshot> bookingsFuture = firestore.collection("bookings").get();
            ApiFuture<QuerySnapshot> reportsFuture = firestore.collection("reports").get();
            ApiFuture<QuerySnapshot> contactsFuture = firestore.collection("contacts").get();
            ApiFuture<QuerySnapshot> reviewsFuture = firestore.collection("reviews").get();
            ApiFuture<QuerySnapshot> articlesFuture = firestore.collection("articles").get();

            List<QueryDocumentSnapshot> userDocs = usersFuture.get().getDocuments();
            List<QueryDocumentSnapshot> tourDocs = toursFuture.get().getDocuments();
            List<QueryDocumentSnapshot> bookingDocs = bookingsFuture.get().getDocuments();
            List<QueryDocumentSnapshot> reportDocs = reportsFuture.get().getDocuments();
            List<QueryDocumentSnapshot> contactDocs = contactsFuture.get().getDocuments();
            List<QueryDocumentSnapshot> reviewDocs = reviewsFuture.get().getDocuments();
            List<QueryDocumentSnapshot> articleDocs = articlesFuture.get().getDocuments();
            
            // 1. KPI Chính
            long totalRevenue = 0;
            int pendingBookings = 0;
            int confirmedBookings = 0;
            int activeTours = 0;
            int startedTrips = 0;

            for (QueryDocumentSnapshot doc : bookingDocs) {
                String status = doc.getString("status");
                if ("CONFIRMED".equals(status)) {
                    confirmedBookings++;
                    Long price = doc.getLong("totalPrice");
                    if (price != null) totalRevenue += price;
                } else if ("PENDING".equals(status)) {
                    pendingBookings++;
                }
                
                if ("started".equals(doc.getString("tripStatus"))) {
                    startedTrips++;
                }
            }
            
            for (QueryDocumentSnapshot doc : tourDocs) {
                if ("active".equals(doc.getString("trang_thai")) || doc.get("trang_thai") == null) {
                    activeTours++;
                }
            }

            // 2. Nhật ký hoạt động tổng hợp (Activity Log)
            List<ActivityLog> activities = new ArrayList<>();
            
            // Lấy 3 đơn đặt mới nhất
            bookingDocs.stream()
                .sorted((d1, d2) -> compareDates(d1.getDate("createdAt"), d2.getDate("createdAt")))
                .limit(3)
                .forEach(d -> activities.add(new ActivityLog("BOOKING", "Đơn hàng mới", d.getString("customerName") + " vừa đặt tour", d.getDate("createdAt"), "bi-cart-plus", "text-primary")));

            // Lấy 2 user mới nhất
            userDocs.stream()
                .sorted((d1, d2) -> compareDates(d1.getDate("createdAt"), d2.getDate("createdAt")))
                .limit(2)
                .forEach(d -> activities.add(new ActivityLog("USER", "Thành viên mới", d.getString("name") + " vừa gia nhập", d.getDate("createdAt"), "bi-person-plus", "text-success")));

            // Lấy 2 đánh giá mới nhất
            reviewDocs.stream()
                .sorted((d1, d2) -> {
                    Object t1 = d1.get("createdAt");
                    Object t2 = d2.get("createdAt");
                    if (t1 instanceof Long && t2 instanceof Long) return ((Long) t2).compareTo((Long) t1);
                    return 0;
                })
                .limit(2)
                .forEach(d -> {
                    Date date = d.contains("createdAt") ? new Date(d.getLong("createdAt")) : new Date();
                    activities.add(new ActivityLog("REVIEW", "Đánh giá mới", d.getString("userName") + ": " + d.getString("comment"), date, "bi-star", "text-warning"));
                });

            activities.sort((a1, a2) -> compareDates(a1.getTime(), a2.getTime()));

            // 3. Trạng thái Hướng dẫn viên
            long totalGuides = userDocs.stream().filter(d -> "guide".equals(d.getString("role"))).count();
            Set<String> busyGuideIds = new HashSet<>();
            for (QueryDocumentSnapshot doc : bookingDocs) {
                if ("started".equals(doc.getString("tripStatus"))) {
                    List<String> ids = (List<String>) doc.get("guideIds");
                    if (ids != null) busyGuideIds.addAll(ids);
                }
            }

            // 4. Biểu đồ doanh thu 6 tháng
            Map<String, Long> monthlyRevenue = new LinkedHashMap<>();
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy");
            for (int i = 5; i >= 0; i--) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MONTH, -i);
                monthlyRevenue.put(monthFormat.format(c.getTime()), 0L);
            }
            for (QueryDocumentSnapshot doc : bookingDocs) {
                if ("CONFIRMED".equals(doc.getString("status"))) {
                    Date date = doc.getDate("createdAt");
                    if (date != null) {
                        String m = monthFormat.format(date);
                        if (monthlyRevenue.containsKey(m)) {
                            monthlyRevenue.put(m, monthlyRevenue.get(m) + doc.getLong("totalPrice"));
                        }
                    }
                }
            }

            // 5. Top 4 Tour Thịnh hành (Popularity dựa trên lượt đặt)
            Map<String, Integer> tourCount = new HashMap<>();
            for (QueryDocumentSnapshot doc : bookingDocs) {
                if ("CONFIRMED".equals(doc.getString("status"))) {
                    String tid = doc.getString("tourId");
                    if (tid != null) tourCount.put(tid, tourCount.getOrDefault(tid, 0) + 1);
                }
            }
            int maxOrders = tourCount.values().stream().max(Integer::compare).orElse(1);
            List<Map<String, Object>> topTours = tourCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(4)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    tourDocs.stream().filter(d -> d.getId().equals(e.getKey())).findFirst().ifPresent(d -> {
                        m.put("title", d.get("title"));
                        m.put("image", d.get("imageUrl"));
                        m.put("count", e.getValue());
                        m.put("percent", (e.getValue() * 100) / maxOrders);
                    });
                    return m;
                }).filter(m -> !m.isEmpty()).collect(Collectors.toList());

            // 6. Giao dịch gần đây
            List<Booking> recentBookings = bookingDocs.stream()
                .sorted((d1, d2) -> compareDates(d1.getDate("createdAt"), d2.getDate("createdAt")))
                .limit(5)
                .map(d -> {
                    Booking b = d.toObject(Booking.class);
                    b.setId(d.getId());
                    return b;
                }).collect(Collectors.toList());

            // 7. Lấy 3 đánh giá gần nhất cho dashboard
            List<Map<String, Object>> recentReviews = reviewDocs.stream()
                .sorted((d1, d2) -> {
                    Object t1 = d1.get("createdAt");
                    Object t2 = d2.get("createdAt");
                    if (t1 instanceof Long && t2 instanceof Long) return ((Long) t2).compareTo((Long) t1);
                    return 0;
                })
                .limit(3)
                .map(d -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("userName", d.getString("userName"));
                    m.put("rating", d.get("rating") != null ? d.getLong("rating") : 5L);
                    m.put("comment", d.getString("comment"));
                    return m;
                }).collect(Collectors.toList());

            // Gửi dữ liệu ra View
            model.addAttribute("totalUsers", userDocs.size());
            model.addAttribute("totalTours", tourDocs.size());
            model.addAttribute("totalArticles", articleDocs.size());
            model.addAttribute("activeTours", activeTours);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("totalBookings", bookingDocs.size());
            model.addAttribute("totalReviews", reviewDocs.size());
            
            model.addAttribute("totalGuides", totalGuides);
            model.addAttribute("busyGuides", busyGuideIds.size());
            model.addAttribute("freeGuides", totalGuides - busyGuideIds.size());
            
            model.addAttribute("confirmedBookings", confirmedBookings);
            model.addAttribute("startedTrips", startedTrips);
            model.addAttribute("pendingBookings", pendingBookings);
            model.addAttribute("pendingReports", reportDocs.stream().filter(d -> !"resolved".equals(d.getString("status"))).count());
            model.addAttribute("pendingContacts", contactDocs.stream().filter(d -> !"processed".equals(d.getString("status"))).count());
            
            model.addAttribute("chartLabels", new ArrayList<>(monthlyRevenue.keySet()));
            model.addAttribute("chartData", new ArrayList<>(monthlyRevenue.values()));
            model.addAttribute("recentBookings", recentBookings);
            model.addAttribute("recentReviews", recentReviews);
            model.addAttribute("topTours", topTours);
            model.addAttribute("activities", activities);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "dashboard";
    }

    private int compareDates(Date d1, Date d2) {
        if (d1 == null || d2 == null) return 0;
        return d2.compareTo(d1);
    }
}
