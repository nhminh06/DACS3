package com.example.dacs3.admin.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private Firestore firestore;

    @GetMapping
    public String listReports(Model model, 
                              @RequestParam(required = false) String type,
                              @RequestParam(defaultValue = "1") int page) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("reports").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            long totalCount = documents.size();
            long pendingCount = documents.stream().filter(d -> !"resolved".equals(d.getString("status"))).count();
            long resolvedCount = totalCount - pendingCount;
            long commentReports = documents.stream().filter(d -> "COMMENT".equals(d.getString("type"))).count();
            long articleReports = totalCount - commentReports;

            model.addAttribute("total", totalCount);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("resolvedCount", resolvedCount);
            model.addAttribute("commentReports", commentReports);
            model.addAttribute("articleReports", articleReports);

            List<Map<String, Object>> reportList = documents.stream().map(d -> {
                Map<String, Object> map = new HashMap<>(d.getData());
                map.put("id", d.getId());
                return map;
            }).collect(Collectors.toList());

            if (type != null && !type.isEmpty()) {
                reportList = reportList.stream()
                        .filter(r -> type.equals(r.get("type")))
                        .collect(Collectors.toList());
            }

            // Sort by newest
            reportList.sort((r1, r2) -> {
                Object t1 = r1.get("createdAt");
                Object t2 = r2.get("createdAt");
                if (t1 == null || t2 == null) return 0;
                return t2.toString().compareTo(t1.toString());
            });

            int pageSize = 10;
            int totalFiltered = reportList.size();
            int totalPages = (int) Math.ceil((double) totalFiltered / pageSize);
            page = Math.max(1, Math.min(page, totalPages > 0 ? totalPages : 1));
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, totalFiltered);

            model.addAttribute("reports", (start < totalFiltered) ? reportList.subList(start, end) : new ArrayList<>());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("typeFilter", type);
            model.addAttribute("totalFiltered", totalFiltered);

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return "reports/list";
    }

    @PostMapping("/reply/{id}")
    @ResponseBody
    public String replyToReport(@PathVariable String id, @RequestParam String replyMessage) throws ExecutionException, InterruptedException {
        firestore.collection("reports").document(id).update(
                "reply", replyMessage,
                "status", "resolved"
        ).get();
        return "success";
    }

    @GetMapping("/delete/{id}")
    public String deleteReport(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("reports").document(id).delete().get();
        return "redirect:/admin/reports";
    }

    @GetMapping("/resolve/{id}")
    public String resolveReport(@PathVariable String id) throws ExecutionException, InterruptedException {
        firestore.collection("reports").document(id).update("status", "resolved").get();
        return "redirect:/admin/reports";
    }
}
