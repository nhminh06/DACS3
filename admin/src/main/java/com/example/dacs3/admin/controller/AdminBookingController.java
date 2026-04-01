package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    @Autowired
    private Firestore firestore;

    @GetMapping
    public String listBookings(Model model, @RequestParam(required = false) String status) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("bookings").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            // Thống kê
            long total = documents.size();
            long confirmedCount = documents.stream().filter(d -> "CONFIRMED".equals(d.getString("status"))).count();
            long pendingCount = documents.stream().filter(d -> "PENDING".equals(d.getString("status"))).count();
            long cancelledCount = documents.stream().filter(d -> "CANCELLED".equals(d.getString("status"))).count();

            model.addAttribute("totalCount", total);
            model.addAttribute("confirmedCount", confirmedCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("cancelledCount", cancelledCount);

            // Lọc theo trạng thái
            List<QueryDocumentSnapshot> filteredBookings = documents;
            if (status != null && !status.isEmpty()) {
                filteredBookings = documents.stream()
                        .filter(d -> status.equals(d.getString("status")))
                        .collect(Collectors.toList());
            }

            model.addAttribute("bookings", filteredBookings);
            model.addAttribute("statusFilter", status);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "bookings/list";
    }

    @GetMapping("/confirm/{id}")
    public String confirmBooking(@PathVariable String id) {
        firestore.collection("bookings").document(id).update("status", "CONFIRMED");
        return "redirect:/admin/bookings";
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
}
