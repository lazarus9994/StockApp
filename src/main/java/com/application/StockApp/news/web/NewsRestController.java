package com.application.StockApp.news.web;

import com.application.StockApp.news.service.NewsService;
import com.application.StockApp.news.web.dto.NewsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsRestController {

    private final NewsService newsService;

    @PostMapping("/receive")
    public String receiveNews(@RequestBody List<NewsDto> newsList) {

        int saved = 0;
        for (NewsDto dto : newsList) {
            if (newsService.saveIfNotExists(dto)) {
                saved++;
            }
        }

        return "Saved " + saved + " news items.";
    }
}
