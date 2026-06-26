package com.example.summarizer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebScrapingServiceImpl implements com.example.summarizer.interfaces.WebScrapingService {

    public String extractContent(String url) throws IOException {
        log.info("Scraping URL: {}", url);

        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(10000)
                .get();

        // Find main article content
        Element article = doc.selectFirst("article, main, [role=main]");
        if (article == null) {
            article = doc.selectFirst("body");
        }

        // Remove noise
        article.select("script, style, nav, footer, .ad, .advertisement, aside").remove();

        String text = article.text();
        log.info("Extracted {} characters", text.length());

        return text.trim();
    }
}
