package com.intelliwatch.controller;

import com.intelliwatch.model.Article;
import com.intelliwatch.service.AnalysisService;
import com.intelliwatch.service.ArticleFetcherService;
import com.intelliwatch.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Slf4j
public class ArticleController {

    private final ArticleFetcherService articleFetcherService;
    private final ArticleRepository articleRepository;
    private final AnalysisService analysisService;

    // Fetch new articles from HackerNews and save them
    @PostMapping("/fetch")
    public ResponseEntity<List<Article>> fetchArticles() {
        log.info("POST /api/articles/fetch triggered");
        List<Article> articles = articleFetcherService.fetchAndSaveTopStories();
        return ResponseEntity.ok(articles);
    }

    // Get all stored articles
    @GetMapping
    public ResponseEntity<List<Article>> getAllArticles() {
        List<Article> articles = articleRepository.findAll();
        return ResponseEntity.ok(articles);
    }

    // Get a single article by ID
    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return articleRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Analyse a single article by ID
    @PostMapping("/{id}/analyse")
    public ResponseEntity<Article> analyseArticle(@PathVariable Long id) {
        log.info("POST /api/articles/{}/analyse triggered", id);
        Article article = analysisService.analyseArticle(id);
        return ResponseEntity.ok(article);
    }

    // Analyse all unanalysed articles
    @PostMapping("/analyse-all")
    public ResponseEntity<List<Article>> analyseAll() {
        log.info("POST /api/articles/analyse-all triggered");
        List<Article> articles = analysisService.analyseAll();
        return ResponseEntity.ok(articles);
    }
}