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
                            @RequestParam(required = false) String sort,
                            @RequestParam(defaultValue = "1") int page) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("tours").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            long totalCount = documents.size();
            long activeCount = documents.stream().filter(d -> {
                Object status = d.get("trang_thai");
                return status == null || "active".equals(status);
            }).count();
            
            long hiddenCount = totalCount - activeCount;
            
            double avgPrice = documents.stream()
                    .mapToLong(d -> getPriceAsLong(d.get("price")))
                    .average().orElse(0.0);

            model.addAttribute("total", totalCount);
            model.addAttribute("activeCount", activeCount);
            model.addAttribute("hiddenCount", hiddenCount);
            model.addAttribute("avgPrice", avgPrice);

            List<Map<String, Object>> tourList = documents.stream().map(d -> {
                Map<String, Object> map = new HashMap<>(d.getData());
                map.put("id", d.getId());
                return map;
            }).collect(Collectors.toList());

            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                tourList = tourList.stream()
                        .filter(t -> {
                            String title = (String) t.get("title");
                            return title != null && title.toLowerCase().contains(searchLower);
                        })
                        .collect(Collectors.toList());
            }

            if (sort != null) {
                switch (sort) {
                    case "name":
                        tourList.sort(Comparator.comparing(t -> (String) t.getOrDefault("title", "")));
                        break;
                    case "price_asc":
                        tourList.sort(Comparator.comparingLong(t -> getPriceAsLong(t.get("price"))));
                        break;
                    case "price_desc":
                        tourList.sort((t1, t2) -> Long.compare(getPriceAsLong(t2.get("price")), getPriceAsLong(t1.get("price"))));
                        break;
                    default:
                        break;
                }
            }

            int pageSize = 10; 
            int totalFiltered = tourList.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);
            
            model.addAttribute("tours", (start < totalFiltered) ? tourList.subList(start, end) : new ArrayList<>());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalFiltered", totalFiltered);
            model.addAttribute("search", search);
            model.addAttribute("sort", sort);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "tours/list";
    }

    private Long getPriceAsLong(Object p) {
        if (p instanceof Long) return (Long) p;
        if (p instanceof Integer) return ((Integer) p).longValue();
        if (p instanceof String) {
            try { return Long.parseLong(((String)p).replaceAll("[^0-9]", "")); } catch(Exception e) {}
        }
        return 0L;
    }

    @GetMapping("/add")
    public String addTourForm(Model model) {
        model.addAttribute("tour", new HashMap<String, Object>());
        model.addAttribute("tourId", null);
        return "tours/form";
    }

    @GetMapping("/edit/{id}")
    public String editTourForm(@PathVariable String id, @RequestParam(required = false) Boolean promo, Model model) {
        Map<String, Object> tourData = new HashMap<>();
        String tourId = null;
        try {
            var doc = firestore.collection("tours").document(id).get().get();
            if (doc.exists()) {
                tourData = new HashMap<>(doc.getData());
                if (Boolean.TRUE.equals(promo)) {
                    tourData.put("isOffer", true);
                }
                tourId = id;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        model.addAttribute("tour", tourData);
        model.addAttribute("tourId", tourId);
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
                           @RequestParam String dichVu,
                           @RequestParam String loTrinh,
                           @RequestParam String traiNghiem,
                           @RequestParam(defaultValue = "1") Integer minGuests,
                           @RequestParam(defaultValue = "50") Integer maxGuests,
                           @RequestParam(required = false) String tourScale,
                           @RequestParam(required = false) Boolean isOffer,
                           @RequestParam(required = false) String originalPrice,
                           @RequestParam(required = false) String originalPriceChild,
                           @RequestParam(required = false) String originalPriceInfant,
                           @RequestParam(required = false) String discountTag,
                           @RequestParam(required = false) String timeLeft,
                           @RequestParam(required = false) String offerImageUrl,
                           @RequestParam(required = false) MultipartFile offerImageFile) throws IOException, ExecutionException, InterruptedException {
        
        Map<String, Object> data = new HashMap<>();
        if (id != null && !id.isEmpty()) {
            var oldDoc = firestore.collection("tours").document(id).get().get();
            if (oldDoc.exists()) data.putAll(oldDoc.getData());
        }

        String finalMainImageUrl = anhDaiDien;
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(mainImageFile.getBytes(), ObjectUtils.emptyMap());
            finalMainImageUrl = (String) uploadResult.get("secure_url");
        }

        String finalOfferImageUrl = offerImageUrl;
        if (offerImageFile != null && !offerImageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(offerImageFile.getBytes(), ObjectUtils.emptyMap());
            finalOfferImageUrl = (String) uploadResult.get("secure_url");
        }

        data.put("maTour", maTour);
        data.put("title", tenTour);
        data.put("type", "1".equals(loaiTour) ? "DAY_TOUR" : "MULTI_DAY");
        data.put("startDate", ngayKhoiHanh);
        data.put("diemKhoiHanh", diemKhoiHanh);
        data.put("duration", soNgay + " ngày");
        data.put("price", parsePriceToLong(giaNguoiLon));
        data.put("giaTreEm", parsePriceToLong(giaTreEm));
        data.put("giaTreNho", parsePriceToLong(giaTreNho));
        data.put("location", vitri);
        data.put("imageUrl", finalMainImageUrl); 
        data.put("dichVu", dichVu);
        data.put("loTrinh", loTrinh);
        data.put("traiNghiem", traiNghiem);
        data.put("minGuests", minGuests);
        data.put("maxGuests", maxGuests);
        data.put("scale", tourScale);
        
        data.put("isOffer", isOffer != null && isOffer);
        data.put("originalPrice", parsePriceToLong(originalPrice));
        data.put("originalPriceChild", parsePriceToLong(originalPriceChild));
        data.put("originalPriceInfant", parsePriceToLong(originalPriceInfant));
        data.put("discountTag", discountTag);
        data.put("timeLeft", (timeLeft == null || timeLeft.isEmpty()) ? "00:00:00" : timeLeft);
        data.put("offerImageUrl", finalOfferImageUrl);
        data.put("trang_thai", data.getOrDefault("trang_thai", "active"));

        if (id == null || id.isEmpty()) {
            firestore.collection("tours").add(data).get();
        } else {
            firestore.collection("tours").document(id).set(data).get();
        }
        return "redirect:/admin/tours";
    }

    private Long parsePriceToLong(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) return 0L;
        try { return Long.parseLong(priceStr.replaceAll("[^0-9]", "")); } catch (Exception e) { return 0L; }
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection("tours").document(id);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            String currentStatus = snapshot.getString("trang_thai");
            String newStatus = "hidden".equals(currentStatus) ? "active" : "hidden";
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
