package com.example.summarizer.service;


import com.example.summarizer.dto.response.SummaryResponse;
import com.example.summarizer.enums.SourceType;
import com.example.summarizer.enums.SummaryLength;

import com.example.summarizer.interfaces.DocumentParsingService;
import com.example.summarizer.interfaces.SummarizationService;
import com.example.summarizer.interfaces.WebScrapingService;
import com.example.summarizer.model.SavedSummary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SummarizationServiceImpl implements SummarizationService {

  private final ChatClient chatClient;
  private final DocumentParsingService documentParsingService;
  private final WebScrapingService webScrapingService;

  public SummarizationServiceImpl(ChatClient.Builder chatClientBuilder,

                                  DocumentParsingService documentParsingService,
                                  WebScrapingService webScrapingService) {
    this.chatClient = chatClientBuilder.build();
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
  public SavedSummary summarizeAndSave(String text, SummaryLength length) {


    String summary = summarize(text, length);
    // TODO: Dummy Save to avoid daily API limit
//    SavedSummary saved = new SavedSummary();
//    saved.setOriginalText(text);
//    saved.setSummary("A quick overview of the topic with only the essential points.");
//    saved.setSourceType(SourceType.TEXT.toString());
//    saved.setOriginalWordCount(120);
//    saved.setSummaryWordCount(22);
//    saved.setSummaryLength(length.toString());
//    saved.setCreatedAt(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    SavedSummary saved = new SavedSummary();
    saved.setOriginalText(text);
    saved.setSummary(summary);
    saved.setSourceType(SourceType.TEXT.toString());
    saved.setOriginalWordCount(wordCount(text));
    saved.setSummaryWordCount(wordCount(summary));
    saved.setSummaryLength(length.toString());
    saved.setCreatedAt(LocalDateTime.now());

    return saved;
  }

  // ============================================================
  // 2. FILE SUMMARIZATION
  // ============================================================

  @Override
  public SavedSummary summarizeFromFile(MultipartFile file) throws Exception {
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
    saved.setOriginalText(text);
    saved.setSummary(summary);
    saved.setSourceType(SourceType.FILE.toString());
    saved.setOriginalWordCount(wordCount(text));
    saved.setSummaryWordCount(wordCount(summary));
    saved.setSummaryLength(SummaryLength.SHORT.toString());
    saved.setCreatedAt(LocalDateTime.now());


    return saved;
  }

  // ============================================================
  // 3. URL SUMMARIZATION
  // ============================================================

  @Override
  public SavedSummary summarizeFromUrl(String url) throws IOException {
    log.info("Summarizing URL: {}", url);

    // Extract content from URL using Jsoup
    String text = webScrapingService.extractContent(url);
    log.info("Extracted {} characters from URL", text.length());

    String summary = summarize(text, SummaryLength.SHORT);

    SavedSummary saved = new SavedSummary();
    saved.setOriginalText(text);
    saved.setSummary(summary);
    saved.setSourceType(SourceType.URL.toString());
    saved.setSourceUrl(url);
    saved.setOriginalWordCount(wordCount(text));
    saved.setSummaryWordCount(wordCount(summary));
    saved.setSummaryLength(SummaryLength.SHORT.toString());
    saved.setCreatedAt(LocalDateTime.now());

    return saved;
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
  // Handled on frontend

  // ============================================================
  // 6. DELETE SUMMARY
  // ============================================================
  // Handled on frontend

  // ============================================================
  // 7. FILTER SUMMARIES
  // ============================================================
  // Handled on frontend

  // ============================================================
  // 8. STATISTICS
  // ============================================================
  // Handled on frontend

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
    String sizeClause = switch (length) {
      case TLDR -> "Summarize this in 1-2 sentences";
      case SHORT -> "Summarize this in 3-5 sentences";
      case MEDIUM -> "Summarize this in 5-10 sentences";
      case DETAILED -> "Create a comprehensive summary (approximately 1/3 of original length)";
    };
    return String.format(sizeClause + "\nAlso the summary MUST be smaller than the text:\n\n%s", text);

  }

  private int wordCount(String text) {
    if (text == null || text.trim().isEmpty()) {
      return 0;
    }
    return text.trim().split("\\s+").length;
  }
}