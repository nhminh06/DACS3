package com.example.dacs3.admin.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.dacs3.admin.service.HuggingFaceService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    private HuggingFaceService huggingFaceService;

    @GetMapping
    public String listArticles(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Integer loai,
                               @RequestParam(required = false, defaultValue = "newest") String sort,
                               @RequestParam(defaultValue = "1") int page) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("articles").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            long total = documents.size();
            long langNhe = documents.stream().filter(d -> isTypeMatch(d.get("loai_id"), 1)).count();
            long amThuc = documents.stream().filter(d -> isTypeMatch(d.get("loai_id"), 2)).count();
            long vanHoa = documents.stream().filter(d -> isTypeMatch(d.get("loai_id"), 3)).count();

            model.addAttribute("total", total);
            model.addAttribute("langNheCount", langNhe);
            model.addAttribute("amThucCount", amThuc);
            model.addAttribute("vanHoaCount", vanHoa);

            List<QueryDocumentSnapshot> filteredArticles = new ArrayList<>(documents);

            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                filteredArticles = filteredArticles.stream()
                        .filter(d -> {
                            String title = d.getString("tieu_de");
                            return title != null && title.toLowerCase().contains(searchLower);
                        })
                        .collect(Collectors.toList());
            }

            if (loai != null && loai > 0) {
                filteredArticles = filteredArticles.stream()
                        .filter(d -> isTypeMatch(d.get("loai_id"), loai))
                        .collect(Collectors.toList());
            }

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

            int pageSize = 6;
            int totalFiltered = filteredArticles.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);

            List<Map<String, Object>> articleList = filteredArticles.subList(start, end).stream().map(d -> {
                Map<String, Object> map = new HashMap<>(d.getData());
                map.put("id", d.getId());
                return map;
            }).collect(Collectors.toList());

            model.addAttribute("articles", articleList);
            model.addAttribute("search", search);
            model.addAttribute("loai", loai);
            model.addAttribute("sort", sort);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalFiltered", totalFiltered);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "articles/list";
    }

    @GetMapping("/view/{id}")
    public String viewArticle(@PathVariable String id, Model model) {
        try {
            var doc = firestore.collection("articles").document(id).get().get();
            if (doc.exists()) {
                Map<String, Object> articleData = new HashMap<>(doc.getData());
                articleData.put("id", doc.getId());
                ensureSectionsExist(articleData);
                model.addAttribute("article", articleData);
                return "articles/detail";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/articles";
    }

    @GetMapping("/ai-review/{id}")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> getAiReview(@PathVariable String id) {
        try {
            var doc = firestore.collection("articles").document(id).get().get();
            if (doc.exists()) {
                Map<String, Object> articleData = doc.getData();
                String title = (String) articleData.getOrDefault("tieu_de", "Không có tiêu đề");
                List<Map<String, Object>> sections = (List<Map<String, Object>>) articleData.get("sections");
                
                StringBuilder fullContent = new StringBuilder();
                if (sections != null) {
                    for (Map<String, Object> section : sections) {
                        String sectionTitle = (String) section.get("tieu_de");
                        String sectionContent = (String) section.get("noi_dung");
                        
                        if (sectionTitle != null && !sectionTitle.isEmpty()) {
                            fullContent.append(sectionTitle).append(". ");
                        }
                        if (sectionContent != null && !sectionContent.isEmpty()) {
                            fullContent.append(sectionContent).append("\n\n");
                        }
                    }
                }

                Map<String, Object> result = huggingFaceService.reviewArticle(title, fullContent.toString());
                if (result != null) {
                    return ResponseEntity.ok(result);
                } else {
                    Map<String, Object> error = new HashMap<>();
                    error.put("error", "AI tạm thời không phản hồi. Vui lòng thử lại sau.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
                }
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Lỗi hệ thống: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

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
                ensureSectionsExist(articleData);
                model.addAttribute("article", articleData);
                
                ApiFuture<QuerySnapshot> tourFuture = firestore.collection("tours").get();
                List<QueryDocumentSnapshot> tourDocs = tourFuture.get().getDocuments();
                List<Map<String, Object>> tourList = new ArrayList<>();
                for (QueryDocumentSnapshot d : tourDocs) {
                    Map<String, Object> map = new HashMap<>(d.getData());
                    map.put("id", d.getId());
                    tourList.add(map);
                }
                model.addAttribute("tours", tourList);
                return "articles/form";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/admin/articles";
    }

    private void ensureSectionsExist(Map<String, Object> data) {
        if (data.get("sections") == null || !(data.get("sections") instanceof List)) {
            List<Map<String, Object>> sections = new ArrayList<>();
            Map<String, Object> section = new HashMap<>();
            section.put("tieu_de", "");
            Object content = data.getOrDefault("noi_dung", data.getOrDefault("content", data.getOrDefault("description", "")));
            section.put("noi_dung", content != null ? content.toString() : "");
            section.put("hinh_anh", data.get("hinh_anh"));
            sections.add(section);
            data.put("sections", sections);
        }
    }

    @PostMapping("/save")
    public String saveArticle(HttpServletRequest request,
                              @RequestParam(required = false) String id,
                              @RequestParam String tieu_de,
                              @RequestParam Integer loai_id,
                              @RequestParam(required = false) String tac_gia,
                              @RequestParam(required = false) String tour_id,
                              @RequestParam(value = "hinh_anh_muc[]", required = false) MultipartFile[] hinhAnhMuc) throws IOException, ExecutionException, InterruptedException {

        // Sử dụng HttpServletRequest.getParameterValues để tránh việc Spring tự động tách chuỗi theo dấu phẩy
        String[] tieuDeMuc = request.getParameterValues("tieu_de_muc[]");
        String[] noiDungMuc = request.getParameterValues("noi_dung_muc[]");
        String[] hinhAnhCu = request.getParameterValues("hinh_anh_cu[]");

        List<Map<String, Object>> mucList = new ArrayList<>();
        if (tieuDeMuc != null) {
            for (int i = 0; i < tieuDeMuc.length; i++) {
                Map<String, Object> muc = new HashMap<>();
                muc.put("tieu_de", tieuDeMuc[i]);
                muc.put("noi_dung", (noiDungMuc != null && i < noiDungMuc.length) ? noiDungMuc[i] : "");
                
                String currentImageUrl = (hinhAnhCu != null && i < hinhAnhCu.length) ? hinhAnhCu[i] : null;

                if (hinhAnhMuc != null && i < hinhAnhMuc.length && !hinhAnhMuc[i].isEmpty()) {
                    @SuppressWarnings("rawtypes")
                    Map uploadResult = cloudinary.uploader().upload(hinhAnhMuc[i].getBytes(), ObjectUtils.emptyMap());
                    muc.put("hinh_anh", uploadResult.get("secure_url"));
                } else {
                    muc.put("hinh_anh", currentImageUrl);
                }
                mucList.add(muc);
            }
        }

        String author = (tac_gia == null || tac_gia.isEmpty()) ? "Admin" : tac_gia;

        if (id == null || id.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("tieu_de", tieu_de);
            data.put("loai_id", loai_id);
            data.put("tour_id", tour_id != null && !tour_id.equals("0") ? tour_id : null);
            data.put("sections", mucList);
            data.put("so_muc", mucList.size());
            data.put("ngay_tao", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            data.put("trang_thai", 1);
            data.put("nguon_goc", "admin");
            data.put("tac_gia", author);
            data.put("is_edited", false);
            firestore.collection("articles").add(data).get();
        } else {
            firestore.collection("articles").document(id).update(
                "tieu_de", tieu_de,
                "loai_id", loai_id,
                "tac_gia", author,
                "tour_id", tour_id != null && !tour_id.equals("0") ? tour_id : null,
                "sections", mucList,
                "so_muc", mucList.size()
            ).get();
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

    @GetMapping("/confirm-edit/{id}")
    public String confirmEdit(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("articles").document(id).update("is_edited", false).get();
        return "redirect:/admin/articles";
    }

    @GetMapping("/delete/{id}")
    public String deleteArticle(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("articles").document(id).delete().get();
        return "redirect:/admin/articles";
    }
}
