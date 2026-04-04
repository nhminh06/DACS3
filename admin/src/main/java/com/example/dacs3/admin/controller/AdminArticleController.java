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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/articles")
public class AdminArticleController {

    @Autowired
    private Firestore firestore;

    @Autowired
    private Cloudinary cloudinary;

    @GetMapping
    public String listArticles(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer loai,
                               @RequestParam(required = false, defaultValue = "newest") String sort) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("articles").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            long total = documents.size();
            
            // Thống kê dựa trên loai_id (1: Làng nghề, 2: Ẩm thực, 3: Văn hóa)
            // Ép kiểu an toàn để tránh lỗi so sánh
            long langNhe = documents.stream().filter(d -> isTypeMatch(d.get("loai_id"), 1)).count();
            long amThuc = documents.stream().filter(d -> isTypeMatch(d.get("loai_id"), 2)).count();
            long vanHoa = documents.stream().filter(d -> isTypeMatch(d.get("loai_id"), 3)).count();

            model.addAttribute("total", total);
            model.addAttribute("langNheCount", langNhe);
            model.addAttribute("amThucCount", amThuc);
            model.addAttribute("vanHoaCount", vanHoa);

            List<QueryDocumentSnapshot> filteredArticles = new ArrayList<>(documents);

            // Filter by Search
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredArticles = filteredArticles.stream()
                        .filter(d -> {
                            String title = d.getString("tieu_de");
                            return title != null && title.toLowerCase().contains(searchLower);
                        })
                        .collect(Collectors.toList());
            }

            // Filter by Category
            if (loai != null && loai > 0) {
                filteredArticles = filteredArticles.stream()
                        .filter(d -> isTypeMatch(d.get("loai_id"), loai))
                        .collect(Collectors.toList());
            }

            // Sorting
            if (sort != null) {
                switch (sort) {
                    case "name":
                        filteredArticles.sort(Comparator.comparing(d -> {
                            String t = d.getString("tieu_de");
                            return t != null ? t : "";
                        }));
                        break;
                    case "oldest":
                        filteredArticles.sort(Comparator.comparing(d -> {
                            Object date = d.get("ngay_tao");
                            return date != null ? date.toString() : "";
                        }));
                        break;
                    case "newest":
                    default:
                        filteredArticles.sort((d1, d2) -> {
                            Object date1 = d1.get("ngay_tao");
                            Object date2 = d2.get("ngay_tao");
                            String s1 = (date1 != null) ? date1.toString() : "";
                            String s2 = (date2 != null) ? date2.toString() : "";
                            return s2.compareTo(s1);
                        });
                        break;
                }
            }

            List<Map<String, Object>> articleList = filteredArticles.stream().map(d -> {
                Map<String, Object> map = new HashMap<>(d.getData());
                map.put("id", d.getId());
                return map;
            }).collect(Collectors.toList());

            model.addAttribute("articles", articleList);
            model.addAttribute("search", search);
            model.addAttribute("loai", loai);
            model.addAttribute("sort", sort);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "articles/list";
    }

    // Hàm phụ trợ so sánh loai_id an toàn
    private boolean isTypeMatch(Object val, Integer target) {
        if (val == null) return false;
        try {
            return Integer.valueOf(val.toString()).equals(target);
        } catch (Exception e) {
            return false;
        }
    }

    @GetMapping("/add")
    public String addArticleForm(Model model) {
        try {
            ApiFuture<QuerySnapshot> tourFuture = firestore.collection("tours").get();
            List<Map<String, Object>> tourList = tourFuture.get().getDocuments().stream().map(d -> {
                Map<String, Object> map = new HashMap<>(d.getData());
                map.put("id", d.getId());
                return map;
            }).collect(Collectors.toList());
            model.addAttribute("tours", tourList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "articles/form";
    }

    @GetMapping("/edit/{id}")
    public String editArticleForm(@PathVariable String id, Model model) {
        try {
            var doc = firestore.collection("articles").document(id).get().get();
            if (doc.exists()) {
                Map<String, Object> articleData = new HashMap<>(doc.getData());
                articleData.put("id", doc.getId());
                model.addAttribute("article", articleData);
                
                ApiFuture<QuerySnapshot> tourFuture = firestore.collection("tours").get();
                List<Map<String, Object>> tourList = tourFuture.get().getDocuments().stream().map(d -> {
                    Map<String, Object> map = new HashMap<>(d.getData());
                    map.put("id", d.getId());
                    return map;
                }).collect(Collectors.toList());
                model.addAttribute("tours", tourList);
                return "articles/form";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/articles";
    }

    @PostMapping("/save")
    public String saveArticle(@RequestParam(required = false) String id,
                              @RequestParam String tieu_de,
                              @RequestParam Integer loai_id,
                              @RequestParam(required = false) String tour_id,
                              @RequestParam("tieu_de_muc[]") String[] tieuDeMuc,
                              @RequestParam("noi_dung_muc[]") String[] noiDungMuc,
                              @RequestParam(value = "hinh_anh_muc[]", required = false) MultipartFile[] hinhAnhMuc,
                              @RequestParam(value = "hinh_anh_cu[]", required = false) String[] hinhAnhCu) throws IOException, ExecutionException, InterruptedException {

        Map<String, Object> data = new HashMap<>();
        data.put("tieu_de", tieu_de);
        data.put("loai_id", loai_id);
        data.put("tour_id", tour_id != null && !tour_id.equals("0") ? tour_id : null);
        
        if (id == null || id.isEmpty()) {
            data.put("ngay_tao", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            data.put("trang_thai", 1);
        }

        List<Map<String, Object>> mucList = new ArrayList<>();
        for (int i = 0; i < tieuDeMuc.length; i++) {
            Map<String, Object> muc = new HashMap<>();
            muc.put("tieu_de", tieuDeMuc[i]);
            muc.put("noi_dung", noiDungMuc[i]);
            
            String currentImageUrl = (hinhAnhCu != null && i < hinhAnhCu.length) ? hinhAnhCu[i] : null;

            if (hinhAnhMuc != null && i < hinhAnhMuc.length && !hinhAnhMuc[i].isEmpty()) {
                Map uploadResult = cloudinary.uploader().upload(hinhAnhMuc[i].getBytes(), ObjectUtils.emptyMap());
                muc.put("hinh_anh", uploadResult.get("secure_url"));
            } else {
                muc.put("hinh_anh", currentImageUrl);
            }
            
            mucList.add(muc);
        }
        data.put("sections", mucList);
        data.put("so_muc", mucList.size());

        if (id == null || id.isEmpty()) {
            firestore.collection("articles").add(data).get();
        } else {
            firestore.collection("articles").document(id).update(data).get();
        }

        return "redirect:/admin/articles";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable String id) throws ExecutionException, InterruptedException {
        var docRef = firestore.collection("articles").document(id);
        var snapshot = docRef.get().get();
        if (snapshot.exists()) {
            Object currentStatus = snapshot.get("trang_thai");
            int newStatus = (currentStatus != null && currentStatus.toString().equals("1")) ? 0 : 1;
            docRef.update("trang_thai", newStatus).get();
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("articles").document(id).delete().get();
        return "redirect:/admin/articles";
    }
}
