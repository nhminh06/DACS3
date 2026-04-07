package com.example.dacs3.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class HuggingFaceService {

    private static final String API_URL =
            "https://router.huggingface.co/hf-inference/models/MoritzLaurer/mDeBERTa-v3-base-mnli-xnli";

    private static final List<String> CANDIDATE_LABELS =
            List.of("du lịch", "văn hóa", "quảng cáo", "nhạy cảm", "tin tức", "rác");

    private static final int    MAX_CONTENT_LENGTH = 1000;
    private static final double THRESHOLD_BAD      = 0.35; 
    private static final double RELIABILITY_HIGH   = 70;
    private static final double RELIABILITY_MED    = 40;

    private static final Pattern SPAM_PATTERN = Pattern.compile(
            "(https?://\\S{10,}|0\\d{9}|click ngay|liên hệ zalo|nhận quà|trúng thưởng|mua ngay|tặng code)", 
            Pattern.CASE_INSENSITIVE
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public HuggingFaceService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(10000);
                    factory.setReadTimeout(60000); 
                    return factory;
                })
                // 🔥 Ép RestTemplate dùng UTF-8 để không bị lỗi font Tiếng Việt
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();
    }

    public Map<String, Object> reviewArticle(String title, String content) {
        if (apiKey == null || apiKey.isEmpty()) {
            return buildResult(false, false, 0, "Thiếu API Key", "high", false);
        }

        String cleanContent = stripHtml(content);
        String fullTextForRules = title + " " + cleanContent;

        if (isSpamByRule(fullTextForRules)) {
            return buildResult(true, false, 10,
                    "Hệ thống phát hiện dấu hiệu spam qua số điện thoại hoặc từ khóa nhạy cảm.", "high", false);
        }

        String inputText = "Tiêu đề: " + title + "\nNội dung: " +
                (cleanContent.length() > MAX_CONTENT_LENGTH
                        ? cleanContent.substring(0, MAX_CONTENT_LENGTH) : cleanContent);

        try {
            Map<String, Object> body = Map.of(
                    "inputs", inputText,
                    "parameters", Map.of("candidate_labels", CANDIDATE_LABELS, "multi_label", false),
                    "options", Map.of("wait_for_model", true)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(apiKey);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    API_URL, new HttpEntity<>(body, headers), String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return parseResponse(response.getBody());
            }

        } catch (org.springframework.web.client.ResourceAccessException e) {
            return buildResult(false, false, 0, "AI đang khởi động, vui lòng thử lại sau 30 giây.", "medium", false);
        } catch (Exception e) {
            System.err.println("[HuggingFaceService] Error: " + e.getMessage());
        }

        return buildResult(false, false, 50, "Kết nối AI thất bại, vui lòng thử lại sau.", "medium", false);
    }

    private Map<String, Object> parseResponse(String body) {
        try {
            JsonNode root = MAPPER.readTree(body);
            Map<String, Double> scoreMap = new HashMap<>();

            // 1. Xử lý định dạng mảng các đối tượng: [{"label": "...", "score": ...}, ...]
            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("label") && node.has("score")) {
                        scoreMap.put(node.get("label").asText(), node.get("score").asDouble());
                    }
                }
            } 
            // 2. Xử lý định dạng đối tượng đơn lẻ: {"labels": [...], "scores": [...]}
            else if (root.has("labels") && root.has("scores")) {
                JsonNode labels = root.path("labels");
                JsonNode scores = root.path("scores");
                for (int i = 0; i < labels.size(); i++) {
                    scoreMap.put(labels.get(i).asText(), scores.get(i).asDouble());
                }
            }
            
            if (scoreMap.isEmpty()) {
                System.err.println("[HuggingFaceService] Unexpected response: " + body);
                throw new Exception("Dữ liệu trả về không đúng định dạng labels/scores");
            }

            // Tính toán logic dựa trên scoreMap đã gộp
            double badScore = scoreMap.getOrDefault("quảng cáo", 0.0) 
                            + scoreMap.getOrDefault("nhạy cảm", 0.0)
                            + scoreMap.getOrDefault("rác", 0.0);
            
            double goodScore = scoreMap.getOrDefault("du lịch", 0.0) 
                             + scoreMap.getOrDefault("văn hóa", 0.0);

            double reliability = (goodScore / (goodScore + badScore + 0.01)) * 100;
            if (scoreMap.getOrDefault("du lịch", 0.0) > 0.4 || scoreMap.getOrDefault("văn hóa", 0.0) > 0.4) {
                reliability += 15;
            }
            reliability = Math.max(0, Math.min(100, reliability));

            boolean isSpam      = (scoreMap.getOrDefault("quảng cáo", 0.0) + scoreMap.getOrDefault("rác", 0.0)) > THRESHOLD_BAD;
            boolean isSensitive = scoreMap.getOrDefault("nhạy cảm", 0.0) > THRESHOLD_BAD;
            boolean verdict     = !isSpam && !isSensitive && reliability >= RELIABILITY_MED;

            String risk = "low";
            if (isSpam || isSensitive || reliability < 35) risk = "high";
            else if (reliability < RELIABILITY_HIGH) risk = "medium";

            return buildResult(isSpam, isSensitive, reliability,
                    buildMessage(reliability, isSpam, isSensitive, goodScore), risk, verdict);

        } catch (Exception e) {
            System.err.println("[HuggingFaceService] Parsing Error: " + e.getMessage());
            return buildResult(false, false, 0, "Không thể phân tích phản hồi từ AI.", "high", false);
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean isSpamByRule(String text) {
        return SPAM_PATTERN.matcher(text).find();
    }

    private String buildMessage(double score, boolean spam, boolean sensitive, double good) {
        StringBuilder sb = new StringBuilder();
        if (spam) sb.append("Cảnh báo: Có dấu hiệu quảng cáo. ");
        if (sensitive) sb.append("Cảnh báo: Nội dung không phù hợp. ");
        if (score >= RELIABILITY_HIGH) sb.append("Bài viết chất lượng cao.");
        else if (score >= RELIABILITY_MED) sb.append("Nội dung ổn định.");
        else sb.append("Thông tin mờ nhạt, cần kiểm tra thủ công.");
        return sb.toString().trim();
    }

    private Map<String, Object> buildResult(boolean isSpam, boolean isSensitive, double score,
                                            String message, String risk, boolean verdict) {
        return Map.of(
                "isSpam", isSpam,
                "isSensitive", isSensitive,
                "confidenceScore", (int) score,
                "factCheck", message,
                "riskLevel", risk,
                "verdict", verdict,
                "summary", "Hệ thống kiểm duyệt đa ngôn ngữ v3.3"
        );
    }
}
