package com.example.dacs3.admin.controller;

import com.example.dacs3.admin.model.Booking;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private JavaMailSender mailSender;

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
    public String listBookings(Model model, 
                               @RequestParam(required = false) String status,
                               @RequestParam(defaultValue = "1") int page) {
        try {
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            Map<String, Map<String, Object>> toursMap = new HashMap<>();
            for (QueryDocumentSnapshot doc : toursFuture.get().getDocuments()) {
                toursMap.put(doc.getId(), doc.getData());
            }

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Booking> allFilteredBookings = new ArrayList<>();
            long totalRevenue = 0;
            int confirmedCount = 0;
            int pendingCount = 0;
            int unpaidCount = 0;

            for (QueryDocumentSnapshot document : documents) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(document.getId());

                Object createdAtObj = document.get("createdAt");
                if (createdAtObj instanceof Timestamp) {
                    booking.setCreatedAt(((Timestamp) createdAtObj).toDate());
                } else if (createdAtObj instanceof Long) {
                    booking.setCreatedAt(new java.util.Date((Long) createdAtObj));
                }
                
                if (document.contains("tour") && document.get("tour") instanceof Map) {
                    booking.setTour((Map<String, Object>) document.get("tour"));
                } else if (booking.getTourId() != null) {
                    booking.setTour(toursMap.get(booking.getTourId()));
                }

                if ("CONFIRMED".equals(booking.getStatus())) {
                    confirmedCount++;
                    totalRevenue += booking.getTotalPrice();
                } else if ("PENDING".equals(booking.getStatus())) {
                    pendingCount++;
                    if ("QR".equals(booking.getPaymentMethod()) && !"da_thanh_toan".equals(booking.getPaymentStatus())) {
                        unpaidCount++;
                    }
                }

                if (status == null || status.isEmpty() || status.equals(booking.getStatus())) {
                    allFilteredBookings.add(booking);
                }
            }

            // Sắp xếp theo ngày đặt mới nhất
            allFilteredBookings.sort((b1, b2) -> {
                if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
                return b2.getCreatedAt().compareTo(b1.getCreatedAt());
            });

            // Phân trang
            int pageSize = 6;
            int totalFiltered = allFilteredBookings.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            
            if (page < 1) page = 1;
            if (totalPages > 0 && page > totalPages) page = totalPages;

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);

            List<Booking> pagedBookings = new ArrayList<>();
            if (start < totalFiltered) {
                pagedBookings = allFilteredBookings.subList(start, end);
            }

            model.addAttribute("bookings", pagedBookings);
            model.addAttribute("totalBookingsCount", documents.size());
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("unpaidCount", unpaidCount);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("currentStatus", status);
            
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalFiltered", totalFiltered);

        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/list";
    }

    @GetMapping("/confirm/{id}")
    public String confirmBooking(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(id);
                loadTourInfo(booking, document);
                
                firestore.collection("bookings").document(id).update("status", "CONFIRMED").get();
                
                sendBookingEmail(booking, "Xác nhận đặt tour thành công", "đã được xác nhận thành công");
                sendAppNotification(booking.getUserId(), "Xác nhận tour thành công", 
                    "Tour " + getTourTitle(booking) + " khởi hành ngày " + booking.getStartDate() + " đã được xác nhận.");
                
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận tour thành công!");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings/booking-detail/" + id;
    }

    @GetMapping("/confirm-payment/{id}")
    public String confirmPayment(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            firestore.collection("bookings").document(id).update("paymentStatus", "da_thanh_toan").get();
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            Booking booking = document.toObject(Booking.class);
            booking.setId(id);
            loadTourInfo(booking, document);
            
            sendBookingEmail(booking, "Xác nhận thanh toán thành công", "đã được xác nhận thanh toán");
            sendAppNotification(booking.getUserId(), "Thanh toán thành công", 
                "Chúng tôi đã xác nhận thanh toán cho tour " + getTourTitle(booking) + ".");
            
            redirectAttributes.addFlashAttribute("successMessage", "Đã xác nhận thanh toán!");
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings/booking-detail/" + id;
    }

    @GetMapping("/reject-payment/{id}")
    public String rejectPayment(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("paymentStatus", "tu_choi");
            updates.put("status", "CANCELLED");
            firestore.collection("bookings").document(id).update(updates).get();
            
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            Booking booking = document.toObject(Booking.class);
            booking.setId(id);
            loadTourInfo(booking, document);
            
            sendBookingEmail(booking, "Thông báo hủy đơn đặt tour", "đã bị từ chối thanh toán và hủy bỏ");
            
            String orderIdSuffix = id.length() > 5 ? id.substring(0, 5) : id;
            sendAppNotification(booking.getUserId(), "Thông báo hủy đơn", 
                "Đơn hàng #" + orderIdSuffix + " đã bị hủy do thanh toán không hợp lệ.");
            
            redirectAttributes.addFlashAttribute("errorMessage", "Đã từ chối thanh toán và hủy đơn.");
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings/booking-detail/" + id;
    }

    @GetMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable String id) {
        try {
            firestore.collection("bookings").document(id).update("status", "CANCELLED").get();
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(id);
                loadTourInfo(booking, document);
                
                sendBookingEmail(booking, "Thông báo hủy tour", "đã được hủy theo yêu cầu");
                sendAppNotification(booking.getUserId(), "Hủy tour thành công", 
                    "Tour " + getTourTitle(booking) + " của bạn đã được hủy bỏ thành công.");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings";
    }

    private String getTourTitle(Booking booking) {
        if (booking.getTour() != null && booking.getTour().get("title") != null) {
            return String.valueOf(booking.getTour().get("title"));
        }
        return "Tour du lịch";
    }

    private void sendAppNotification(String userId, String title, String message) {
        if (userId == null || userId.isEmpty()) return;
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId", userId);
            notification.put("title", title);
            notification.put("message", message);
            notification.put("timestamp", Timestamp.now());
            notification.put("isRead", false);
            notification.put("type", "BOOKING_STATUS");
            firestore.collection("notifications").add(notification);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @SuppressWarnings("unchecked")
    private void loadTourInfo(Booking booking, DocumentSnapshot doc) throws Exception {
        if (doc.contains("tour") && doc.get("tour") instanceof Map) {
            booking.setTour((Map<String, Object>) doc.get("tour"));
        } else if (booking.getTourId() != null) {
            Map<String, Object> tour = firestore.collection("tours").document(booking.getTourId()).get().get().getData();
            booking.setTour(tour);
        }
    }

    private void sendBookingEmail(Booking booking, String subjectTitle, String statusText) {
        try {
            if (booking.getEmail() == null || booking.getEmail().isEmpty()) return;
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            
            String tourName = getTourTitle(booking);
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

            String orderIdDisplay = booking.getId().length() >= 8 ? booking.getId().substring(0, 8).toUpperCase() : booking.getId().toUpperCase();

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e2e8f0; border-radius: 12px; overflow: hidden;'>" +
                    "<div style='background-color: #2563eb; padding: 20px; text-align: center; color: white;'>" +
                    "<h2>WIMD Travel - " + subjectTitle + "</h2>" +
                    "</div>" +
                    "<div style='padding: 24px; color: #1e293b;'>" +
                    "<p>Xin chào <strong>" + (booking.getCustomerName() != null ? booking.getCustomerName() : "Quý khách") + "</strong>,</p>" +
                    "<p>Đơn đặt tour của bạn tại <strong>WIMD Travel</strong> " + statusText + ".</p>" +
                    "<div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 20px 0;'>" +
                    "<p><strong>Tour:</strong> " + tourName + "</p>" +
                    "<p><strong>Ngày khởi hành:</strong> " + booking.getStartDate() + "</p>" +
                    "<p><strong>Mã đơn hàng:</strong> #" + orderIdDisplay + "</p>" +
                    "<p><strong>Số tiền:</strong> <span style='color: #ef4444; font-weight: bold;'>" + currencyFormat.format(booking.getTotalPrice()) + "</span></p>" +
                    "</div>" +
                    "<p>Cảm ơn bạn đã lựa chọn WIMD Travel!</p>" +
                    "</div>" +
                    "</div>";

            helper.setText(htmlMsg, true);
            helper.setTo(booking.getEmail());
            helper.setSubject("[WIMD Travel] " + subjectTitle);
            helper.setFrom("minhminh778894@gmail.com");
            mailSender.send(mimeMessage);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @GetMapping("/booking-detail/{id}")
    @SuppressWarnings("unchecked")
    public String bookingDetail(@PathVariable String id, Model model) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(id);
                loadTourInfo(booking, document);
                
                model.addAttribute("booking", booking);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/booking_detail";
    }

    @GetMapping("/grouped-trip-detail/{id}")
    @SuppressWarnings("unchecked")
    public String groupedTripDetail(@PathVariable String id, Model model) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (document.exists()) {
                Booking booking = document.toObject(Booking.class);
                booking.setId(id);
                loadTourInfo(booking, document);
                
                // Lấy tất cả các booking cùng tour, ngày khởi hành và trạng thái chuyến đi
                ApiFuture<QuerySnapshot> future = firestore.collection("bookings")
                        .whereEqualTo("tourId", booking.getTourId())
                        .whereEqualTo("startDate", booking.getStartDate())
                        .whereEqualTo("status", "CONFIRMED")
                        .get();
                
                List<Booking> bookings = new ArrayList<>();
                long totalRevenue = 0;
                int totalPassengers = 0;
                
                String targetTripStatus = document.getString("tripStatus");
                if (targetTripStatus == null) targetTripStatus = "preparing";

                for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                    String s = doc.getString("tripStatus");
                    if (s == null) s = "preparing";

                    if (s.equals(targetTripStatus)) {
                        Booking b = doc.toObject(Booking.class);
                        b.setId(doc.getId());
                        bookings.add(b);
                        totalRevenue += b.getTotalPrice();
                        totalPassengers += (b.getAdults() != null ? b.getAdults() : 0);
                        totalPassengers += (b.getChildren() != null ? b.getChildren() : 0);
                        totalPassengers += (b.getInfants() != null ? b.getInfants() : 0);
                    }
                }
                
                model.addAttribute("booking", booking);
                model.addAttribute("bookings", bookings);
                model.addAttribute("totalPassengers", totalPassengers);
                model.addAttribute("totalRevenue", totalRevenue);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/detail";
    }

    @GetMapping("/schedule")
    @SuppressWarnings("unchecked")
    public String viewSchedule(Model model, 
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String tourId,
                               @RequestParam(required = false) String status) {
        try {
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            Map<String, Map<String, Object>> toursMap = new HashMap<>();
            List<Map<String, Object>> toursList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : toursFuture.get().getDocuments()) {
                Map<String, Object> tourData = doc.getData();
                tourData.put("id", doc.getId());
                toursMap.put(doc.getId(), tourData);
                toursList.add(tourData);
            }
            model.addAttribute("toursList", toursList);

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings")
                    .whereEqualTo("status", "CONFIRMED")
                    .get();
            
            Map<String, GroupedTrip> groupedMap = new LinkedHashMap<>();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());
                if (doc.contains("tour") && doc.get("tour") instanceof Map) {
                    b.setTour((Map<String, Object>) doc.get("tour"));
                } else if (b.getTourId() != null) {
                    b.setTour(toursMap.get(b.getTourId()));
                }

                String tStatus = doc.getString("tripStatus");
                if (tStatus == null) tStatus = "preparing";
                b.setTripStatus(tStatus);

                String key = b.getTourId() + "_" + b.getStartDate() + "_" + tStatus;
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
                trip.setTotalRevenue(trip.getTotalRevenue() + b.getTotalPrice());
                trip.setTotalPassengers(trip.getTotalPassengers() + (b.getAdults() != null ? b.getAdults() : 0));
                trip.setTotalPassengers(trip.getTotalPassengers() + (b.getChildren() != null ? b.getChildren() : 0));
                trip.setTotalPassengers(trip.getTotalPassengers() + (b.getInfants() != null ? b.getInfants() : 0));
                trip.setBookingCount(trip.getBookingCount() + 1);
            }

            List<GroupedTrip> allTrips = new ArrayList<>(groupedMap.values());
            
            // Filter logic
            if (search != null && !search.isEmpty()) {
                String s = search.toLowerCase();
                allTrips = allTrips.stream()
                        .filter(t -> t.getTour() != null && t.getTour().get("title") != null && 
                                   String.valueOf(t.getTour().get("title")).toLowerCase().contains(s))
                        .collect(Collectors.toList());
            }
            if (tourId != null && !tourId.isEmpty()) {
                allTrips = allTrips.stream()
                        .filter(t -> tourId.equals(t.getTourId()))
                        .collect(Collectors.toList());
            }
            if (status != null && !status.isEmpty()) {
                allTrips = allTrips.stream()
                        .filter(t -> status.equals(t.getTripStatus()))
                        .collect(Collectors.toList());
            }

            // Pagination logic
            int pageSize = 2;
            int totalFilteredTrips = allTrips.size();
            int totalPages = (int) Math.ceil((double) totalFilteredTrips / pageSize);
            
            if (page < 1) page = 1;
            if (totalPages > 0 && page > totalPages) page = totalPages;

            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFilteredTrips);

            List<GroupedTrip> pagedTrips = new ArrayList<>();
            if (start < totalFilteredTrips) {
                pagedTrips = allTrips.subList(start, end);
            }

            // Stats based on ALL trips (not filtered)
            int preparingCount = 0;
            int startedCount = 0;
            int completedCount = 0;
            for (GroupedTrip trip : groupedMap.values()) {
                String s = trip.getTripStatus();
                if ("preparing".equals(s)) preparingCount++;
                else if ("started".equals(s)) startedCount++;
                else if ("completed".equals(s)) completedCount++;
            }

            model.addAttribute("trips", pagedTrips);
            model.addAttribute("totalTrips", groupedMap.size());
            model.addAttribute("preparingCount", preparingCount);
            model.addAttribute("startedCount", startedCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("search", search);
            model.addAttribute("tourFilter", tourId);
            model.addAttribute("statusFilter", status);

        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/schedule";
    }

    @GetMapping("/trip-schedule/{id}")
    @SuppressWarnings("unchecked")
    public String tripSchedule(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        try {
            DocumentSnapshot document = firestore.collection("bookings").document(id).get().get();
            if (!document.exists()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin chuyến đi!");
                return "redirect:/admin/bookings/schedule";
            }
            
            Booking booking = document.toObject(Booking.class);
            booking.setId(id);
            loadTourInfo(booking, document);

            // Đảm bảo lấy guideId trực tiếp từ document
            String gId = document.getString("guideId");
            if (gId != null && !gId.isEmpty()) {
                DocumentSnapshot guideDoc = firestore.collection("users").document(gId).get().get();
                if (guideDoc.exists()) {
                    booking.setGuideId(gId);
                    booking.setGuide(guideDoc.getData());
                }
            }
            
            String currentTripStatus = document.getString("tripStatus");
            if (currentTripStatus == null) currentTripStatus = "preparing";
            booking.setTripStatus(currentTripStatus);

            // Get all bookings for this trip
            ApiFuture<QuerySnapshot> future = firestore.collection("bookings")
                    .whereEqualTo("tourId", booking.getTourId())
                    .whereEqualTo("startDate", booking.getStartDate())
                    .whereEqualTo("status", "CONFIRMED")
                    .get();
            
            List<Booking> bookings = new ArrayList<>();
            long totalRevenue = 0;
            int totalPassengers = 0;
            
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                String s = doc.getString("tripStatus");
                if (s == null) s = "preparing";

                if (s.equals(currentTripStatus)) {
                    Booking b = doc.toObject(Booking.class);
                    b.setId(doc.getId());
                    bookings.add(b);
                    totalRevenue += b.getTotalPrice();
                    totalPassengers += (b.getAdults() != null ? b.getAdults() : 0);
                    totalPassengers += (b.getChildren() != null ? b.getChildren() : 0);
                    totalPassengers += (b.getInfants() != null ? b.getInfants() : 0);
                }
            }

            // Fetch all users with role 'guide'
            ApiFuture<QuerySnapshot> guidesFuture = firestore.collection("users")
                    .whereEqualTo("role", "guide")
                    .get();
            
            // Check busy status for each guide
            ApiFuture<QuerySnapshot> allBookingsFuture = firestore.collection("bookings")
                    .whereEqualTo("status", "CONFIRMED")
                    .get();
            List<QueryDocumentSnapshot> activeBookings = allBookingsFuture.get().getDocuments();

            List<Map<String, Object>> guidesList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : guidesFuture.get().getDocuments()) {
                Map<String, Object> g = new HashMap<>(doc.getData());
                g.put("id", doc.getId());
                
                boolean isBusy = false;
                String busyReason = "";
                
                for (QueryDocumentSnapshot ab : activeBookings) {
                    String assignedGuideId = ab.getString("guideId");
                    String abTripStatus = ab.getString("tripStatus");
                    if (abTripStatus == null) abTripStatus = "preparing";

                    if (doc.getId().equals(assignedGuideId) && (abTripStatus.equals("preparing") || abTripStatus.equals("started"))) {
                        // Check if it's NOT the current trip
                        if (!booking.getTourId().equals(ab.getString("tourId")) || 
                            !booking.getStartDate().equals(ab.getString("startDate")) ||
                            !currentTripStatus.equals(abTripStatus)) {
                            isBusy = true;
                            busyReason = "started".equals(abTripStatus) ? "Đang dẫn tour" : "Đã được gán tour khác";
                            break;
                        }
                    }
                }
                
                g.put("isBusy", isBusy);
                g.put("busyReason", busyReason);
                guidesList.add(g);
            }
            
            GroupedTrip trip = new GroupedTrip();
            trip.setId(id);
            trip.setTourId(booking.getTourId());
            trip.setStartDate(booking.getStartDate());
            trip.setTripStatus(currentTripStatus);
            trip.setTour(booking.getTour());
            trip.setBookings(bookings);
            trip.setTotalRevenue(totalRevenue);
            trip.setTotalPassengers(totalPassengers);
            trip.setBookingCount(bookings.size());
            
            model.addAttribute("booking", booking);
            model.addAttribute("trip", trip);
            model.addAttribute("bookings", bookings);
            model.addAttribute("totalPassengers", totalPassengers);
            model.addAttribute("guides", guidesList);
        } catch (Exception e) { 
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/bookings/schedule";
        }
        return "bookings/trip_schedule";
    }

    @PostMapping("/trip-schedule/update-status")
    @ResponseBody
    public Map<String, Object> updateTripStatus(@RequestParam String bookingId, 
                                               @RequestParam String tripStatus,
                                               @RequestParam(required = false) String startTime,
                                               @RequestParam(required = false) String endTime,
                                               @RequestParam(required = false) String tripNote) {
        Map<String, Object> response = new HashMap<>();
        try {
            DocumentSnapshot doc = firestore.collection("bookings").document(bookingId).get().get();
            if (!doc.exists()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng");
                return response;
            }

            String tourId = doc.getString("tourId");
            String startDate = doc.getString("startDate");
            String currentTripStatus = doc.getString("tripStatus");
            if (currentTripStatus == null) currentTripStatus = "preparing";

            // Update all bookings for this trip
            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
                    .whereEqualTo("status", "CONFIRMED")
                    .get().get();

            Map<String, Object> updates = new HashMap<>();
            updates.put("tripStatus", tripStatus);
            if (startTime != null && !startTime.isEmpty()) updates.put("startTime", startTime);
            if (endTime != null && !endTime.isEmpty()) updates.put("endTime", endTime);
            if (tripNote != null && !tripNote.isEmpty()) updates.put("tripNote", tripNote);

            for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                String s = d.getString("tripStatus");
                if (s == null) s = "preparing";
                if (s.equals(currentTripStatus)) {
                    firestore.collection("bookings").document(d.getId()).update(updates).get();
                }
            }

            response.put("success", true);
            response.put("message", "Cập nhật trạng thái thành công");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/trip-schedule/assign-guide")
    @ResponseBody
    public Map<String, Object> assignGuide(@RequestParam String bookingId, @RequestParam String guideId) {
        Map<String, Object> response = new HashMap<>();
        try {
            DocumentSnapshot doc = firestore.collection("bookings").document(bookingId).get().get();
            if (!doc.exists()) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng");
                return response;
            }

            String tourId = doc.getString("tourId");
            String startDate = doc.getString("startDate");
            String currentTripStatus = doc.getString("tripStatus");
            if (currentTripStatus == null) currentTripStatus = "preparing";

            // Luôn cập nhật booking cụ thể này
            firestore.collection("bookings").document(bookingId).update("guideId", guideId).get();

            // Tìm và cập nhật các booking khác cùng chuyến
            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
                    .whereEqualTo("status", "CONFIRMED")
                    .get().get();

            for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                String s = d.getString("tripStatus");
                if (s == null) s = "preparing";
                
                if (s.equals(currentTripStatus) && !d.getId().equals(bookingId)) {
                    firestore.collection("bookings").document(d.getId()).update("guideId", guideId).get();
                }
            }

            response.put("success", true);
            response.put("message", "Gán hướng dẫn viên thành công");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable String id) {
        firestore.collection("bookings").document(id).delete();
        return "redirect:/admin/bookings";
    }
}
