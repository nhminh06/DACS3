package com.example.dacs3.admin.service;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

public class HuggingFaceTester {

    private static final String API_KEY = "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";

    private static final String URL =
            "https://router.huggingface.co/hf-inference/models/facebook/bart-large-mnli";

    public static void main(String[] args) {

        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", "Mua iPhone giá rẻ!!! Click ngay!!! http://spam.com");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("candidate_labels", Arrays.asList("spam", "normal"));

        body.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 🔥 FIX QUAN TRỌNG
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        headers.setBearerAuth(API_KEY);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(URL, entity, String.class);

            System.out.println("Status: " + response.getStatusCode());
            System.out.println("Response: " + response.getBody());

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}