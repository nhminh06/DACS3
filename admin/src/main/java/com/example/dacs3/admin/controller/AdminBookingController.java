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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
        private int minGuests = 1;
        private int maxGuests = 50;
        private Date latestBookingDate;
        private boolean canCancelBatch = false;

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
        public int getMinGuests() { return minGuests; }
        public void setMinGuests(int minGuests) { this.minGuests = minGuests; }
        public int getMaxGuests() { return maxGuests; }
        public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
        public Date getLatestBookingDate() { return latestBookingDate; }
        public void setLatestBookingDate(Date latestBookingDate) { this.latestBookingDate = latestBookingDate; }
        public boolean isCanCancelBatch() { return canCancelBatch; }
        public void setCanCancelBatch(boolean canCancelBatch) { this.canCancelBatch = canCancelBatch; }
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public String listBookings(Model model, 
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String search,
                               @RequestParam(defaultValue = "1") int page) {
        
        String effectiveStatus = (status == null || status.isEmpty()) ? "PENDING" : status;

        try {
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            Map<String, Map<String, Object>> toursMap = new HashMap<>();
            for (QueryDocumentSnapshot doc : toursFuture.get().getDocuments()) {
                Map<String, Object> tourData = doc.getData();
                tourData.put("id", doc.getId());
                toursMap.put(doc.getId(), tourData);
            }

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Booking> allBookings = new ArrayList<>();
            Map<String, Integer> occupancyMap = new HashMap<>();
            int totalConfirmed = 0;
            int totalPending = 0;
            int totalCancelled = 0;
            long totalRevenueAll = 0;

            for (QueryDocumentSnapshot doc : documents) {
                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());

                Object createdAtObj = doc.get("createdAt");
                if (createdAtObj instanceof Timestamp) {
                    b.setCreatedAt(((Timestamp) createdAtObj).toDate());
                } else if (createdAtObj instanceof Long) {
                    b.setCreatedAt(new Date((Long) createdAtObj));
                }
                
                if (doc.contains("tour") && doc.get("tour") instanceof Map) {
                    b.setTour((Map<String, Object>) doc.get("tour"));
                } else if (b.getTourId() != null) {
                    b.setTour(toursMap.get(b.getTourId()));
                }

                if ("CONFIRMED".equals(b.getStatus())) {
                    totalConfirmed++;
                    totalRevenueAll += b.getTotalPrice();
                    
                    String key = b.getTourId() + "_" + b.getStartDate();
                    int passengers = (b.getAdults() != null ? b.getAdults() : 0) + 
                                     (b.getChildren() != null ? b.getChildren() : 0) + 
                                     (b.getInfants() != null ? b.getInfants() : 0);
                    occupancyMap.put(key, occupancyMap.getOrDefault(key, 0) + passengers);
                } else if ("PENDING".equals(b.getStatus())) {
                    totalPending++;
                } else if ("CANCELLED".equals(b.getStatus())) {
                    totalCancelled++;
                }

                boolean matchesStatus = "ALL".equals(effectiveStatus) || effectiveStatus.equals(b.getStatus());
                boolean matchesSearch = true;
                if (search != null && !search.isEmpty()) {
                    String s = search.toLowerCase();
                    String tourTitle = getTourTitle(b).toLowerCase();
                    String custName = (b.getCustomerName() != null ? b.getCustomerName() : "").toLowerCase();
                    matchesSearch = b.getId().toLowerCase().contains(s) || tourTitle.contains(s) || custName.contains(s);
                }

                if (matchesStatus && matchesSearch) {
                    allBookings.add(b);
                }
            }

            allBookings.sort((b1, b2) -> {
                if (b1.getCreatedAt() == null || b2.getCreatedAt() == null) return 0;
                return b2.getCreatedAt().compareTo(b1.getCreatedAt());
            });

            int pageSize = 6;
            int totalFiltered = allBookings.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            int currentPage = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);

            model.addAttribute("bookings", allBookings.subList(start, end));
            model.addAttribute("occupancyMap", occupancyMap);
            model.addAttribute("totalBookingsCount", documents.size());
            model.addAttribute("confirmedCount", totalConfirmed);
            model.addAttribute("pendingCount", totalPending);
            model.addAttribute("cancelledCount", totalCancelled);
            model.addAttribute("totalRevenue", totalRevenueAll);
            model.addAttribute("search", search);
            model.addAttribute("currentStatus", effectiveStatus);
            model.addAttribute("currentPage", currentPage);
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
                
                redirectAttributes.addFlashAttribute("successMessage", "Xác nhận thành công!");
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings";
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
                
                ApiFuture<QuerySnapshot> future = firestore.collection("bookings")
                        .whereEqualTo("tourId", booking.getTourId())
                        .whereEqualTo("startDate", booking.getStartDate())
                        .get();
                
                List<Booking> bookings = new ArrayList<>();
                long totalRevenue = 0;
                int totalPassengers = 0;
                
                for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    b.setId(doc.getId());
                    if ("CONFIRMED".equals(b.getStatus()) || "CANCELLED".equals(b.getStatus())) {
                        bookings.add(b);
                        if ("CONFIRMED".equals(b.getStatus())) {
                            totalRevenue += b.getTotalPrice();
                        }
                        totalPassengers += (b.getAdults() != null ? b.getAdults() : 0);
                        totalPassengers += (b.getChildren() != null ? b.getChildren() : 0);
                        totalPassengers += (b.getInfants() != null ? b.getInfants() : 0);
                    }
                }
                
                model.addAttribute("booking", booking);
                model.addAttribute("bookings", bookings);
                model.addAttribute("totalPassengers", totalPassengers);
                model.addAttribute("totalRevenue", totalRevenue);
                
                if (booking.getTour() != null) {
                    Object minObj = booking.getTour().get("minGuests");
                    Object maxObj = booking.getTour().get("maxGuests");
                    int minG = minObj != null ? ((Long) minObj).intValue() : 1;
                    int maxG = maxObj != null ? ((Long) maxObj).intValue() : 50;
                    model.addAttribute("minGuests", minG);
                    model.addAttribute("maxGuests", maxG);
                }
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

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings").get();
            
            Map<String, GroupedTrip> groupedMap = new LinkedHashMap<>();
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                String bookingStatus = doc.getString("status");
                String tStatus = doc.getString("tripStatus");
                
                if (!"CONFIRMED".equals(bookingStatus) && tStatus == null) continue;

                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());
                if (doc.contains("tour") && doc.get("tour") instanceof Map) {
                    b.setTour((Map<String, Object>) doc.get("tour")); 
                } else if (b.getTourId() != null) {
                    b.setTour(toursMap.get(b.getTourId()));
                }

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
                    if (gt.getTour() != null) {
                        Object minObj = gt.getTour().get("minGuests");
                        Object maxObj = gt.getTour().get("maxGuests");
                        if (minObj != null) gt.setMinGuests(((Long) minObj).intValue());
                        if (maxObj != null) gt.setMaxGuests(((Long) maxObj).intValue());
                    }
                    return gt;
                });
                trip.getBookings().add(b);
                
                if ("CONFIRMED".equals(bookingStatus)) {
                    trip.setTotalRevenue(trip.getTotalRevenue() + b.getTotalPrice());
                }
                
                trip.setTotalPassengers(trip.getTotalPassengers() + (b.getAdults() != null ? b.getAdults() : 0));
                trip.setTotalPassengers(trip.getTotalPassengers() + (b.getChildren() != null ? b.getChildren() : 0));
                trip.setTotalPassengers(trip.getTotalPassengers() + (b.getInfants() != null ? b.getInfants() : 0));
                trip.setBookingCount(trip.getBookingCount() + 1);
            }

            LocalDate now = LocalDate.now();
            for (GroupedTrip trip : groupedMap.values()) {
                try {
                    LocalDate start = LocalDate.parse(trip.getStartDate());
                    long daysUntil = ChronoUnit.DAYS.between(now, start);
                    if (daysUntil <= 2 && "preparing".equals(trip.getTripStatus()) && trip.getTotalPassengers() < trip.getMinGuests()) {
                        trip.setCanCancelBatch(true);
                    }
                } catch (Exception e) {}
            }

            List<GroupedTrip> allTrips = new ArrayList<>(groupedMap.values());
            
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

            allTrips.sort((t1, t2) -> {
                Map<String, Integer> priority = new HashMap<>();
                priority.put("preparing", 1);
                priority.put("started", 2);
                priority.put("completed", 3);
                priority.put("cancelled", 4);
                
                int p1 = priority.getOrDefault(t1.getTripStatus(), 1);
                int p2 = priority.getOrDefault(t2.getTripStatus(), 1);
                
                if (p1 != p2) return Integer.compare(p1, p2);
                return t1.getStartDate().compareTo(t2.getStartDate());
            });

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

            List<Map<String, Object>> assignedGuides = new ArrayList<>();
            List<String> gIds = booking.getGuideIds();
            if (gIds != null && !gIds.isEmpty()) {
                for (String gId : gIds) {
                    DocumentSnapshot gDoc = firestore.collection("users").document(gId).get().get();
                    if (gDoc.exists()) {
                        Map<String, Object> gData = gDoc.getData();
                        gData.put("id", gDoc.getId());
                        assignedGuides.add(gData);
                    }
                }
            }
            booking.setGuidesList(assignedGuides);
            
            String currentTripStatus = document.getString("tripStatus");
            if (currentTripStatus == null) currentTripStatus = "preparing";
            booking.setTripStatus(currentTripStatus);

            ApiFuture<QuerySnapshot> future = firestore.collection("bookings")
                    .whereEqualTo("tourId", booking.getTourId())
                    .whereEqualTo("startDate", booking.getStartDate())
                    .get();
            
            List<Booking> bookings = new ArrayList<>();
            long totalRevenue = 0;
            int totalPassengers = 0;
            
            for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                String bookingStatus = doc.getString("status");
                String s = doc.getString("tripStatus");
                if (s == null) s = "preparing";

                if (s.equals(currentTripStatus)) {
                    Booking b = doc.toObject(Booking.class);
                    b.setId(doc.getId());
                    bookings.add(b);
                    if ("CONFIRMED".equals(bookingStatus)) {
                        totalRevenue += b.getTotalPrice();
                    }
                    totalPassengers += (b.getAdults() != null ? b.getAdults() : 0);
                    totalPassengers += (b.getChildren() != null ? b.getChildren() : 0);
                    totalPassengers += (b.getInfants() != null ? b.getInfants() : 0);
                }
            }

            ApiFuture<QuerySnapshot> guidesFuture = firestore.collection("users")
                    .whereEqualTo("role", "guide")
                    .get();
            
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
                    List<String> assignedGuideIds = (List<String>) ab.get("guideIds");
                    String abTripStatus = ab.getString("tripStatus");
                    if (abTripStatus == null) abTripStatus = "preparing";

                    if (assignedGuideIds != null && assignedGuideIds.contains(doc.getId()) && (abTripStatus.equals("preparing") || abTripStatus.equals("started"))) {
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
            
            if (booking.getTour() != null) {
                Object minObj = booking.getTour().get("minGuests");
                Object maxObj = booking.getTour().get("maxGuests");
                trip.setMinGuests(minObj != null ? ((Long) minObj).intValue() : 1);
                trip.setMaxGuests(maxObj != null ? ((Long) maxObj).intValue() : 50);
            }

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

            if ("started".equals(tripStatus)) {
                DocumentSnapshot tourDoc = firestore.collection("tours").document(tourId).get().get();
                int minGuests = 1;
                int maxGuests = 50;
                if (tourDoc.exists()) {
                    Long min = tourDoc.getLong("minGuests");
                    Long max = tourDoc.getLong("maxGuests");
                    if (min != null) minGuests = min.intValue();
                    if (max != null) maxGuests = max.intValue();
                }

                QuerySnapshot tripBookings = firestore.collection("bookings")
                        .whereEqualTo("tourId", tourId)
                        .whereEqualTo("startDate", startDate)
                        .whereEqualTo("status", "CONFIRMED")
                        .get().get();
                
                int totalPassengers = 0;
                for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                    String s = d.getString("tripStatus");
                    if (s == null) s = "preparing";
                    if (s.equals(currentTripStatus)) {
                        totalPassengers += (d.getLong("adults") != null ? d.getLong("adults").intValue() : 0);
                        totalPassengers += (d.getLong("children") != null ? d.getLong("children").intValue() : 0);
                        totalPassengers += (d.getLong("infants") != null ? d.getLong("infants").intValue() : 0);
                    }
                }

                if (totalPassengers < minGuests) {
                    response.put("success", false);
                    response.put("message", "Không thể khởi hành: Số lượng khách (" + totalPassengers + ") chưa đạt tối thiểu (" + minGuests + ")");
                    return response;
                }
                if (totalPassengers > maxGuests + 3) {
                    response.put("success", false);
                    response.put("message", "Không thể khởi hành: Số lượng khách (" + totalPassengers + ") vượt quá giới hạn tối đa (" + (maxGuests + 3) + ")");
                    return response;
                }
            }

            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
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

    @PostMapping("/trip-batch-cancel")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public Map<String, Object> batchCancelTrip(@RequestParam String tourId, @RequestParam String startDate) {
        Map<String, Object> response = new HashMap<>();
        try {
            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
                    .get().get();

            for (QueryDocumentSnapshot doc : tripBookings.getDocuments()) {
                String id = doc.getId();
                String currentStatus = doc.getString("status");
                
                if (!"CANCELLED".equals(currentStatus)) {
                    Booking b = doc.toObject(Booking.class);
                    b.setId(id);
                    loadTourInfo(b, doc);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", "CANCELLED");
                    updates.put("tripStatus", "cancelled");
                    updates.put("tripNote", "Hủy chuyến do không đủ số lượng khách tối thiểu.");
                    
                    firestore.collection("bookings").document(id).update(updates).get();

                    sendBookingEmail(b, "Thông báo hủy chuyến đi", "đã bị hủy do không đủ số lượng khách tối thiểu. Chúng tôi sẽ liên hệ để hoàn tiền (nếu có).");
                    sendAppNotification(b.getUserId(), "Chuyến đi bị hủy", 
                        "Chuyến đi " + getTourTitle(b) + " ngày " + startDate + " đã bị hủy do không đủ khách.");
                } else {
                    firestore.collection("bookings").document(id).update("tripStatus", "cancelled").get();
                }
            }
            response.put("success", true);
            response.put("message", "Đã hủy toàn bộ đơn hàng của chuyến đi này.");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/trip-schedule/assign-guide")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public Map<String, Object> assignGuide(@RequestParam String bookingId, @RequestParam String guideId, @RequestParam(required = false) String action) {
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

            List<String> currentGuideIds = (List<String>) doc.get("guideIds");
            if (currentGuideIds == null) currentGuideIds = new ArrayList<>();
            else currentGuideIds = new ArrayList<>(currentGuideIds);

            if ("remove".equals(action)) {
                currentGuideIds.remove(guideId);
            } else if (guideId != null && !guideId.isEmpty() && !currentGuideIds.contains(guideId)) {
                currentGuideIds.add(guideId);
            } else if (guideId == null || guideId.isEmpty()) {
                currentGuideIds.clear();
            }

            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
                    .get().get();

            for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                String s = d.getString("tripStatus");
                if (s == null) s = "preparing";
                
                if (s.equals(currentTripStatus)) {
                    firestore.collection("bookings").document(d.getId()).update("guideIds", currentGuideIds).get();
                }
            }

            response.put("success", true);
            response.put("message", "Cập nhật hướng dẫn viên thành công");
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

    @GetMapping("/trip-delete/{tourId}/{startDate}")
    public String deleteTrip(@PathVariable String tourId, @PathVariable String startDate) {
        try {
            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
                    .get().get();
            for (QueryDocumentSnapshot doc : tripBookings.getDocuments()) {
                firestore.collection("bookings").document(doc.getId()).delete();
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings/schedule";
    }
}
