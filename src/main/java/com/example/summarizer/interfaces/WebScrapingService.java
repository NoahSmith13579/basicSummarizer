package com.example.summarizer.interfaces;

import java.io.IOException;

public interface WebScrapingService {
    public String extractContent(String url) throws IOException;
}
