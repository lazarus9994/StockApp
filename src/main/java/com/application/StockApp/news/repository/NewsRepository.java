package com.application.StockApp.news.repository;

import com.application.StockApp.news.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    Optional<News> findByExternalId(String externalId);

    List<News> findByStockCodeOrderByPublishedAtDesc(String stockCode);
}
