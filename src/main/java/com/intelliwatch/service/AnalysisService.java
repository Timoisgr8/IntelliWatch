package com.intelliwatch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliwatch.model.Article;
import com.intelliwatch.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final ArticleRepository articleRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public Article analyseArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found: " + articleId));

        log.info("Analysing article: {}", article.getTitle());

        String prompt = buildPrompt(article);
        String response = callGeminiApi(prompt);
        parseAndUpdateArticle(article, response);

        return articleRepository.save(article);
    }

    public List<Article> analyseAll() {
        List<Article> unanalysed = articleRepository.findAll()
                .stream()
                .filter(a -> a.getSummary() == null)
                .toList();

        log.info("Analysing {} unanalysed articles", unanalysed.size());

        for (Article article : unanalysed) {
            try {
                analyseArticle(article.getId());
                Thread.sleep(15000); // 15 second delay to respect 5 RPM limit
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Analysis interrupted");
                break;
            } catch (Exception e) {
                log.warn("Failed to analyse article {}: {}", article.getId(), e.getMessage());
            }
        }

        return articleRepository.findAll();
    }

    private String buildPrompt(Article article) {
        return """
                Analyse this article for threat and risk intelligence relevance.
                
                Title: %s
                URL: %s
                
                Respond in this exact JSON format with no additional text:
                {
                    "summary": "2-3 sentence summary of the article",
                    "riskTags": ["TAG1", "TAG2"],
                    "confidenceScore": 0.0
                }
                
                Risk tags must be chosen from: CYBERSECURITY, GEOPOLITICAL, MISINFORMATION, AI_RISK, ECONOMIC, PRIVACY, INFRASTRUCTURE, LOW_RELEVANCE
                Confidence score is between 0.0 and 1.0 indicating how relevant this is to threat intelligence.
                Return only the JSON object, no markdown, no explanation.
                """.formatted(article.getTitle(), article.getUrl());
    }

    private String callGeminiApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(
                GEMINI_URL + geminiApiKey,
                request,
                String.class
        );

        return response.getBody();
    }

    private void parseAndUpdateArticle(Article article, String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            // Gemini response structure:
            // candidates[0].content.parts[0].text
            String content = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

            // Strip markdown code fences if Gemini adds them
            content = content.replaceAll("```json", "").replaceAll("```", "").trim();

            JsonNode parsed = objectMapper.readTree(content);

            article.setSummary(parsed.path("summary").asText());
            article.setConfidenceScore(parsed.path("confidenceScore").asDouble());

            JsonNode tags = parsed.path("riskTags");
            String[] riskTags = new String[tags.size()];
            for (int i = 0; i < tags.size(); i++) {
                riskTags[i] = tags.get(i).asText();
            }
            article.setRiskTags(riskTags);

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            article.setSummary("Analysis failed");
            article.setRiskTags(new String[]{"LOW_RELEVANCE"});
            article.setConfidenceScore(0.0);
        }
    }
}