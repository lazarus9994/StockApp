package com.application.StockApp.news.service;

import com.application.StockApp.news.model.News;
import com.application.StockApp.news.repository.NewsRepository;
import com.application.StockApp.news.web.dto.NewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;

    public boolean saveIfNotExists(NewsDto dto) {

        // Duplicate protection
        if (dto.getExternalId() != null &&
                newsRepository.findByExternalId(dto.getExternalId()).isPresent()) {
            return false;
        }

        News news = News.builder()
                .stockCode(dto.getStockCode())
                .headline(dto.getHeadline())
                .content(dto.getContent())
                .source(dto.getSource())
                .url(dto.getUrl())
                .externalId(dto.getExternalId())
                .publishedAt(dto.getPublishedAt())
                .receivedAt(LocalDateTime.now())
                .build();

        newsRepository.save(news);
        return true;
    }

    public List<News> getByStock(String stockCode) {
        return newsRepository.findByStockCodeOrderByPublishedAtDesc(stockCode);
    }

    public List<News> getLatestNews() {
        return newsRepository.findAll().subList(0, 10);
    }
}
