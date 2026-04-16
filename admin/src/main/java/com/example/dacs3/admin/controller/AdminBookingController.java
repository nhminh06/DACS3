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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bookings")
@SuppressWarnings("unchecked")
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
        private int confirmedPassengers = 0;
        private int bookingCount = 0;
        private int confirmedCount = 0;
        private int minGuests = 1;
        private int maxGuests = 50;
        private int guideCount = 0;
        private boolean canCancelBatch = false;
        private boolean hasBookings = false;

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
        public int getConfirmedPassengers() { return confirmedPassengers; }
        public void setConfirmedPassengers(int confirmedPassengers) { this.confirmedPassengers = confirmedPassengers; }
        public int getBookingCount() { return bookingCount; }
        public void setBookingCount(int bookingCount) { this.bookingCount = bookingCount; }
        public int getConfirmedCount() { return confirmedCount; }
        public void setConfirmedCount(int confirmedCount) { this.confirmedCount = confirmedCount; }
        public int getMinGuests() { return minGuests; }
        public void setMinGuests(int minGuests) { this.minGuests = minGuests; }
        public int getMaxGuests() { return maxGuests; }
        public void setMaxGuests(int maxGuests) { this.maxGuests = maxGuests; }
        public int getGuideCount() { return guideCount; }
        public void setGuideCount(int guideCount) { this.guideCount = guideCount; }
        public boolean isCanCancelBatch() { return canCancelBatch; }
        public void setCanCancelBatch(boolean canCancelBatch) { this.canCancelBatch = canCancelBatch; }
        public boolean isHasBookings() { return hasBookings; }
        public void setHasBookings(boolean hasBookings) { this.hasBookings = hasBookings; }
    }

    @GetMapping
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
            Booking b = document.toObject(Booking.class);
            b.setId(id);
            loadTourInfo(b, document);
            
            sendBookingEmail(b, "Thông báo hủy đơn đặt tour", "đã bị từ chối thanh toán và hủy bỏ");
            sendAppNotification(b.getUserId(), "Thông báo hủy đơn", 
                "Đơn hàng #" + id.substring(0, Math.min(id.length(), 5)) + " đã bị hủy do thanh toán không hợp lệ.");
            
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
                Booking b = document.toObject(Booking.class);
                b.setId(id);
                loadTourInfo(b, document);
                sendBookingEmail(b, "Thông báo hủy tour", "đã được hủy theo yêu cầu");
                sendAppNotification(b.getUserId(), "Hủy tour thành công", 
                    "Tour " + getTourTitle(b) + " của bạn đã được hủy bỏ thành công.");
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
                int confirmedPassengers = 0;
                int confirmedCount = 0;
                
                for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
                    Booking b = doc.toObject(Booking.class);
                    b.setId(doc.getId());
                    bookings.add(b);
                    
                    int passengers = (b.getAdults() != null ? b.getAdults() : 0) + 
                                     (b.getChildren() != null ? b.getChildren() : 0) + 
                                     (b.getInfants() != null ? b.getInfants() : 0);
                                     
                    if (!"CANCELLED".equals(b.getStatus())) {
                        totalPassengers += passengers;
                    }
                    if ("CONFIRMED".equals(b.getStatus())) {
                        confirmedCount++;
                        confirmedPassengers += passengers;
                        totalRevenue += b.getTotalPrice();
                    }
                }
                
                model.addAttribute("booking", booking);
                model.addAttribute("bookings", bookings);
                model.addAttribute("totalPassengers", totalPassengers);
                model.addAttribute("confirmedPassengers", confirmedPassengers);
                model.addAttribute("confirmedCount", confirmedCount);
                model.addAttribute("totalRevenue", totalRevenue);
                
                if (booking.getTour() != null) {
                    Object minG = booking.getTour().get("minGuests");
                    Object maxG = booking.getTour().get("maxGuests");
                    model.addAttribute("minGuests", minG != null ? ((Long) minG).intValue() : 1);
                    model.addAttribute("maxGuests", maxG != null ? ((Long) maxG).intValue() : 50);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/detail";
    }

    @GetMapping("/schedule")
    public String viewSchedule(Model model, 
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String hasTrips) {
        try {
            ApiFuture<QuerySnapshot> toursFuture = firestore.collection("tours").get();
            List<QueryDocumentSnapshot> tourDocs = toursFuture.get().getDocuments();
            
            ApiFuture<QuerySnapshot> bookingsFuture = firestore.collection("bookings").get();
            List<QueryDocumentSnapshot> bookingDocs = bookingsFuture.get().getDocuments();

            int preparingCount = 0;
            int startedCount = 0;
            int completedCount = 0;
            long totalConfirmedRevenue = 0;
            int totalConfirmedBookings = 0;
            
            Map<String, Integer> tourBookingCount = new HashMap<>();
            Map<String, String> tripStatusesWithBookings = new HashMap<>();

            for (QueryDocumentSnapshot doc : bookingDocs) {
                String tourId = doc.getString("tourId");
                String startDate = doc.getString("startDate");
                String tripStatus = doc.getString("tripStatus");
                if (tripStatus == null) tripStatus = "preparing";
                String bookingStatus = doc.getString("status");

                String tripKey = tourId + "_" + startDate;
                tripStatusesWithBookings.put(tripKey, tripStatus);
                
                tourBookingCount.put(tourId, tourBookingCount.getOrDefault(tourId, 0) + 1);
                if ("CONFIRMED".equals(bookingStatus)) {
                    totalConfirmedBookings++;
                    Long price = doc.getLong("totalPrice");
                    if (price != null) totalConfirmedRevenue += price;
                }
            }

            List<Map<String, Object>> toursList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : tourDocs) {
                Map<String, Object> tour = new HashMap<>(doc.getData());
                String tourId = doc.getId();
                tour.put("id", tourId);
                
                // Get defined dates
                Set<String> allPlannedDates = new HashSet<>();
                Object sdObj = tour.get("startDate");
                if (sdObj instanceof String) {
                    String[] parts = ((String) sdObj).split(",");
                    for (String p : parts) if (!p.trim().isEmpty()) allPlannedDates.add(p.trim());
                } else if (sdObj instanceof List) {
                    allPlannedDates.addAll((List<String>) sdObj);
                }

                int tourActiveTrips = 0;
                
                // Count from planned dates (even if no bookings)
                for (String date : allPlannedDates) {
                    String status = tripStatusesWithBookings.getOrDefault(tourId + "_" + date, "preparing");
                    if ("preparing".equals(status)) {
                        preparingCount++;
                        tourActiveTrips++;
                    } else if ("started".equals(status)) {
                        startedCount++;
                        tourActiveTrips++;
                    } else if ("completed".equals(status)) {
                        completedCount++;
                    }
                }
                
                // Count extra departures that have bookings but are not in the planned list
                for (Map.Entry<String, String> entry : tripStatusesWithBookings.entrySet()) {
                    if (entry.getKey().startsWith(tourId + "_")) {
                        String date = entry.getKey().substring(tourId.length() + 1);
                        if (!allPlannedDates.contains(date)) {
                            String status = entry.getValue();
                            if ("preparing".equals(status)) {
                                preparingCount++;
                                tourActiveTrips++;
                            } else if ("started".equals(status)) {
                                startedCount++;
                                tourActiveTrips++;
                            } else if ("completed".equals(status)) {
                                completedCount++;
                            }
                        }
                    }
                }

                tour.put("activeTrips", tourActiveTrips);
                tour.put("totalBookings", tourBookingCount.getOrDefault(tourId, 0));
                
                boolean matchesSearch = search == null || search.isEmpty() || 
                    String.valueOf(tour.get("title")).toLowerCase().contains(search.toLowerCase()) ||
                    String.valueOf(tour.get("maTour")).toLowerCase().contains(search.toLowerCase());
                
                boolean matchesType = type == null || type.isEmpty() || "ALL".equals(type) ||
                    type.equals(tour.get("type"));
                
                boolean matchesHasTrips = hasTrips == null || hasTrips.isEmpty() || "ALL".equals(hasTrips) ||
                    ("ACTIVE".equals(hasTrips) && tourActiveTrips > 0) ||
                    ("NONE".equals(hasTrips) && tourActiveTrips == 0);

                if (matchesSearch && matchesType && matchesHasTrips) {
                    toursList.add(tour);
                }
            }

            int pageSize = 6;
            int totalFiltered = toursList.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            int currentPage = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);

            model.addAttribute("tours", toursList.subList(start, end));
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("search", search);
            model.addAttribute("currentType", type != null ? type : "ALL");
            model.addAttribute("currentHasTrips", hasTrips != null ? hasTrips : "ALL");
            model.addAttribute("totalToursCount", tourDocs.size());
            model.addAttribute("preparingCount", preparingCount);
            model.addAttribute("startedCount", startedCount);
            model.addAttribute("completedCount", completedCount);
            model.addAttribute("totalRevenue", totalConfirmedRevenue);
            model.addAttribute("totalConfirmedBookings", totalConfirmedBookings);

        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/schedule";
    }

    @GetMapping("/tour-trips/{tourId}")
    public String viewTourTrips(@PathVariable String tourId, Model model) {
        try {
            DocumentSnapshot tourDoc = firestore.collection("tours").document(tourId).get().get();
            if (!tourDoc.exists()) return "redirect:/admin/bookings/schedule";
            
            Map<String, Object> tourData = tourDoc.getData();
            tourData.put("id", tourDoc.getId());
            model.addAttribute("tour", tourData);

            int minG = 1; int maxG = 50;
            if (tourData.get("minGuests") != null) minG = ((Long) tourData.get("minGuests")).intValue();
            if (tourData.get("maxGuests") != null) maxG = ((Long) tourData.get("maxGuests")).intValue();

            List<String> tourStartDates = new ArrayList<>();
            Object startDateObj = tourData.get("startDate");
            if (startDateObj instanceof String) {
                String sdStr = (String) startDateObj;
                if (!sdStr.isEmpty()) {
                    tourStartDates = Arrays.stream(sdStr.split(","))
                                         .map(String::trim)
                                         .collect(Collectors.toList());
                }
            } else if (startDateObj instanceof List) {
                tourStartDates = (List<String>) startDateObj;
            }

            Map<String, GroupedTrip> groupedTrips = new LinkedHashMap<>();
            LocalDate today = LocalDate.now();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (String sd : tourStartDates) {
                String key = sd + "_preparing";
                GroupedTrip gt = new GroupedTrip();
                gt.setTourId(tourId);
                gt.setStartDate(sd);
                gt.setTripStatus("preparing");
                gt.setTour(tourData);
                gt.setMinGuests(minG);
                gt.setMaxGuests(maxG);
                gt.setHasBookings(false);
                groupedTrips.put(key, gt);
            }

            ApiFuture<QuerySnapshot> bookingsFuture = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .get();
                    
            for (QueryDocumentSnapshot doc : bookingsFuture.get().getDocuments()) {
                Booking b = doc.toObject(Booking.class);
                b.setId(doc.getId());
                String tripStatus = doc.getString("tripStatus");
                if (tripStatus == null) tripStatus = "preparing";
                
                String key = b.getStartDate() + "_" + tripStatus;
                GroupedTrip trip = groupedTrips.get(key);
                
                if (trip == null) {
                    trip = new GroupedTrip();
                    trip.setId(b.getId());
                    trip.setTourId(tourId);
                    trip.setStartDate(b.getStartDate());
                    trip.setTripStatus(tripStatus);
                    trip.setTour(tourData);
                    trip.setMinGuests(minG);
                    trip.setMaxGuests(maxG);
                    groupedTrips.put(key, trip);
                } else if (trip.getId() == null) {
                    trip.setId(b.getId());
                }
                
                trip.setHasBookings(true);
                trip.getBookings().add(b);
                trip.setBookingCount(trip.getBookingCount() + 1);
                
                int passengers = (b.getAdults() != null ? b.getAdults() : 0) + 
                                 (b.getChildren() != null ? b.getChildren() : 0) + 
                                 (b.getInfants() != null ? b.getInfants() : 0);
                                 
                if (!"CANCELLED".equals(b.getStatus())) {
                    trip.setTotalPassengers(trip.getTotalPassengers() + passengers);
                }
                if ("CONFIRMED".equals(b.getStatus())) {
                    trip.setConfirmedCount(trip.getConfirmedCount() + 1);
                    trip.setConfirmedPassengers(trip.getConfirmedPassengers() + passengers);
                    trip.setTotalRevenue(trip.getTotalRevenue() + b.getTotalPrice());
                }
                
                // --- Cập nhật guideCount cho trip ---
                List<String> gIds = (List<String>) doc.get("guideIds");
                if (gIds != null) {
                    trip.setGuideCount(Math.max(trip.getGuideCount(), gIds.size()));
                }
            }

            // --- Logic mới: Ẩn tour trống nếu đã có chuyến đi thực tế cho cùng ngày ---
            List<String> datesWithActualTrips = new ArrayList<>();
            for (GroupedTrip trip : groupedTrips.values()) {
                if (trip.isHasBookings() || !"preparing".equals(trip.getTripStatus())) {
                    datesWithActualTrips.add(trip.getStartDate());
                }
            }
            
            groupedTrips.entrySet().removeIf(entry -> {
                GroupedTrip trip = entry.getValue();
                return "preparing".equals(trip.getTripStatus()) && 
                       !trip.isHasBookings() && 
                       datesWithActualTrips.contains(trip.getStartDate());
            });
            // --------------------------------------------------------------------------

            for (GroupedTrip trip : groupedTrips.values()) {
                if ("preparing".equals(trip.getTripStatus())) {
                    try {
                        LocalDate start = LocalDate.parse(trip.getStartDate(), dtf);
                        long daysUntil = ChronoUnit.DAYS.between(today, start);
                        if (daysUntil <= 2 && trip.getTotalPassengers() < trip.getMinGuests() && trip.isHasBookings()) {
                            trip.setCanCancelBatch(true);
                        }
                    } catch (Exception e) {}
                }
            }

            List<GroupedTrip> trips = new ArrayList<>(groupedTrips.values());
            trips.sort((t1, t2) -> {
                try {
                    LocalDate d1 = LocalDate.parse(t1.getStartDate(), dtf);
                    LocalDate d2 = LocalDate.parse(t2.getStartDate(), dtf);
                    return d1.compareTo(d2);
                } catch (Exception e) { return 0; }
            });
            model.addAttribute("trips", trips);

        } catch (Exception e) { e.printStackTrace(); }
        return "bookings/tour_trips";
    }

    @GetMapping("/trip-schedule/{id}")
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
                    if (!"CANCELLED".equals(bookingStatus)) {
                        totalPassengers += (b.getAdults() != null ? b.getAdults() : 0);
                        totalPassengers += (b.getChildren() != null ? b.getChildren() : 0);
                        totalPassengers += (b.getInfants() != null ? b.getInfants() : 0);
                    }
                    if ("CONFIRMED".equals(bookingStatus)) {
                        totalRevenue += b.getTotalPrice();
                    }
                }
            }

            ApiFuture<QuerySnapshot> guidesFuture = firestore.collection("users").whereEqualTo("role", "guide").get();
            ApiFuture<QuerySnapshot> allBookingsFuture = firestore.collection("bookings").whereEqualTo("status", "CONFIRMED").get();
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
                        if (!booking.getTourId().equals(ab.getString("tourId")) || !booking.getStartDate().equals(ab.getString("startDate")) || !currentTripStatus.equals(abTripStatus)) {
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
            trip.setGuideCount(gIds != null ? gIds.size() : 0);
            
            if (booking.getTour() != null) {
                Object minG = booking.getTour().get("minGuests");
                Object maxG = booking.getTour().get("maxGuests");
                trip.setMinGuests(minG != null ? ((Long) minG).intValue() : 1);
                trip.setMaxGuests(maxG != null ? ((Long) maxG).intValue() : 50);
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
    public Map<String, Object> updateTripStatus(@RequestParam String bookingId, @RequestParam String tripStatus, @RequestParam(required = false) String startTime, @RequestParam(required = false) String endTime, @RequestParam(required = false) String tripNote) {
        Map<String, Object> response = new HashMap<>();
        try {
            DocumentSnapshot doc = firestore.collection("bookings").document(bookingId).get().get();
            if (!doc.exists()) { response.put("success", false); response.put("message", "Không tìm thấy đơn hàng"); return response; }
            String tourId = doc.getString("tourId");
            String startDate = doc.getString("startDate");
            String currentTripStatus = doc.getString("tripStatus");
            if (currentTripStatus == null) currentTripStatus = "preparing";
            if ("started".equals(tripStatus)) {
                DocumentSnapshot tourDoc = firestore.collection("tours").document(tourId).get().get();
                int minG = 1; int maxG = 50;
                if (tourDoc.exists()) { Long min = tourDoc.getLong("minGuests"); Long max = tourDoc.getLong("maxGuests"); if (min != null) minG = min.intValue(); if (max != null) maxG = max.intValue(); }
                QuerySnapshot tripBookings = firestore.collection("bookings").whereEqualTo("tourId", tourId).whereEqualTo("startDate", startDate).whereEqualTo("status", "CONFIRMED").get().get();
                int totalPassengers = 0;
                for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                    String s = d.getString("tripStatus"); if (s == null) s = "preparing";
                    if (s.equals(currentTripStatus)) { totalPassengers += (d.getLong("adults") != null ? d.getLong("adults").intValue() : 0); totalPassengers += (d.getLong("children") != null ? d.getLong("children").intValue() : 0); totalPassengers += (d.getLong("infants") != null ? d.getLong("infants").intValue() : 0); }
                }
                if (totalPassengers < minG) { response.put("success", false); response.put("message", "Chưa đạt tối thiểu " + minG); return response; }
            }
            QuerySnapshot tripBookings = firestore.collection("bookings").whereEqualTo("tourId", tourId).whereEqualTo("startDate", startDate).get().get();
            Map<String, Object> updates = new HashMap<>();
            updates.put("tripStatus", tripStatus);
            if (startTime != null && !startTime.isEmpty()) updates.put("startTime", startTime);
            if (endTime != null && !endTime.isEmpty()) updates.put("endTime", endTime);
            if (tripNote != null && !tripNote.isEmpty()) updates.put("tripNote", tripNote);
            for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                String s = d.getString("tripStatus"); if (s == null) s = "preparing";
                if (s.equals(currentTripStatus)) firestore.collection("bookings").document(d.getId()).update(updates).get();
            }
            response.put("success", true);
        } catch (Exception e) { e.printStackTrace(); response.put("success", false); }
        return response;
    }

    @PostMapping("/trip-batch-cancel")
    public String batchCancelTrip(@RequestParam String tourId, @RequestParam String startDate, RedirectAttributes redirectAttributes) {
        try {
            QuerySnapshot tripBookings = firestore.collection("bookings").whereEqualTo("tourId", tourId).whereEqualTo("startDate", startDate).get().get();
            for (QueryDocumentSnapshot doc : tripBookings.getDocuments()) {
                firestore.collection("bookings").document(doc.getId()).update("status", "CANCELLED", "tripStatus", "cancelled").get();
            }
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy chuyến đi thành công.");
        } catch (Exception e) { 
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi hủy chuyến đi.");
        }
        return "redirect:/admin/bookings/tour-trips/" + tourId;
    }

    @PostMapping("/trip-schedule/assign-guide")
    @ResponseBody
    public Map<String, Object> assignGuide(@RequestParam String bookingId, @RequestParam String guideId, @RequestParam(required = false) String action) {
        Map<String, Object> response = new HashMap<>();
        try {
            DocumentSnapshot doc = firestore.collection("bookings").document(bookingId).get().get();
            if (!doc.exists()) { response.put("success", false); return response; }
            String tourId = doc.getString("tourId");
            String startDate = doc.getString("startDate");
            String currentTripStatus = doc.getString("tripStatus");
            if (currentTripStatus == null) currentTripStatus = "preparing";
            
            List<String> guideIds = (List<String>) doc.get("guideIds");
            if (guideIds == null) guideIds = new ArrayList<>(); else guideIds = new ArrayList<>(guideIds);
            
            if ("remove".equals(action)) {
                guideIds.remove(guideId);
            } else if (guideId != null && !guideId.isEmpty() && !guideIds.contains(guideId)) {
                guideIds.add(guideId);
            }
            
            QuerySnapshot tripBookings = firestore.collection("bookings")
                    .whereEqualTo("tourId", tourId)
                    .whereEqualTo("startDate", startDate)
                    .get().get();
                    
            for (QueryDocumentSnapshot d : tripBookings.getDocuments()) {
                String s = d.getString("tripStatus"); 
                if (s == null) s = "preparing";
                if (s.equals(currentTripStatus)) {
                    firestore.collection("bookings").document(d.getId()).update("guideIds", guideIds).get();
                }
            }
            
            response.put("success", true);
            response.put("guideCount", guideIds.size());
        } catch (Exception e) { e.printStackTrace(); response.put("success", false); }
        return response;
    }

    @GetMapping("/delete/{id}")
    public String deleteBooking(@PathVariable String id) { firestore.collection("bookings").document(id).delete(); return "redirect:/admin/bookings"; }

    @GetMapping("/trip-delete/{tourId}/{startDate}")
    public String deleteTrip(@PathVariable String tourId, @PathVariable String startDate, RedirectAttributes redirectAttributes) {
        try {
            QuerySnapshot tripBookings = firestore.collection("bookings").whereEqualTo("tourId", tourId).whereEqualTo("startDate", startDate).get().get();
            for (QueryDocumentSnapshot doc : tripBookings.getDocuments()) { firestore.collection("bookings").document(doc.getId()).delete(); }
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa toàn bộ thông tin chuyến đi.");
        } catch (Exception e) { e.printStackTrace(); }
        return "redirect:/admin/bookings/tour-trips/" + tourId;
    }
}
