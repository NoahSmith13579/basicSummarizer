package com.example.summarizer.service;


import com.example.summarizer.enums.SourceType;
import com.example.summarizer.enums.SummaryLength;

import com.example.summarizer.exception.InvalidInputException;
import com.example.summarizer.exception.ResourceNotFoundException;
import com.example.summarizer.interfaces.DocumentParsingService;
import com.example.summarizer.interfaces.SummarizationService;
import com.example.summarizer.interfaces.WebScrapingService;
import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.model.User;

import com.example.summarizer.repository.SavedSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class SummarizationServiceImpl implements SummarizationService {

    private final ChatClient chatClient;
    private final SavedSummaryRepository summaryRepository;
    private final DocumentParsingService documentParsingService;
    private final WebScrapingService webScrapingService;

    public SummarizationServiceImpl(ChatClient.Builder chatClientBuilder,
                                    SavedSummaryRepository summaryRepository,
                                    DocumentParsingService documentParsingService,
                                    WebScrapingService webScrapingService){
        this.chatClient = chatClientBuilder.build();
        this.summaryRepository = summaryRepository;
        this.documentParsingService = documentParsingService;
        this.webScrapingService = webScrapingService;
    }

    // ============================================================
    // 1. PLAIN TEXT SUMMARIZATION
    // ============================================================

    @Override
    public String summarize(String text, SummaryLength length) {
        log.info("Summarizing {} characters", text.length());

        String prompt = createSummarizationPrompt(text, length);

        try {
            String summary = chatClient
                    .prompt(prompt)
                    .call()
                    .content();

            log.info("Summarization complete. Original: {}, Summary: {}",
                    wordCount(text), wordCount(summary));

            return summary;

        } catch (Exception e) {
            log.error("Error calling LLM", e);
            throw new RuntimeException("Failed to summarize: " + e.getMessage());
        }
    }

    @Override
    public SavedSummary summarizeAndSave(User user, String text, SummaryLength length) {
        // user may be null for anonymous/unauthenticated requests
        log.info("Summarizing and saving for user: {}",
                user != null ? user.getUsername() : "anonymous");

        String summary = summarize(text, length);

        SavedSummary saved = new SavedSummary();
        saved.setUser(user);          // null is valid — persisted as no FK
        saved.setOriginalText(text);
        saved.setSummary(summary);
        saved.setSourceType(SourceType.TEXT.toString());
        saved.setOriginalWordCount(wordCount(text));
        saved.setSummaryWordCount(wordCount(summary));
        saved.setSummaryLength(length.toString());
        saved.setCreatedAt(LocalDateTime.now());

        SavedSummary result = summaryRepository.save(saved);
        log.info("Saved summary with ID: {}", result.getId());
        return result;
    }

    // ============================================================
    // 2. FILE SUMMARIZATION
    // ============================================================

    @Override
    public SavedSummary summarizeFromFile(User user, MultipartFile file) throws Exception {
        log.info("Summarizing file: {}", file.getOriginalFilename());

        // Parse file using Tika
        String text = documentParsingService.parseFile(file);
        log.info("Extracted {} characters from file", text.length());

        // For large documents, use chunking
        String summary;
        if (wordCount(text) > 3000) {
            summary = summarizeLargeDocument(text, SummaryLength.SHORT);
        } else {
            summary = summarize(text, SummaryLength.SHORT);
        }

        SavedSummary saved = new SavedSummary();
        saved.setUser(user);
        saved.setOriginalText(text);
        saved.setSummary(summary);
        saved.setSourceType(SourceType.FILE.toString());
        saved.setOriginalWordCount(wordCount(text));
        saved.setSummaryWordCount(wordCount(summary));
        saved.setSummaryLength(SummaryLength.SHORT.toString());
        saved.setCreatedAt(LocalDateTime.now());

        SavedSummary result = summaryRepository.save(saved);
        log.info("File summarization saved with ID: {}", result.getId());
        return result;
    }

    // ============================================================
    // 3. URL SUMMARIZATION
    // ============================================================

    @Override
    public SavedSummary summarizeFromUrl(User user, String url) throws IOException {
        log.info("Summarizing URL: {}", url);

        // Extract content from URL using Jsoup
        String text = webScrapingService.extractContent(url);
        log.info("Extracted {} characters from URL", text.length());

        String summary = summarize(text, SummaryLength.SHORT);

        SavedSummary saved = new SavedSummary();
        saved.setUser(user);
        saved.setOriginalText(text);
        saved.setSummary(summary);
        saved.setSourceType(SourceType.URL.toString());
        saved.setSourceUrl(url);
        saved.setOriginalWordCount(wordCount(text));
        saved.setSummaryWordCount(wordCount(summary));
        saved.setSummaryLength(SummaryLength.SHORT.toString());
        saved.setCreatedAt(LocalDateTime.now());

        SavedSummary result = summaryRepository.save(saved);
        log.info("URL summarization saved with ID: {}", result.getId());
        return result;
    }

    // ============================================================
    // 4. STREAMING ASYNC SUMMARIZATION
    // ============================================================

    @Override
    @Async
    public void summarizeAsync(String text, SummaryLength length, SseEmitter emitter) {
        log.info("Starting async summarization for streaming");

        try {
            String prompt = createSummarizationPrompt(text, length);

            chatClient.prompt(prompt)
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .data(chunk)
                                    .name("summary"));
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .doOnError(error -> {
                        try {
                            emitter.send(SseEmitter.event()
                                    .data("Error: " + error.getMessage())
                                    .name("error"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .doOnComplete(() -> {
                        emitter.complete();
                        log.info("Streaming complete");
                    })
                    .blockLast();

        } catch (Exception e) {
            log.error("Async summarization error", e);
            emitter.completeWithError(e);
        }
    }

    // ============================================================
    // 5. RETRIEVE SUMMARIES
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<SavedSummary> getUserSummaries(User user) {
        log.info("Fetching summaries for user: {}", user.getUsername());
        return summaryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public SavedSummary getSummary(Long id, User user) {
        log.info("Fetching summary: {} for user: {}", id, user.getUsername());

        return summaryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Summary not found or you don't have permission to access it"));
    }

    // ============================================================
    // 6. DELETE SUMMARY
    // ============================================================

    @Override
    public void deleteSummary(Long id, User user) {
        log.info("Deleting summary: {} for user: {}", id, user.getUsername());

        SavedSummary summary = summaryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Summary not found"));

        summaryRepository.delete(summary);
        log.info("Summary deleted: {}", id);
    }

    // ============================================================
    // 7. FILTER SUMMARIES
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<SavedSummary> getUserSummariesBySourceType(User user, String sourceType) {
        log.info("Filtering summaries by source: {} for user: {}", sourceType, user.getUsername());

        try {
            SourceType type = SourceType.valueOf(sourceType.toUpperCase());
            return summaryRepository.findByUserAndSourceType(user, type.toString());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid source type: {}", sourceType);
            throw new InvalidInputException("Invalid source type. Must be TEXT, FILE, or URL");
        }
    }

    // ============================================================
    // 8. STATISTICS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(User user) {
        log.info("Calculating statistics for user: {}", user.getUsername());

        List<SavedSummary> summaries = summaryRepository.findByUser(user);

        long totalSummaries = summaries.size();
        long totalOriginalChars = summaries.stream()
                .mapToLong(s -> s.getOriginalText().length())
                .sum();
        long totalSummaryChars = summaries.stream()
                .mapToLong(s -> s.getSummary().length())
                .sum();

        double savedChars = totalOriginalChars - totalSummaryChars;
        double avgCompression = totalSummaries > 0
                ? (savedChars / totalOriginalChars) * 100
                : 0;

        // Estimate reading time saved (average 200 words per minute)
        int avgReadingSpeed = 200;
        long savedWords = summaries.stream()
                .mapToLong(s -> s.getOriginalWordCount() - s.getSummaryWordCount())
                .sum();
        double minutesSaved = savedWords / (double) avgReadingSpeed;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSummaries", totalSummaries);
        stats.put("totalOriginalCharacters", totalOriginalChars);
        stats.put("totalCharactersSaved", (long) savedChars);
        stats.put("averageCompressionRatio", Math.round(avgCompression * 100.0) / 100.0);
        stats.put("estimatedMinutesSaved", Math.round(minutesSaved * 100.0) / 100.0);
        stats.put("totalSummarizations", totalSummaries);

        return stats;
    }

    // ============================================================
    // HELPER METHODS
    // ============================================================

    private String summarizeLargeDocument(String text, SummaryLength length) {
        log.info("Document too large ({}), using chunking strategy", wordCount(text));

        List<String> chunks = chunkDocument(text, 12000);
        log.info("Split into {} chunks", chunks.size());

        List<String> chunkSummaries = chunks.stream()
                .map(chunk -> {
                    try {
                        return summarize(chunk, SummaryLength.SHORT);
                    } catch (Exception e) {
                        log.error("Error summarizing chunk", e);
                        return "";
                    }
                })
                .collect(Collectors.toList());

        String combined = String.join("\n\n", chunkSummaries);
        return summarize(combined, length);
    }

    private List<String> chunkDocument(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();
        String[] paragraphs = text.split("\n\n");

        StringBuilder currentChunk = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (currentChunk.length() + paragraph.length() > chunkSize) {
                if (!currentChunk.isEmpty()) {
                    chunks.add(currentChunk.toString());
                }
                currentChunk = new StringBuilder(paragraph);
            } else {
                if (!currentChunk.isEmpty()) {
                    currentChunk.append("\n\n");
                }
                currentChunk.append(paragraph);
            }
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }

    private String createSummarizationPrompt(String text, SummaryLength length) {
        return switch (length) {
            case TLDR -> String.format(
                    "Summarize this in 1-2 sentences:\n\n%s", text);
            case SHORT -> String.format(
                    "Summarize this in 3-5 sentences:\n\n%s", text);
            case MEDIUM -> String.format(
                    "Summarize this in 5-10 sentences:\n\n%s", text);
            case DETAILED -> String.format(
                    "Create a comprehensive summary (approximately 1/3 of original length):\n\n%s", text);
        };
    }

    private int wordCount(String text) {
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        return text.trim().split("\\s+").length;
    }
}