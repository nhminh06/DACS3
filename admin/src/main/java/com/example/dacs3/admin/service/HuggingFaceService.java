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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class HuggingFaceService {

    // ── HuggingFace: phân loại chủ đề ──────────────────────────────────────
    private static final String HF_API_URL =
            "https://router.huggingface.co/hf-inference/models/joeddav/xlm-roberta-large-xnli";

    // ── Groq: fact-check (miễn phí) ─────────────────────────────────────────
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL   = "llama-3.3-70b-versatile";

    private static final List<String> CANDIDATE_LABELS =
            List.of("du lịch", "văn hóa", "quảng cáo", "nhạy cảm", "tin tức", "rác");

    private static final int MAX_CONTENT_LENGTH = 1000;

    // ── Ngưỡng quyết định ───────────────────────────────────────────────────
    private static final double THRESHOLD_SPAM_HF  = 0.55;
    private static final double THRESHOLD_SENSITIVE = 0.35;
    private static final double RELIABILITY_HIGH   = 70;
    private static final double RELIABILITY_MED    = 40;

    // ── Retry config ─────────────────────────────────────────────────────────
    private static final int    GROQ_MAX_RETRIES       = 3;
    private static final long   GROQ_RETRY_DELAY_MS    = 3000L;
    private static final long   GROQ_RATE_LIMIT_DELAY  = 10000L;

    // Chỉ bắt lặp ký tự rõ ràng: aaaaaaa (7 ký tự liên tiếp)
    private static final Pattern GIBBERISH_PATTERN = Pattern.compile(
            "(.)\\1{6,}", Pattern.CASE_INSENSITIVE
    );

    // SĐT: phải đứng riêng, không kề chữ số khác hoặc VNĐ/đ/.
    private static final Pattern SPAM_PATTERN = Pattern.compile(
            "(https?://\\S{10,}"
                    + "|(?<![\\d.,])\\b0\\d{9}\\b(?![\\d.,VNĐđ%])"
                    + "|click ngay|liên hệ zalo|nhận quà"
                    + "|trúng thưởng|mua ngay|tặng code)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // Từ nhạy cảm / bạo lực
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(chết|giết|đâm|chém|tự tử|tự sát|máu me|kinh dị|phản động|biểu tình|đồi trụy|ngu ngốc|đồ tồi)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // Lặp từ: "abc abc abc abc"
    private static final Pattern WORD_REPETITION_PATTERN = Pattern.compile(
            "(\\b\\w+\\b)(\\s+\\1){3,}",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    // ── Regex phát hiện tuyên bố y tế / lịch sử phóng đại ─────────────────
    private static final Pattern MEDICAL_CLAIM_PATTERN = Pattern.compile(
            "(chữa (được|khỏi|hết|trị)|trị (bệnh|ung thư|tiểu đường|tim mạch|huyết áp)"
                    + "|kéo dài tuổi thọ|tăng tuổi thọ|sống (đến|tới|lâu hơn)\\s*\\d+"
                    + "|phòng (ngừa|chống)\\s*(ung thư|covid|virus|bệnh)"
                    + "|100% (hiệu quả|an toàn|chữa khỏi)"
                    + "|thần dược|tiên dược|linh dược)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final Pattern HISTORY_EXAGGERATION_PATTERN = Pattern.compile(
            "(lớn nhất (thế giới|châu á|đông nam á|việt nam)"
                    + "|số 1 (thế giới|châu á|đông nam á)"
                    + "|trung tâm thương mại lớn nhất"
                    + "|cổ xưa nhất|đầu tiên trên thế giới"
                    + "|duy nhất trên thế giới)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${huggingface.api.key}")
    private String hfApiKey;

    @Value("${groq.api.key}")
    private String groqApiKey;

    // RestTemplate dùng chung cho HuggingFace
    private final RestTemplate restTemplate;

    // RestTemplate riêng cho Groq với timeout lớn hơn
    private final RestTemplate groqRestTemplate;

    public HuggingFaceService(RestTemplateBuilder builder) {
        // HuggingFace: connect 30s, read 60s
        this.restTemplate = builder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
                    f.setConnectTimeout(30000);
                    f.setReadTimeout(60000);
                    return f;
                })
                .additionalMessageConverters(new StringHttpMessageConverter(StandardCharsets.UTF_8))
                .build();

        // Groq: connect 15s, read 90s (model lớn cần thêm thời gian)
        this.groqRestTemplate = buildGroqRestTemplate();
    }

    /**
     * Tạo RestTemplate riêng cho Groq với timeout cao hơn.
     */
    private RestTemplate buildGroqRestTemplate() {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(15000);
        f.setReadTimeout(90000);
        RestTemplate rt = new RestTemplate(f);
        rt.getMessageConverters().add(0,
                new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return rt;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ENTRY POINT
    // ═══════════════════════════════════════════════════════════════════════

    public Map<String, Object> reviewArticle(String title, String content) {
        System.out.println("[ReviewService] Kiểm duyệt: " + title);

        if (hfApiKey == null || hfApiKey.isBlank()) {
            return buildResult(false, false, 0,
                    "Thiếu HuggingFace API Key.", "high", false, "", "");
        }

        String cleanContent = stripHtml(content);

        // BƯỚC 1: Quy tắc cứng (regex) — vi phạm rõ ràng → từ chối ngay
        Map<String, Object> ruleResult = checkManualRules(title, cleanContent);
        if (ruleResult != null) return ruleResult;

        // BƯỚC 2: HuggingFace — phân loại chủ đề
        Map<String, Double> scoreMap = callHuggingFace(title, cleanContent);

        // BƯỚC 3: Groq — fact-check (có retry)
        FactCheckResult factCheck = callGroqFactCheck(title, cleanContent);

        // BƯỚC 4: Tổng hợp
        return buildFinalResult(scoreMap, factCheck);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BƯỚC 1 — KIỂM TRA QUY TẮC CỨNG
    // ═══════════════════════════════════════════════════════════════════════

    private Map<String, Object> checkManualRules(String title, String cleanContent) {
        String   fullText = title + "\n" + cleanContent;
        String[] lines    = fullText.split("\\n");

        boolean      hasSensitive     = false;
        boolean      hasSpam          = false;
        Set<String>  sensitiveDetails = new LinkedHashSet<>();
        List<String> spamReasons      = new ArrayList<>();

        if (WORD_REPETITION_PATTERN.matcher(fullText).find()) {
            hasSpam = true;
            spamReasons.add("Lặp từ quá nhiều");
        }
        if (GIBBERISH_PATTERN.matcher(fullText).find()) {
            hasSpam = true;
            spamReasons.add("Nội dung vô nghĩa/rác");
        }
        Matcher spamMatcher = SPAM_PATTERN.matcher(fullText);
        if (spamMatcher.find()) {
            hasSpam = true;
            spamReasons.add("Liên kết/Quảng cáo bị chặn: [" + spamMatcher.group() + "]");
        }
        for (String word : fullText.split("\\s+")) {
            if (word.length() > 30) {
                hasSpam = true;
                spamReasons.add("Chuỗi ký tự quá dài: [" + word.substring(0, 20) + "...]");
                break;
            }
        }
        for (int i = 0; i < lines.length; i++) {
            Matcher m = SENSITIVE_PATTERN.matcher(lines[i]);
            while (m.find()) {
                hasSensitive = true;
                sensitiveDetails.add("'" + m.group() + "' (Dòng " + (i + 1) + ")");
            }
        }

        if (hasSensitive || hasSpam) {
            StringBuilder msg = new StringBuilder();
            if (hasSensitive)
                msg.append("Phát hiện từ cấm: ")
                        .append(String.join(", ", sensitiveDetails)).append(". ");
            if (hasSpam)
                msg.append("Dấu hiệu spam: ")
                        .append(String.join(", ", spamReasons)).append(".");
            String spamDetail = hasSpam
                    ? "Quy tắc regex: " + String.join(", ", spamReasons) : "";
            return buildResult(hasSpam, hasSensitive, 5,
                    msg.toString().trim(), "high", false, "", spamDetail);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BƯỚC 2 — HUGGINGFACE: PHÂN LOẠI CHỦ ĐỀ
    // ═══════════════════════════════════════════════════════════════════════

    private Map<String, Double> callHuggingFace(String title, String cleanContent) {
        String inputText = "Tiêu đề: " + title + "\nNội dung: " +
                (cleanContent.length() > MAX_CONTENT_LENGTH
                        ? cleanContent.substring(0, MAX_CONTENT_LENGTH) : cleanContent);
        try {
            Map<String, Object> body = Map.of(
                    "inputs",     inputText,
                    "parameters", Map.of("candidate_labels", CANDIDATE_LABELS,
                            "multi_label", false),
                    "options",    Map.of("wait_for_model", true)
            );
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            headers.setBearerAuth(hfApiKey);

            for (int attempt = 1; attempt <= 3; attempt++) {
                try {
                    ResponseEntity<String> resp = restTemplate.postForEntity(
                            HF_API_URL, new HttpEntity<>(body, headers), String.class);
                    String resBody = resp.getBody();
                    System.out.println("[HuggingFace] Response: " + resBody);

                    if (resBody != null && (resBody.contains("loading")
                            || resBody.contains("estimated_time"))) {
                        System.out.println("[HuggingFace] Model loading, đợi 15s... (lần "
                                + attempt + ")");
                        Thread.sleep(15000);
                        continue;
                    }
                    if (resp.getStatusCode() == HttpStatus.OK)
                        return parseHuggingFaceScores(resBody);

                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                    String err = e.getResponseBodyAsString();
                    System.err.println("[HuggingFace] HTTP Error "
                            + e.getStatusCode() + ": " + err);
                    if (err.contains("loading") || err.contains("estimated_time")) {
                        Thread.sleep(15000);
                        continue;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("[HuggingFace] Lỗi: " + e.getMessage());
        }
        return Collections.emptyMap();
    }

    private Map<String, Double> parseHuggingFaceScores(String body) {
        try {
            JsonNode            root     = MAPPER.readTree(body);
            Map<String, Double> scoreMap = new HashMap<>();
            if (root.isArray()) {
                for (JsonNode node : root)
                    if (node.has("label") && node.has("score"))
                        scoreMap.put(node.get("label").asText(),
                                node.get("score").asDouble());
            } else if (root.has("labels") && root.has("scores")) {
                JsonNode labels = root.path("labels");
                JsonNode scores = root.path("scores");
                for (int i = 0; i < labels.size(); i++)
                    scoreMap.put(labels.get(i).asText(), scores.get(i).asDouble());
            }
            return scoreMap;
        } catch (Exception e) {
            System.err.println("[HuggingFace] Parse lỗi: " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BƯỚC 3 — GROQ: FACT-CHECK (có retry + timeout riêng)
    // ═══════════════════════════════════════════════════════════════════════

    private static class FactCheckResult {
        double  factScore;
        boolean hasFakeClaims;
        boolean isAdvertising;
        String  detail;
        boolean available;

        FactCheckResult(double factScore, boolean hasFakeClaims,
                        boolean isAdvertising, String detail, boolean available) {
            this.factScore     = factScore;
            this.hasFakeClaims = hasFakeClaims;
            this.isAdvertising = isAdvertising;
            this.detail        = detail;
            this.available     = available;
        }
    }

    private FactCheckResult callGroqFactCheck(String title, String content) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            System.out.println("[Groq] Không có API key, bỏ qua fact-check.");
            return new FactCheckResult(70, false, false, "", false);
        }

        String truncated = content.length() > 1500
                ? content.substring(0, 1500) + "..." : content;

        // Phát hiện sớm bằng regex
        boolean hasMedicalClaim = MEDICAL_CLAIM_PATTERN.matcher(truncated).find();
        boolean hasHistoryClaim = HISTORY_EXAGGERATION_PATTERN.matcher(truncated).find();

        StringBuilder warningHints = new StringBuilder();
        if (hasMedicalClaim)
            warningHints.append(
                    "\n⚠ Lưu ý: Bài viết có tuyên bố liên quan đến sức khỏe/y tế. "
                            + "Hãy kiểm tra kỹ tính chính xác.");
        if (hasHistoryClaim)
            warningHints.append(
                    "\n⚠ Lưu ý: Bài viết có tuyên bố lịch sử mang tính cực đoan "
                            + "(lớn nhất, số 1...). Hãy kiểm tra kỹ tính chính xác.");

        String prompt = """
                Bạn là chuyên gia kiểm tra thông tin du lịch, lịch sử và y tế Việt Nam.
                Bắt buộc phải đọc và tìm hiểu về nội dung sác nhập tỉnh thành việt nam ngày 1 tháng 7 năm 2025.
                Hãy đánh giá bài viết sau và trả lời ĐÚNG định dạng JSON bên dưới.
                KHÔNG thêm bất kỳ text nào khác ngoài JSON.
                %s

                === QUY TẮC ĐÁNH GIÁ ===
                - Tuyên bố y tế / sức khoẻ không có căn cứ khoa học → hasFakeClaims = true, factScore giảm mạnh (< 40)
                - Tuyên bố lịch sử phóng đại, không có nguồn gốc → hasFakeClaims = true, factScore giảm mạnh (< 40)
                - Thông tin địa lý, du lịch thực tế đúng → factScore cao
                - Bài viết kêu gọi mua hàng/dịch vụ trá hình → isAdvertising = true
                - Bài viết sai về địa điểm trực thộc của địa danh, địa điểm lịch sử → hasFakeClaims = true, factScore giảm mạnh (< 40) 
                Tiêu đề: %s
                Nội dung: %s

                Trả về JSON:
                {
                  "factScore": <số từ 0-100, 100 = hoàn toàn chính xác>,
                  "hasFakeClaims": <true nếu có thông tin sai/phóng đại nghiêm trọng, false nếu không>,
                  "isAdvertising": <true nếu là quảng cáo thương mại trá hình, false nếu là nội dung thông tin bình thường>,
                  "adReason": "<nếu isAdvertising=true, giải thích ngắn gọn 1 câu; nếu false để trống>",
                  "fakeClaims": ["<liệt kê từng thông tin sai/phóng đại, tối đa 3 mục, để trống nếu không có>"],
                  "summary": "<nhận xét ngắn gọn 1 câu bằng tiếng Việt>"
                }
                """.formatted(warningHints.toString(), title, truncated);

        Map<String, Object> body = Map.of(
                "model",       GROQ_MODEL,
                "max_tokens",  600,
                "temperature", 0.1,
                "messages",    List.of(
                        Map.of("role", "system",
                                "content", """
                                        Bạn là chuyên gia fact-check nội dung du lịch Việt Nam.
                                        Chỉ trả về JSON, không có text khác.
                                        Đặc biệt chú ý:
                                        - Tuyên bố y tế thiếu căn cứ (ví dụ: thực phẩm chữa bệnh, kéo dài tuổi thọ cụ thể) là SAI.
                                        - Tuyên bố lịch sử không có nguồn (ví dụ: "trung tâm thương mại lớn nhất Đông Nam Á") là cần kiểm chứng, thường là SAI nếu không có tài liệu.
                                        """),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(groqApiKey);

        // ── Retry loop ────────────────────────────────────────────────────
        for (int attempt = 1; attempt <= GROQ_MAX_RETRIES; attempt++) {
            try {
                System.out.println("[Groq] Gọi lần " + attempt + "/" + GROQ_MAX_RETRIES);

                ResponseEntity<String> resp = groqRestTemplate.postForEntity(
                        GROQ_API_URL, new HttpEntity<>(body, headers), String.class);

                System.out.println("[Groq] Status: " + resp.getStatusCode());

                if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                    return parseGroqResponse(resp.getBody());
                }

                // Nếu status không phải 200 nhưng không ném exception
                System.err.println("[Groq] Status không mong đợi: " + resp.getStatusCode());

            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                int statusCode = e.getStatusCode().value();
                System.err.println("[Groq] HTTP Error lần " + attempt
                        + " | Status: " + statusCode
                        + " | Body: " + e.getResponseBodyAsString());

                if (statusCode == 429) {
                    // Rate limit → chờ lâu hơn
                    long waitMs = GROQ_RATE_LIMIT_DELAY * attempt;
                    System.out.println("[Groq] Rate limit, chờ " + waitMs + "ms...");
                    sleepSafe(waitMs);
                } else if (statusCode >= 500) {
                    // Lỗi server Groq → retry
                    sleepSafe(GROQ_RETRY_DELAY_MS * attempt);
                } else {
                    // Lỗi client (400, 401, 403...) → không retry
                    System.err.println("[Groq] Lỗi client, dừng retry.");
                    break;
                }

            } catch (Exception e) {
                // Bắt Connection reset, timeout, I/O error...
                System.err.println("[Groq] Lỗi kết nối lần " + attempt
                        + ": " + e.getClass().getSimpleName()
                        + " - " + e.getMessage());

                if (attempt < GROQ_MAX_RETRIES) {
                    long waitMs = GROQ_RETRY_DELAY_MS * attempt;
                    System.out.println("[Groq] Thử lại sau " + waitMs + "ms...");
                    sleepSafe(waitMs);
                }
            }
        }

        // ── Fallback sau khi hết retry ────────────────────────────────────
        System.err.println("[Groq] Không thể kết nối sau " + GROQ_MAX_RETRIES + " lần thử.");

        if (hasMedicalClaim || hasHistoryClaim) {
            List<String> suspects = new ArrayList<>();
            if (hasMedicalClaim) suspects.add("Tuyên bố y tế/sức khoẻ chưa được kiểm chứng");
            if (hasHistoryClaim) suspects.add("Tuyên bố lịch sử mang tính cực đoan");
            String detail = "⚠ Cần kiểm tra thủ công: " + String.join("; ", suspects);
            return new FactCheckResult(45, true, false, detail, false);
        }

        return new FactCheckResult(70, false, false,
                "Không thể fact-check tự động (kết nối thất bại).", false);
    }

    private FactCheckResult parseGroqResponse(String body) {
        try {
            JsonNode root    = MAPPER.readTree(body);
            String   rawText = root.path("choices").get(0)
                    .path("message").path("content").asText();
            System.out.println("[Groq] Raw response: " + rawText);

            int start = rawText.indexOf('{');
            int end   = rawText.lastIndexOf('}');
            if (start == -1 || end == -1)
                return new FactCheckResult(70, false, false,
                        "Không parse được JSON từ Groq.", false);

            JsonNode json          = MAPPER.readTree(rawText.substring(start, end + 1));
            double   factScore     = json.path("factScore").asDouble(70);
            boolean  hasFakeClaims = json.path("hasFakeClaims").asBoolean(false);
            boolean  isAdvertising = json.path("isAdvertising").asBoolean(false);
            String   adReason      = json.path("adReason").asText("").trim();
            String   summary       = json.path("summary").asText("");

            List<String> fakes     = new ArrayList<>();
            JsonNode     fakesNode = json.path("fakeClaims");
            if (fakesNode.isArray())
                fakesNode.forEach(n -> {
                    String s = n.asText().trim();
                    if (!s.isBlank()) fakes.add(s);
                });

            String detail = summary;
            if (!fakes.isEmpty())
                detail += " | Thông tin sai: " + String.join("; ", fakes);
            if (isAdvertising && !adReason.isBlank())
                detail += " | Lý do quảng cáo: " + adReason;

            if (isAdvertising) factScore = Math.min(factScore, 40);

            System.out.println("[Groq] factScore=" + factScore
                    + " | hasFake=" + hasFakeClaims
                    + " | isAdvertising=" + isAdvertising
                    + " | detail=" + detail);

            return new FactCheckResult(factScore, hasFakeClaims || isAdvertising,
                    isAdvertising, detail, true);

        } catch (Exception e) {
            System.err.println("[Groq] Parse lỗi: " + e.getMessage());
            return new FactCheckResult(70, false, false,
                    "Lỗi phân tích kết quả Groq.", false);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  BƯỚC 4 — TỔNG HỢP KẾT QUẢ
    // ═══════════════════════════════════════════════════════════════════════

    private Map<String, Object> buildFinalResult(Map<String, Double> scoreMap,
                                                 FactCheckResult factCheck) {
        double adScore        = scoreMap.getOrDefault("quảng cáo", 0.0);
        double sensitiveScore = scoreMap.getOrDefault("nhạy cảm",  0.0);
        double trashScore     = scoreMap.getOrDefault("rác",        0.0);
        double tourismScore   = scoreMap.getOrDefault("du lịch",   0.0);
        double cultureScore   = scoreMap.getOrDefault("văn hóa",   0.0);
        double newsScore      = scoreMap.getOrDefault("tin tức",   0.0);

        double badScore  = adScore + sensitiveScore + trashScore;
        double goodScore = tourismScore + cultureScore + newsScore;

        // Điểm HuggingFace (thô)
        double hfScore = scoreMap.isEmpty() ? 60.0
                : (goodScore / (goodScore + badScore + 0.01)) * 100;

        if (tourismScore > 0.5 || cultureScore > 0.5 || newsScore > 0.5) hfScore += 15;
        else if (tourismScore > 0.3 || cultureScore > 0.3)               hfScore += 8;
        if (adScore + trashScore > 0.4)                                   hfScore -= 25;
        hfScore = Math.max(0, Math.min(100, hfScore));

        // ── Phán quyết spam ────────────────────────────────────────────────
        boolean hfSaysSpam     = (adScore + trashScore) > THRESHOLD_SPAM_HF;
        boolean groqSaysSpam   = factCheck.available && factCheck.isAdvertising;
        boolean groqClearsSpam = factCheck.available
                && factCheck.factScore >= 65
                && !groqSaysSpam
                && !factCheck.hasFakeClaims;

        boolean isSpam      = (hfSaysSpam || groqSaysSpam) && !groqClearsSpam;
        boolean isSensitive = sensitiveScore > THRESHOLD_SENSITIVE
                || (factCheck.hasFakeClaims && !groqClearsSpam);

        // ── Lý do spam chi tiết ────────────────────────────────────────────
        StringBuilder spamDetail = new StringBuilder();
        if (isSpam) {
            if (hfSaysSpam && !groqClearsSpam) {
                spamDetail.append(String.format(
                        "HuggingFace AI: điểm 'quảng cáo'=%.0f%%, 'rác'=%.0f%% (tổng %.0f%% > ngưỡng %.0f%%)",
                        adScore * 100, trashScore * 100,
                        (adScore + trashScore) * 100, THRESHOLD_SPAM_HF * 100));
            }
            if (groqSaysSpam) {
                if (spamDetail.length() > 0) spamDetail.append(" | ");
                spamDetail.append("Groq AI xác nhận: ").append(factCheck.detail);
            }
        }

        // Điểm tổng hợp: HuggingFace 40% + Groq 60%
        double finalScore = factCheck.available
                ? (hfScore * 0.40) + (factCheck.factScore * 0.60)
                : hfScore;

        // Penalty nếu bài có thông tin sai
        if (factCheck.hasFakeClaims && !groqClearsSpam) finalScore -= 25;
        if (isSpam)                                      finalScore -= 20;
        finalScore = Math.max(0, Math.min(100, finalScore));

        String risk = "low";
        if (isSpam || (factCheck.hasFakeClaims && !groqClearsSpam) || finalScore < 35)
            risk = "high";
        else if (isSensitive || finalScore < RELIABILITY_HIGH)
            risk = "medium";

        boolean verdict = !isSpam
                && !(factCheck.hasFakeClaims && !groqClearsSpam)
                && finalScore >= RELIABILITY_MED;

        // ── Thông điệp tóm tắt ────────────────────────────────────────────
        StringBuilder msg = new StringBuilder();
        if (isSpam) {
            msg.append("Cảnh báo: Có dấu hiệu quảng cáo/spam. ");
            if (spamDetail.length() > 0)
                msg.append("(").append(spamDetail).append(") ");
        }
        if (factCheck.hasFakeClaims && !groqClearsSpam && !factCheck.detail.isBlank())
            msg.append("⚠ Thông tin sai/phóng đại: ").append(factCheck.detail).append(" ");
        if (finalScore >= RELIABILITY_HIGH)
            msg.append("Bài viết chính xác và chất lượng cao.");
        else if (finalScore >= RELIABILITY_MED)
            msg.append("Nội dung ổn định.");
        else
            msg.append("Nội dung cần kiểm tra thủ công.");

        System.out.println("[ReviewService] hfScore=" + String.format("%.1f", hfScore)
                + " | adScore="      + String.format("%.2f", adScore)
                + " | trashScore="   + String.format("%.2f", trashScore)
                + " | factScore="    + factCheck.factScore
                + " | finalScore="   + String.format("%.1f", finalScore)
                + " | hasFake="      + factCheck.hasFakeClaims
                + " | hfSaysSpam="   + hfSaysSpam
                + " | groqSaysSpam=" + groqSaysSpam
                + " | groqClears="   + groqClearsSpam
                + " | isSpam="       + isSpam
                + " | risk="         + risk);

        return buildResult(isSpam, isSensitive, finalScore, msg.toString().trim(),
                risk, verdict,
                factCheck.available ? factCheck.detail : "",
                spamDetail.toString());
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════════════

    private String stripHtml(String html) {
        if (html == null) return "";
        return html.replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n")
                .replaceAll("<[^>]*>", " ")
                .replaceAll("[\\t\\r\\f ]+", " ")
                .trim();
    }

    private void sleepSafe(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, Object> buildResult(boolean isSpam, boolean isSensitive,
                                            double score, String message, String risk,
                                            boolean verdict, String factDetail,
                                            String spamDetail) {
        return Map.of(
                "isSpam",          isSpam,
                "isSensitive",     isSensitive,
                "confidenceScore", (int) score,
                "factCheck",       message,
                "factDetail",      factDetail,
                "spamDetail",      spamDetail,
                "riskLevel",       risk,
                "verdict",         verdict,
                "summary",         "Hệ thống kiểm duyệt AI(HuggingFace + Groq)"
        );
    }
}