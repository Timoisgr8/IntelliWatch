package com.intelliwatch.controller;

import com.intelliwatch.model.Article;
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
}