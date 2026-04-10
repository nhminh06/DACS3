package com.example.dacs3.admin.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GroqApiTest {

    private static final String GROQ_API_KEY = "<YOUR_GROQ_API_KEY>";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL   = "llama-3.3-70b-versatile";

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(30000);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(factory);

        // ✅ StringConverter hỗ trợ application/json để nhận response về String
        StringHttpMessageConverter stringConverter =
                new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setSupportedMediaTypes(List.of(
                MediaType.TEXT_PLAIN,
                MediaType.ALL,
                MediaType.APPLICATION_JSON
        ));

        // ✅ JacksonConverter để serialize Map -> JSON khi gửi request
        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter jacksonConverter =
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter();

        restTemplate.setMessageConverters(List.of(stringConverter, jacksonConverter));
        return restTemplate;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  TEST 1: Kết nối cơ bản
    // ─────────────────────────────────────────────────────────────────────
    @Test
    public void testBasicConnection() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("TEST 1: Kết nối cơ bản Groq API");
        System.out.println("═══════════════════════════════════════");

        RestTemplate restTemplate = buildRestTemplate();

        Map<String, Object> body = Map.of(
                "model",      GROQ_MODEL,
                "max_tokens", 100,
                "messages",   List.of(
                        Map.of("role", "user", "content", "Xin chào, bạn có hoạt động không?")
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GROQ_API_KEY);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    GROQ_API_URL, new HttpEntity<>(body, headers), String.class);

            System.out.println("✅ Status: " + resp.getStatusCode());

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(resp.getBody());
            String reply = root.path("choices").get(0)
                    .path("message").path("content").asText();
            System.out.println("✅ Groq trả lời: " + reply);

        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  TEST 2: Fact-check bài viết ĐÚNG (Hội An)
    // ─────────────────────────────────────────────────────────────────────
    @Test
    public void testFactCheckValidArticle() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("TEST 2: Fact-check bài viết ĐÚNG (Hội An)");
        System.out.println("═══════════════════════════════════════");

        String title   = "Phố cổ Hội An - Di sản văn hóa thế giới";
        String content = """
                Phố cổ Hội An nằm ở hạ lưu sông Thu Bồn, thuộc tỉnh Quảng Nam.
                Năm 1999, UNESCO công nhận Hội An là Di sản Văn hóa Thế giới.
                Nơi đây nổi tiếng với kiến trúc cổ kính và lễ hội đèn lồng hàng tháng.
                Hội An cách trung tâm Đà Nẵng khoảng 30km về phía Nam.
                """;

        callGroqAndPrint(title, content);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  TEST 3: Fact-check bài viết SAI (Lý Sơn - thông tin phóng đại)
    // ─────────────────────────────────────────────────────────────────────
    @Test
    public void testFactCheckFakeArticle() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("TEST 3: Fact-check bài viết SAI (Lý Sơn)");
        System.out.println("═══════════════════════════════════════");

        String title   = "Tour du lịch Đảo Lý Sơn";
        String content = """
                Nhiều người tin rằng tỏi Lý Sơn có thể chữa được hầu hết các bệnh tim mạch
                và giúp kéo dài tuổi thọ lên đến 100 tuổi nếu sử dụng thường xuyên.
                Một số tài liệu còn cho rằng Đảo Lý Sơn từng là trung tâm thương mại
                lớn nhất Đông Nam Á vào thế kỷ 18.
                """;

        callGroqAndPrint(title, content);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  TEST 4: Fact-check bài viết SPAM
    // ─────────────────────────────────────────────────────────────────────
    @Test
    public void testFactCheckSpamArticle() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("TEST 4: Fact-check bài viết SPAM");
        System.out.println("═══════════════════════════════════════");

        String title   = "Tour siêu rẻ Đà Nẵng chỉ 99k";
        String content = """
                Mua ngay tour Đà Nẵng giá chỉ 99k! Liên hệ Zalo 0987654321.
                Nhận quà tặng code giảm giá 50%. Click ngay để đặt chỗ!
                Trúng thưởng iPhone 15 khi đặt tour hôm nay!
                """;

        callGroqAndPrint(title, content);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  TEST 5: Kiểm tra JSON response có parse được không
    // ─────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonParsing() {
        System.out.println("═══════════════════════════════════════");
        System.out.println("TEST 5: Kiểm tra JSON parse từ Groq");
        System.out.println("═══════════════════════════════════════");

        RestTemplate restTemplate = buildRestTemplate();

        String prompt = """
                Trả về JSON sau, KHÔNG thêm text khác:
                {
                  "factScore": 85,
                  "hasFakeClaims": false,
                  "fakeClaims": [],
                  "summary": "Bài viết chính xác"
                }
                """;

        Map<String, Object> body = Map.of(
                "model",       GROQ_MODEL,
                "max_tokens",  200,
                "temperature", 0.1,
                "messages",    List.of(
                        Map.of("role", "system",
                                "content", "Chỉ trả về JSON, không có text khác."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GROQ_API_KEY);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    GROQ_API_URL, new HttpEntity<>(body, headers), String.class);

            System.out.println("✅ Raw response: " + resp.getBody());

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(resp.getBody());
            String content = root.path("choices").get(0)
                    .path("message").path("content").asText();

            System.out.println("✅ Extracted content: " + content);

            int start = content.indexOf('{');
            int end   = content.lastIndexOf('}');
            if (start != -1 && end != -1) {
                com.fasterxml.jackson.databind.JsonNode json =
                        mapper.readTree(content.substring(start, end + 1));
                System.out.println("✅ factScore    : " + json.path("factScore").asDouble());
                System.out.println("✅ hasFakeClaims: " + json.path("hasFakeClaims").asBoolean());
                System.out.println("✅ summary      : " + json.path("summary").asText());
                System.out.println("✅ JSON parse THÀNH CÔNG!");
            } else {
                System.err.println("❌ Không tìm thấy JSON trong response");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi parse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    //  HELPER: Gọi Groq fact-check và in kết quả
    // ─────────────────────────────────────────────────────────────────────
    private void callGroqAndPrint(String title, String content) {
        RestTemplate restTemplate = buildRestTemplate();

        String prompt = """
                Bạn là chuyên gia kiểm tra thông tin du lịch và văn hóa Việt Nam.
                Hãy đánh giá bài viết sau và trả lời ĐÚNG định dạng JSON bên dưới.
                KHÔNG thêm bất kỳ text nào khác ngoài JSON.

                Tiêu đề: %s
                Nội dung: %s

                Trả về JSON:
                {
                  "factScore": <số từ 0-100, 100 = hoàn toàn chính xác>,
                  "hasFakeClaims": <true nếu có thông tin sai/phóng đại, false nếu không>,
                  "fakeClaims": ["<liệt kê thông tin sai nếu có, tối đa 3 mục>"],
                  "summary": "<nhận xét ngắn gọn 1 câu bằng tiếng Việt>"
                }
                """.formatted(title, content);

        Map<String, Object> body = Map.of(
                "model",       GROQ_MODEL,
                "max_tokens",  500,
                "temperature", 0.1,
                "messages",    List.of(
                        Map.of("role", "system",
                                "content", "Bạn là chuyên gia fact-check nội dung du lịch Việt Nam. Chỉ trả về JSON, không có text khác."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GROQ_API_KEY);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(
                    GROQ_API_URL, new HttpEntity<>(body, headers), String.class);

            System.out.println("✅ Status: " + resp.getStatusCode());

            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(resp.getBody());
            String rawText = root.path("choices").get(0)
                    .path("message").path("content").asText();

            System.out.println("🔍 Raw AI text: " + rawText);

            int start = rawText.indexOf('{');
            int end   = rawText.lastIndexOf('}');
            if (start != -1 && end != -1) {
                com.fasterxml.jackson.databind.JsonNode json =
                        mapper.readTree(rawText.substring(start, end + 1));

                System.out.println("📊 factScore    : " + json.path("factScore").asDouble());
                System.out.println("🚨 hasFakeClaims: " + json.path("hasFakeClaims").asBoolean());
                System.out.println("📝 summary      : " + json.path("summary").asText());

                com.fasterxml.jackson.databind.JsonNode fakes = json.path("fakeClaims");
                if (fakes.isArray() && fakes.size() > 0) {
                    System.out.println("❌ Thông tin sai:");
                    fakes.forEach(f -> System.out.println("   - " + f.asText()));
                } else {
                    System.out.println("✅ Không phát hiện thông tin sai");
                }
            } else {
                System.err.println("❌ Không tìm thấy JSON trong response: " + rawText);
            }

        } catch (Exception e) {
            System.err.println("❌ Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
}