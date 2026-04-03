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
        return "bookings/list";
    }

    @GetMapping("/schedule")
    public String manageSchedule(Model model) {
        return "bookings/schedule";
    }

    @GetMapping("/detail")
    public String tripDetail(Model model) {
        return "bookings/detail";
    }

    @GetMapping("/booking-detail")
    public String bookingDetail(Model model) {
        return "bookings/booking_detail";
    }

    @GetMapping("/trip-schedule")
    public String tripSchedule(Model model) {
        return "bookings/trip_schedule";
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
