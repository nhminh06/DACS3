package com.example.dacs3.admin.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tours")
public class AdminTourController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping
    public String listTours(Model model, 
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String sort) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("tours").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            long total = documents.size();
            long active = documents.stream().filter(d -> {
                Object status = d.get("trang_thai");
                return status == null || "active".equals(status) || Integer.valueOf(1).equals(status);
            }).count();
            
            long hidden = total - active;
            
            double avgPrice = documents.stream()
                    .mapToLong(d -> {
                        Object p = d.get("price");
                        if (p instanceof Long) return (Long) p;
                        if (p instanceof Integer) return ((Integer) p).longValue();
                        if (p instanceof String) {
                            try { return Long.parseLong((String)p); } catch(Exception e) {}
                        }
                        return 0L;
                    })
                    .average().orElse(0.0);

            model.addAttribute("total", total);
            model.addAttribute("activeCount", active);
            model.addAttribute("hiddenCount", hidden);
            model.addAttribute("avgPrice", avgPrice);

            List<QueryDocumentSnapshot> filteredTours = new ArrayList<>(documents);
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredTours = filteredTours.stream()
                        .filter(d -> {
                            String title = d.getString("title");
                            return title != null && title.toLowerCase().contains(searchLower);
                        })
                        .collect(Collectors.toList());
            }

            if (sort != null) {
                switch (sort) {
                    case "name":
                        filteredTours.sort(Comparator.comparing(d -> {
                            String t = d.getString("title");
                            return t != null ? t : "";
                        }));
                        break;
                    case "price_asc":
                        filteredTours.sort(Comparator.comparingLong(d -> {
                            Object p = d.get("price");
                            if (p instanceof Long) return (Long) p;
                            if (p instanceof Integer) return ((Integer) p).longValue();
                            if (p instanceof String) {
                                try { return Long.parseLong((String)p); } catch(Exception e) {}
                            }
                            return 0L;
                        }));
                        break;
                    case "price_desc":
                        filteredTours.sort((d1, d2) -> {
                            Long p1 = 0L;
                            Object obj1 = d1.get("price");
                            if (obj1 instanceof Long) p1 = (Long) obj1;
                            else if (obj1 instanceof Integer) p1 = ((Integer) obj1).longValue();
                            else if (obj1 instanceof String) { try { p1 = Long.parseLong((String)obj1); } catch(Exception e) {} }

                            Long p2 = 0L;
                            Object obj2 = d2.get("price");
                            if (obj2 instanceof Long) p2 = (Long) obj2;
                            else if (obj2 instanceof Integer) p2 = ((Integer) obj2).longValue();
                            else if (obj2 instanceof String) { try { p2 = Long.parseLong((String)obj2); } catch(Exception e) {} }

                            return Long.compare(p2, p1);
                        });
                        break;
                }
            }

            model.addAttribute("tours", filteredTours);
            model.addAttribute("search", search);
            model.addAttribute("sort", sort);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "tours/list";
    }

    @GetMapping("/add")
    public String addTourForm(Model model) {
        model.addAttribute("tour", new HashMap<>());
        model.addAttribute("tourId", null);
        return "tours/form";
    }

    @GetMapping("/edit/{id}")
    public String editTourForm(@PathVariable String id, Model model) {
        try {
            var doc = firestore.collection("tours").document(id).get().get();
            if (doc.exists()) {
                model.addAttribute("tour", doc.getData());
                model.addAttribute("tourId", id);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "tours/form";
    }

    @PostMapping("/save")
    public String saveTour(@RequestParam(required = false) String id,
                           @RequestParam String maTour,
                           @RequestParam String tenTour,
                           @RequestParam String loaiTour,
                           @RequestParam String ngayKhoiHanh,
                           @RequestParam String diemKhoiHanh,
                           @RequestParam Integer soNgay,
                           @RequestParam String giaNguoiLon,
                           @RequestParam(required = false) String giaTreEm,
                           @RequestParam(required = false) String giaTreNho,
                           @RequestParam String vitri,
                           @RequestParam(required = false) String anhDaiDien,
                           @RequestParam(required = false) MultipartFile mainImageFile,
                           @RequestParam(required = false) MultipartFile[] bannerImageFiles,
                           @RequestParam String dichVu,
                           @RequestParam String loTrinh,
                           @RequestParam String traiNghiem) throws IOException, ExecutionException, InterruptedException {
        
        Map<String, Object> data = new HashMap<>();

        if (id != null && !id.isEmpty()) {
            try {
                var oldDoc = firestore.collection("tours").document(id).get().get();
                if (oldDoc.exists()) {
                    data.putAll(oldDoc.getData());
                }
            } catch (Exception e) {}
        }

        String finalMainImageUrl = anhDaiDien;
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(mainImageFile.getBytes(), ObjectUtils.emptyMap());
            finalMainImageUrl = (String) uploadResult.get("secure_url");
        }

        List<String> bannerUrls = (List<String>) data.getOrDefault("banners", new ArrayList<>());
        if (bannerImageFiles != null && bannerImageFiles.length > 0) {
            for (MultipartFile file : bannerImageFiles) {
                if (!file.isEmpty()) {
                    Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                    bannerUrls.add((String) uploadResult.get("secure_url"));
                }
            }
        }
        if (bannerUrls.size() > 5) {
            bannerUrls = bannerUrls.subList(bannerUrls.size() - 5, bannerUrls.size());
        }

        data.put("maTour", maTour);
        data.put("title", tenTour);
        data.put("type", "1".equals(loaiTour) ? "DAY_TOUR" : "MULTI_DAY");
        data.put("startDate", ngayKhoiHanh);
        data.put("diemKhoiHanh", diemKhoiHanh);
        data.put("duration", soNgay + " ngày");
        
        // Lưu giá theo kiểu Number (Long) để Firestore có thể sắp xếp và tính toán
        data.put("price", parsePriceToLong(giaNguoiLon));
        data.put("giaTreEm", parsePriceToLong(giaTreEm));
        data.put("giaTreNho", parsePriceToLong(giaTreNho));
        
        data.put("location", vitri);
        data.put("imageUrl", finalMainImageUrl); 
        data.put("banners", bannerUrls);
        data.put("dichVu", dichVu);
        data.put("loTrinh", loTrinh);
        data.put("traiNghiem", traiNghiem);
        data.put("trang_thai", data.getOrDefault("trang_thai", "active"));
        data.put("rating", data.getOrDefault("rating", 5.0));
        data.put("reviewCount", data.getOrDefault("reviewCount", 0));

        if (id == null || id.isEmpty()) {
            firestore.collection("tours").add(data).get();
        } else {
            firestore.collection("tours").document(id).set(data).get();
        }
        return "redirect:/admin/tours";
    }

    private Long parsePriceToLong(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0L;
        try {
            // Loại bỏ các ký tự không phải số (như dấu chấm, dấu phẩy nếu người dùng nhập nhầm)
            String cleanPrice = priceStr.replaceAll("[^0-9]", "");
            return Long.parseLong(cleanPrice);
        } catch (Exception e) {
            return 0L;
        }
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection("tours").document(id);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            String currentStatus = snapshot.getString("trang_thai");
            String newStatus = "active".equals(currentStatus) ? "hidden" : "active";
            docRef.update("trang_thai", newStatus).get();
        }
        return "redirect:/admin/tours";
    }

    @GetMapping("/delete/{id}")
    public String deleteTour(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("tours").document(id).delete().get();
        return "redirect:/admin/tours";
    }
}
