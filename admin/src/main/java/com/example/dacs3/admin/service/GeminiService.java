package com.example.dacs3.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class GeminiService {

    @Value("${huggingface.api.key}")
    private String apiKey;

    // Sử dụng Hugging Face BART model cho zero-shot classification
    private final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-mnli";

    public Map<String, Object> reviewArticle(String title, String content) {
        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("Hugging Face API Key is missing!");
            return null;
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Kết hợp tiêu đề và một phần nội dung để phân tích
            String inputText = "Title: " + title + "\nContent: " + 
                               (content.length() > 500 ? content.substring(0, 500) : content);

            Map<String, Object> body = new HashMap<>();
            body.put("inputs", inputText);

            Map<String, Object> parameters = new HashMap<>();
            // Các nhãn để phân loại bài viết
            parameters.put("candidate_labels", Arrays.asList("spam", "sensitive", "normal", "travel", "culture"));
            body.put("parameters", parameters);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, entity, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return parseHuggingFaceResponse(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Hugging Face API: " + e.getMessage());
        }
        return null;
    }

    private Map<String, Object> parseHuggingFaceResponse(String body) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(body);
            
            JsonNode labels = root.path("labels");
            JsonNode scores = root.path("scores");
            
            Map<String, Double> results = new HashMap<>();
            for (int i = 0; i < labels.size(); i++) {
                results.put(labels.get(i).asText(), scores.get(i).asDouble());
            }

            Map<String, Object> finalResult = new HashMap<>();
            double spamScore = results.getOrDefault("spam", 0.0);
            double sensitiveScore = results.getOrDefault("sensitive", 0.0);
            double normalScore = results.getOrDefault("normal", 0.0);

            finalResult.put("isSpam", spamScore > 0.5);
            finalResult.put("isSensitive", sensitiveScore > 0.5);
            finalResult.put("confidenceScore", Math.max(spamScore, Math.max(sensitiveScore, normalScore)) * 100);
            
            // Vì model classification không trả về text, ta tự tạo nhận xét dựa trên score
            if (spamScore > 0.5) {
                finalResult.put("factCheck", "Nội dung có dấu hiệu spam hoặc quảng cáo không phù hợp.");
                finalResult.put("riskLevel", "high");
            } else if (sensitiveScore > 0.5) {
                finalResult.put("factCheck", "Nội dung có thể chứa thông tin nhạy cảm.");
                finalResult.put("riskLevel", "medium");
            } else {
                finalResult.put("factCheck", "Bài viết có vẻ ổn định và phù hợp với tiêu chuẩn.");
                finalResult.put("riskLevel", "low");
            }
            
            finalResult.put("summary", "Phân tích tự động dựa trên mô hình Hugging Face BART.");

            return finalResult;
        } catch (Exception e) {
            System.err.println("Lỗi parse Hugging Face response: " + e.getMessage());
        }
        return null;
    }
}
