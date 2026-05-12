package com.example.dacs3.admin.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin/banners")
public class AdminBannerController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping
    public String listBanners(Model model) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("banners")
                    .orderBy("order", Query.Direction.ASCENDING).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            List<Map<String, Object>> banners = new ArrayList<>();
            for (QueryDocumentSnapshot document : documents) {
                Map<String, Object> banner = new HashMap<>(document.getData());
                banner.put("id", document.getId());
                banners.add(banner);
            }
            model.addAttribute("banners", banners);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            model.addAttribute("banners", new ArrayList<>());
        }
        return "banners/list";
    }

    @PostMapping("/save")
    public String saveBanner(@RequestParam(required = false) String id,
                             @RequestParam(required = false, defaultValue = "Banner") String title,
                             @RequestParam(required = false, defaultValue = "") String subtitle,
                             @RequestParam(required = false, defaultValue = "") String link,
                             @RequestParam(required = false, defaultValue = "0") Integer order,
                             @RequestParam(required = false) MultipartFile imageFile,
                             @RequestParam(required = false) String imageUrl) throws IOException, ExecutionException, InterruptedException {

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("subtitle", subtitle);
        data.put("link", link);
        data.put("order", order);
        data.put("status", "active");

        String finalImageUrl = imageUrl;
        if (imageFile != null && !imageFile.isEmpty()) {
            Map uploadResult = cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            finalImageUrl = (String) uploadResult.get("secure_url");
        }
        data.put("imageUrl", finalImageUrl);

        if (id == null || id.isEmpty()) {
            firestore.collection("banners").add(data).get();
        } else {
            firestore.collection("banners").document(id).set(data).get();
        }
        return "redirect:/admin/banners";
    }

    @GetMapping("/delete/{id}")
    public String deleteBanner(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("banners").document(id).delete().get();
        return "redirect:/admin/banners";
    }
}
