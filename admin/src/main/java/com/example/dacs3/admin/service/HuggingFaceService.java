package com.example.dacs3.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
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

    // Phát hiện chuỗi vô nghĩa: lặp ký tự (jjjj), không nguyên âm, hoặc trộn chữ/số quá dài
    private static final Pattern GIBBERISH_PATTERN = Pattern.compile(
            "(.)\\1{6,}|\\b[^aeiouyáàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợ\\s\\d]{8,}\\b|\\b(?=[^\\s]*[a-zA-Z])(?=[^\\s]*\\d)[^\\s]{10,}\\b",
            Pattern.CASE_INSENSITIVE
    );

    // CHẶN TỪ NGỮ NHẠY CẢM / ÁC Ý (Bạo lực, tự tử, đồi trụy, phản động)
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(chết|giết|đâm|chém|tự tử|tự sát|máu me|kinh dị|phản động|biểu tình|đồi trụy|ngu ngốc|đồ tồi|)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // PHÁT HIỆN LẶP TỪ (Ví dụ: "chết chết chết chết")
    private static final Pattern WORD_REPETITION_PATTERN = Pattern.compile(
            "(\\b\\w+\\b)(\\s+\\1){3,}",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${huggingface.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public HuggingFaceService(RestTemplateBuilder builder) {
        this.restTemplate = builder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
                    factory.setConnectTimeout(15000);
                    factory.setReadTimeout(120000); 
                    return factory;
                })
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();
    }

    public Map<String, Object> reviewArticle(String title, String content) {
        if (apiKey == null || apiKey.isEmpty()) {
            return buildResult(false, false, 0, "Thiếu API Key", "high", false);
        }

        String cleanContent = stripHtml(content);
        String fullTextForRules = title + " " + cleanContent;

        // BƯỚC 1: KIỂM TRA QUY TẮC CỨNG (NHANH & CHÍNH XÁC)
        Map<String, Object> ruleResult = checkManualRules(fullTextForRules);
        if (ruleResult != null) return ruleResult;

        String inputText = "Tiêu đề: " + title + "\nNội dung: " +
                (cleanContent.length() > MAX_CONTENT_LENGTH
                        ? cleanContent.substring(0, MAX_CONTENT_LENGTH) : cleanContent);

        // BƯỚC 2: GỌI AI (CHUYÊN SÂU)
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

        } catch (ResourceAccessException e) {
            System.err.println("[HuggingFaceService] Timeout: " + e.getMessage());
            return buildResult(false, false, 0, "AI phản hồi chậm do đang khởi động model. Vui lòng thử lại sau 30 giây.", "medium", false);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                return buildResult(false, false, 0, "Hệ thống AI đang khởi động. Vui lòng thử lại sau giây lát.", "medium", false);
            }
        } catch (Exception e) {
            System.err.println("[HuggingFaceService] Error: " + e.getMessage());
        }

        return buildResult(false, false, 50, "Kết nối AI thất bại, vui lòng thử lại sau.", "medium", false);
    }

    private Map<String, Object> checkManualRules(String text) {
        // 1. Kiểm tra lặp từ (Ví dụ: "chết chết chết chết")
        if (WORD_REPETITION_PATTERN.matcher(text).find()) {
            return buildResult(true, true, 5, "Nội dung có dấu hiệu spam (lặp từ quá nhiều lần).", "high", false);
        }

        // 2. Kiểm tra từ ngữ nhạy cảm trực tiếp
        if (SENSITIVE_PATTERN.matcher(text).find()) {
            return buildResult(false, true, 10, "Nội dung chứa từ ngữ nhạy cảm hoặc ác ý không phù hợp.", "high", false);
        }

        // 3. Kiểm tra chuỗi vô nghĩa (jjjjj, gfhfgh)
        if (GIBBERISH_PATTERN.matcher(text).find()) {
            return buildResult(true, false, 15, "Nội dung vô nghĩa hoặc rác (gibberish).", "high", false);
        }

        // 4. Kiểm tra link/số điện thoại spam
        if (SPAM_PATTERN.matcher(text).find()) {
            return buildResult(true, false, 20, "Nội dung chứa liên kết hoặc thông tin quảng cáo bị chặn.", "high", false);
        }

        // 5. Kiểm tra độ dài từ bất thường
        for (String word : text.split("\\s+")) {
            if (word.length() > 30) {
                return buildResult(true, false, 10, "Nội dung chứa chuỗi ký tự quá dài không hợp lệ.", "high", false);
            }
        }

        return null; // Không vi phạm quy tắc cứng
    }

    private Map<String, Object> parseResponse(String body) {
        try {
            JsonNode root = MAPPER.readTree(body);
            Map<String, Double> scoreMap = new HashMap<>();

            if (root.isArray()) {
                for (JsonNode node : root) {
                    if (node.has("label") && node.has("score")) {
                        scoreMap.put(node.get("label").asText(), node.get("score").asDouble());
                    }
                }
            } else if (root.has("labels") && root.has("scores")) {
                JsonNode labels = root.path("labels");
                JsonNode scores = root.path("scores");
                for (int i = 0; i < labels.size(); i++) {
                    scoreMap.put(labels.get(i).asText(), scores.get(i).asDouble());
                }
            }
            
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
            return buildResult(false, false, 0, "Lỗi phân tích dữ liệu AI.", "high", false);
        }
    }

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
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
                "summary", "Hệ thống kiểm duyệt AI Pro v4.0"
        );
    }
}
