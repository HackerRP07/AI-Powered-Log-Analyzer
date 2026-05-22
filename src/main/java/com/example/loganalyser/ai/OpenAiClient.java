package com.example.loganalyser.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpenAiClient {

    private final ObjectMapper objectMapper;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    @Value("${openai.base-url:https://api.openai.com}")
    private String baseUrl;

    @Value("${openai.api-key:}")
    private String apiKey;

    public OpenAiClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String getAnalysis(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return """
                {
                  "summary": "OPENAI_API_KEY is not configured. This is a local fallback summary.",
                  "findings": [],
                  "timeline": [],
                  "recommendations": ["Set OPENAI_API_KEY to enable real AI analysis."],
                  "confidenceScore": 0.0
                }
                """;
        }

        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        String body = """
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "user",
                      "content": %s
                    }
                  ],
                  "temperature": 0.2
                }
                """.formatted(model, toJsonString(prompt));

        String response = client.post()
                .uri("/v1/chat/completions")
                .body(body)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").path(0).path("message").path("content").asText("{}");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse OpenAI response.", e);
        }
    }

    private String toJsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize prompt.", e);
        }
    }
}
