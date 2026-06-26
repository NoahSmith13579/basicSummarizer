package com.example.summarizer;

import com.example.summarizer.dto.request.SummarizeRequest;
import com.example.summarizer.dto.request.UrlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonBindCheckTest {

    @Test
    void summarizeRequestDeserializesWithJackson() throws Exception {
        String json = "{\"text\":\"Some long text to summarize.\",\"summaryLength\":\"SHORT\"}";
        ObjectMapper mapper = new ObjectMapper();
        SummarizeRequest req = mapper.readValue(json, SummarizeRequest.class);

        assertNotNull(req);
        assertEquals("Some long text to summarize.", req.getText());
        assertEquals(com.example.summarizer.enums.SummaryLength.SHORT, req.getSummaryLength());
    }

    @Test
    void urlRequestDeserializesWithJackson() throws Exception {
        String json = "{\"url\":\"https://example.com/article\"}";
        ObjectMapper mapper = new ObjectMapper();
        UrlRequest req = mapper.readValue(json, UrlRequest.class);

        assertNotNull(req);
        assertEquals("https://example.com/article", req.getUrl());
    }
}
