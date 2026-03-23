package com.intelliwatch.service;

import com.intelliwatch.model.Article;
import com.intelliwatch.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleFetcherService {
    private final ArticleRepository articleRepository;
    private final RestTemplate restTemplate;

    private static final String HN_TOP_STORIES = "https://hacker-news.firebaseio.com/v0/topstories.json";
    private static final String HN_ITEM = "https://hacker-news.firebaseio.com/v0/item/{id}.json";
    private static final int FETCH_LIMIT = 20;

    public List<Article> fetchAndSaveTopStories() {
        log.info("Fetching top stories from HackerNews...");

        int[] storyIds = restTemplate.getForObject(HN_TOP_STORIES, int[].class);

        if (storyIds == null) {
            log.warn("No story IDs returned from HackerNews");
            return List.of();
        }

        List<Article> saved = new ArrayList<>();

        for (int i = 0; i < Math.min(FETCH_LIMIT, storyIds.length); i++) {
            try {
                Map item = restTemplate.getForObject(HN_ITEM, Map.class, storyIds[i]);

                if (item == null || item.get("url") == null || item.get("title") == null) {
                    continue;
                }

                String url = (String) item.get("url");
                String title = (String) item.get("title");

                if (articleRepository.existsByUrl(url)) {
                    log.debug("Skipping duplicate article: {}", url);
                    continue;
                }

                Article article = new Article();
                article.setTitle(title);
                article.setUrl(url);
                article.setSource("HackerNews");
                article.setFetchedAt(LocalDateTime.now());

                saved.add(articleRepository.save(article));
                log.info("Saved article: {}", title);

            } catch (Exception e) {
                log.warn("Failed to fetch story {}: {}", storyIds[i], e.getMessage());
            }
        }

        log.info("Fetched and saved {} new articles", saved.size());
        return saved;
    }
}
