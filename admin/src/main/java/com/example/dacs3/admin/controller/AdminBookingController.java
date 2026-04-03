package com.example.dacs3.admin.controller;

import com.example.dacs3.admin.model.Booking;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    @Autowired
    private Firestore firestore;

    public static class GroupedTrip {
        private String id;
        private String tourId;
        private String startDate;
        private String tripStatus;
        private Map<String, Object> tour;
        private List<Booking> bookings = new ArrayList<>();
        private long totalRevenue = 0;
        private int totalPassengers = 0;
        private int bookingCount = 0;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTourId() { return tourId; }
        public void setTourId(String tourId) { this.tourId = tourId; }
        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }
        public String getTripStatus() { return tripStatus; }
        public void setTripStatus(String tripStatus) { this.tripStatus = tripStatus; }
        public Map<String, Object> getTour() { return tour; }
        public void setTour(Map<String, Object> tour) { this.tour = tour; }
        public List<Booking> getBookings() { return bookings; }
        public void setBookings(List<Booking> bookings) { this.bookings = bookings; }
        public long getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(long totalRevenue) { this.totalRevenue = totalRevenue; }
        public int getTotalPassengers() { return totalPassengers; }
        public void setTotalPassengers(int totalPassengers) { this.totalPassengers = totalPassengers; }
        public int getBookingCount() { return bookingCount; }
        public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public String listBookings(Model model, @RequestParam(required = false) String status) {
        try {
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            List<QueryDocumentSnapshot> tourDocs = toursFuture.get().getDocuments();
            Map<String, Map<String, Object>> toursMap = new HashMap<>();
            for (QueryDocumentSnapshot doc : tourDocs) {
                toursMap.put(doc.getId(), doc.getData());
            }

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Booking> bookings = new ArrayList<>();
            long totalRevenue = 0;
            int confirmedCount = 0;
            int pendingCount = 0;
            int unpaidCount = 0;

            for (QueryDocumentSnapshot document : documents) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(document.getId());

                Object createdAtObj = document.get("createdAt");
                if (createdAtObj instanceof com.google.cloud.Timestamp) {
                    booking.setCreatedAt(((com.google.cloud.Timestamp) createdAtObj).toDate());
                } else if (createdAtObj instanceof Long) {
                    booking.setCreatedAt(new java.util.Date((Long) createdAtObj));
                }
                
                if (document.contains("tour") && document.get("tour") instanceof Map) {
                    booking.setTour((Map<String, Object>) document.get("tour"));
                } else if (booking.getTourId() != null) {
                    booking.setTour(toursMap.get(booking.getTourId()));
                }

                String currentStatus = booking.getStatus();
                if ("CONFIRMED".equals(currentStatus)) {
                    confirmedCount++;
                    totalRevenue += booking.getTotalPrice();
                } else if ("PENDING".equals(currentStatus)) {
                    pendingCount++;
                    if ("QR".equals(booking.getPaymentMethod()) && !"da_thanh_toan".equals(booking.getPaymentStatus())) {
                        unpaidCount++;
                    }
                }

                if (status != null && !status.isEmpty() && !status.equals(currentStatus)) {
                    continue;
                }
                bookings.add(booking);
            }

            model.addAttribute("bookings", bookings);
            model.addAttribute("totalBookingsCount", documents.size());
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("unpaidCount", unpaidCount);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("currentStatus", status);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "bookings/list";
    }

    @GetMapping("/schedule")
    @SuppressWarnings("unchecked")
    public String viewSchedule(Model model) {
        try {
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            Map<String, Map<String, Object>> toursMap = new HashMap<>();
            for (QueryDocumentSnapshot doc : toursFuture.get().getDocuments()) {
                toursMap.put(doc.getId(), doc.getData());
            }

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings")
                    .whereEqualTo("status", "CONFIRMED")
                    .get();
            
            Map<String, GroupedTrip> groupedMap = new LinkedHashMap<>();
            long overallRevenue = 0;

            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());
                
                if (doc.contains("tour") && doc.get("tour") instanceof Map) {
                    Map<String, Object> tourData = (Map<String, Object>) doc.get("tour");
                    b.setTour(tourData);
                    if (b.getTourId() == null && tourData.containsKey("id")) {
                        b.setTourId(String.valueOf(tourData.get("id")));
                    }
                } else if (b.getTourId() != null) {
                    b.setTour(toursMap.get(b.getTourId()));
                }

                String key = b.getTourId() + "_" + b.getStartDate() + "_" + b.getTripStatus();
                GroupedTrip trip = groupedMap.computeIfAbsent(key, k -> {
                    GroupedTrip gt = new GroupedTrip();
                    gt.setId(b.getId());
                    gt.setTourId(b.getTourId());
                    gt.setStartDate(b.getStartDate());
                    gt.setTripStatus(b.getTripStatus());
                    gt.setTour(b.getTour());
                    return gt;
                });
                
                trip.getBookings().add(b);
                trip.setBookingCount(trip.getBookingCount() + 1);
                trip.setTotalRevenue(trip.getTotalRevenue() + b.getTotalPrice());
                int p = (b.getAdults() != null ? b.getAdults().intValue() : 0)
                        + (b.getChildren() != null ? b.getChildren().intValue() : 0)
                        + (b.getInfants() != null ? b.getInfants().intValue() : 0);
                trip.setTotalPassengers(trip.getTotalPassengers() + p);
                
                overallRevenue += b.getTotalPrice();
            }
            
            model.addAttribute("trips", new ArrayList<>(groupedMap.values()));
            model.addAttribute("totalTrips", groupedMap.size());
            model.addAttribute("totalRevenue", overallRevenue);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "bookings/schedule";
    }

    @GetMapping("/trip-schedule")
    public String tripScheduleRedirect() {
        return "redirect:/admin/bookings/schedule";
    }

    @GetMapping("/trip-schedule/{id}")
    @SuppressWarnings("unchecked")
    public String viewTripSchedule(@PathVariable String id, Model model) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(id);

                if (document.contains("tour") && document.get("tour") instanceof Map) {
                    booking.setTour((Map<String, Object>) document.get("tour"));
                } else if (booking.getTourId() != null) {
                    Map<String, Object> tourData = firestore.collection("tours").document(booking.getTourId()).get().get().getData();
                    booking.setTour(tourData);
                }

                if (booking.getGuideId() != null) {
                    DocumentSnapshot guideDoc = firestore.collection("users").document(booking.getGuideId()).get().get();
                    if (guideDoc.exists()) {
                        booking.setGuide(guideDoc.getData());
                    } else {
                        // Kiểm tra hướng dẫn viên tĩnh
                        if ("guide1".equals(booking.getGuideId())) {
                            Map<String, Object> g = new HashMap<>();
                            g.put("name", "Nguyễn Văn Nam"); g.put("sdt", "0912345678"); g.put("email", "nam@wind.com");
                            booking.setGuide(g);
                        } else if ("guide2".equals(booking.getGuideId())) {
                            Map<String, Object> g = new HashMap<>();
                            g.put("name", "Trần Thị Lan"); g.put("sdt", "0987654321"); g.put("email", "lan@wind.com");
                            booking.setGuide(g);
                        } else if ("guide3".equals(booking.getGuideId())) {
                            Map<String, Object> g = new HashMap<>();
                            g.put("name", "Lê Hoàng Minh"); g.put("sdt", "0905112233"); g.put("email", "minh@wind.com");
                            booking.setGuide(g);
                        }
                    }
                }

                ApiFuture<QuerySnapshot> groupFuture = firestore.collection("bookings")
                        .whereEqualTo("tourId", booking.getTourId())
                        .whereEqualTo("startDate", booking.getStartDate())
                        .whereEqualTo("status", "CONFIRMED")
                        .get();
                
                List<Booking> bookings = new ArrayList<>();
                int totalPassengers = 0;
                for (QueryDocumentSnapshot doc : groupFuture.get().getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    if (booking.getTripStatus().equals(b.getTripStatus())) {
                        b.setId(doc.getId());
                        bookings.add(b);
                        int p = (b.getAdults() != null ? b.getAdults().intValue() : 0)
                                + (b.getChildren() != null ? b.getChildren().intValue() : 0)
                                + (b.getInfants() != null ? b.getInfants().intValue() : 0);
                        totalPassengers += p;
                    }
                }

                List<Map<String, Object>> guides = new ArrayList<>();
                // 3 hướng dẫn viên tĩnh
                Map<String, Object> g1 = new HashMap<>();
                g1.put("id", "guide1"); g1.put("name", "Nguyễn Văn Nam (Nội địa)"); g1.put("email", "nam@wind.com");
                guides.add(g1);
                Map<String, Object> g2 = new HashMap<>();
                g2.put("id", "guide2"); g2.put("name", "Trần Thị Lan (Quốc tế)"); g2.put("email", "lan@wind.com");
                guides.add(g2);
                Map<String, Object> g3 = new HashMap<>();
                g3.put("id", "guide3"); g3.put("name", "Lê Hoàng Minh (Chuyên tuyến)"); g3.put("email", "minh@wind.com");
                guides.add(g3);

                try {
                    ApiFuture<QuerySnapshot> guidesFuture = firestore.collection("users")
                            .whereEqualTo("role", "staff")
                            .get();
                    for (QueryDocumentSnapshot doc : guidesFuture.get().getDocuments()) {
                        Map<String, Object> g = doc.getData();
                        g.put("id", doc.getId());
                        guides.add(g);
                    }
                } catch (Exception e) { }
                
                model.addAttribute("booking", booking);
                model.addAttribute("bookings", bookings);
                model.addAttribute("totalPassengers", totalPassengers);
                model.addAttribute("guides", guides);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "bookings/trip_schedule";
    }

    @PostMapping("/trip-schedule/update-status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateTripStatus(@RequestParam String bookingId, 
                                                               @RequestParam String tripStatus,
                                                               @RequestParam(required = false) String startTime,
                                                               @RequestParam(required = false) String endTime,
                                                               @RequestParam(required = false) String tripNote) {
        Map<String, Object> response = new HashMap<>();
        try {
            DocumentSnapshot doc = firestore.collection("bookings").document(bookingId).get().get();
            if (doc.exists()) {
                String tourId = doc.getString("tourId");
                String startDate = doc.getString("startDate");
                String currentTripStatus = doc.getString("tripStatus");
                if (currentTripStatus == null) currentTripStatus = "preparing";
                String guideId = doc.getString("guideId");

                if ("started".equals(tripStatus)) {
                    if (guideId == null || guideId.isEmpty()) {
                        response.put("success", false);
                        response.put("message", "Không thể khởi hành! Vui lòng gán hướng dẫn viên trước.");
                        return ResponseEntity.ok(response);
                    }
                }

                QuerySnapshot group = firestore.collection("bookings")
                        .whereEqualTo("tourId", tourId)
                        .whereEqualTo("startDate", startDate)
                        .get().get();

                Map<String, Object> updates = new HashMap<>();
                updates.put("tripStatus", tripStatus);
                if (startTime != null && !startTime.isEmpty()) updates.put("startTime", startTime);
                if (endTime != null && !endTime.isEmpty()) updates.put("endTime", endTime);
                if (tripNote != null) updates.put("tripNote", tripNote);

                for (DocumentSnapshot d : group.getDocuments()) {
                    String bStatus = d.getString("tripStatus");
                    if (bStatus == null) bStatus = "preparing";
                    if (currentTripStatus.equals(bStatus)) {
                        d.getReference().update(updates);
                    }
                }
                
                response.put("success", true);
                response.put("message", "Cập nhật trạng thái chuyến đi thành công!");
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("message", "Không tìm thấy thông tin booking.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @PostMapping("/trip-schedule/assign-guide")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> assignGuide(@RequestParam String bookingId, 
                                                          @RequestParam String guideId) {
        Map<String, Object> response = new HashMap<>();
        try {
            DocumentSnapshot doc = firestore.collection("bookings").document(bookingId).get().get();
            if (doc.exists()) {
                String tourId = doc.getString("tourId");
                String startDate = doc.getString("startDate");
                String currentTripStatus = doc.getString("tripStatus");
                if (currentTripStatus == null) currentTripStatus = "preparing";

                QuerySnapshot group = firestore.collection("bookings")
                        .whereEqualTo("tourId", tourId)
                        .whereEqualTo("startDate", startDate)
                        .get().get();

                for (DocumentSnapshot d : group.getDocuments()) {
                    String bStatus = d.getString("tripStatus");
                    if (bStatus == null) bStatus = "preparing";
                    if (currentTripStatus.equals(bStatus)) {
                        d.getReference().update("guideId", guideId.isEmpty() ? null : guideId);
                    }
                }
                response.put("success", true);
                response.put("message", "Đã cập nhật hướng dẫn viên!");
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("message", "Không tìm thấy thông tin booking.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/detail/{id}")
    @SuppressWarnings("unchecked")
    public String viewDetail(@PathVariable String id, Model model) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking mainBooking = document.toObject(Booking.class);
                mainBooking.setId(id);

                if (document.contains("tour") && document.get("tour") instanceof Map) {
                    Map<String, Object> tourData = (Map<String, Object>) document.get("tour");
                    mainBooking.setTour(tourData);
                    if (mainBooking.getTourId() == null && tourData.containsKey("id")) {
                        mainBooking.setTourId(String.valueOf(tourData.get("id")));
                    }
                } else if (mainBooking.getTourId() != null) {
                    Map<String, Object> tourData = firestore.collection("tours").document(mainBooking.getTourId()).get().get().getData();
                    mainBooking.setTour(tourData);
                }

                ApiFuture<QuerySnapshot> groupFuture = firestore.collection("bookings")
                        .whereEqualTo("tourId", mainBooking.getTourId())
                        .whereEqualTo("startDate", mainBooking.getStartDate())
                        .whereEqualTo("status", "CONFIRMED")
                        .get();
                
                List<Booking> bookings = new ArrayList<>();
                long totalRevenue = 0;
                int totalPassengers = 0;
                for (QueryDocumentSnapshot doc : groupFuture.get().getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    if (mainBooking.getTripStatus().equals(b.getTripStatus())) {
                        b.setId(doc.getId());
                        bookings.add(b);
                        totalRevenue += b.getTotalPrice();
                        int p = (b.getAdults() != null ? b.getAdults().intValue() : 0)
                                + (b.getChildren() != null ? b.getChildren().intValue() : 0)
                                + (b.getInfants() != null ? b.getInfants().intValue() : 0);
                        totalPassengers += p;
                    }
                }
                
                model.addAttribute("booking", mainBooking); 
                model.addAttribute("bookings", bookings); 
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("totalPassengers", totalPassengers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "bookings/detail";
    }

    @GetMapping("/confirm/{id}")
    public String confirmBooking(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                
                if ("QR".equals(booking.getPaymentMethod())) {
                    if (!"da_thanh_toan".equals(booking.getPaymentStatus())) {
                        redirectAttributes.addFlashAttribute("errorMessage", "Chưa xác nhận thanh toán! Không thể xác nhận tour.");
                        return "redirect:/admin/bookings/booking-detail/" + id;
                    }
                }
                
                firestore.collection("bookings").document(id).update("status", "CONFIRMED");
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận tour thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/confirm-payment/{id}")
    public String confirmPayment(@PathVariable String id, RedirectAttributes redirectAttributes) {
        firestore.collection("bookings").document(id).update("paymentStatus", "da_thanh_toan");
        redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán!");
        return "redirect:/admin/bookings/booking-detail/" + id;
    }

    @GetMapping("/reject-payment/{id}")
    public String rejectPayment(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", "tu_choi");
        updates.put("status", "CANCELLED");
        firestore.collection("bookings").document(id).update(updates);
        redirectAttributes.addFlashAttribute("errorMessage", "Đã từ chối thanh toán và hủy đơn.");
        return "redirect:/admin/bookings/booking-detail/" + id;
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable String id) {
        firestore.collection("bookings").document(id).update("status", "CANCELLED");
        return "redirect:/admin/bookings";
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable String id) {
        firestore.collection("bookings").document(id).delete();
        return "redirect:/admin/bookings";
    }

    @GetMapping("/booking-detail/{id}")
    @SuppressWarnings("unchecked")
    public String bookingDetail(@PathVariable String id, Model model) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(id);

                Object createdAtObj = document.get("createdAt");
                if (createdAtObj instanceof com.google.cloud.Timestamp) {
                    booking.setCreatedAt(((com.google.cloud.Timestamp) createdAtObj).toDate());
                } else if (createdAtObj instanceof Long) {
                    booking.setCreatedAt(new java.util.Date((Long) createdAtObj));
                }

                if (document.contains("tour") && document.get("tour") instanceof Map) {
                    booking.setTour((Map<String, Object>) document.get("tour"));
                } else if (booking.getTourId() != null) {
                    Map<String, Object> tour = firestore.collection("tours").document(booking.getTourId()).get().get().getData();
                    booking.setTour(tour);
                }
                model.addAttribute("booking", booking);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "bookings/booking_detail";
    }
}
