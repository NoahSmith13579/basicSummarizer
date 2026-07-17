package com.example.summarizer.controller;

import com.example.summarizer.dto.request.SummarizeRequest;
import com.example.summarizer.dto.request.UrlRequest;
import com.example.summarizer.dto.response.SummaryResponse;
import com.example.summarizer.enums.SummaryLength;
import com.example.summarizer.exception.InvalidInputException;
import com.example.summarizer.exception.ResourceNotFoundException;
import com.example.summarizer.interfaces.SummarizationControllerContract;
import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.interfaces.SummarizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/summarize")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Summarization", description = "Content summarization endpoints")
public class SummarizationController implements SummarizationControllerContract {

  private final SummarizationService summarizationService;


  // ============================================================
  // GLOBAL EXCEPTION HANDLER
  // ============================================================

  @RestControllerAdvice
  public static class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<Map<String, String>> handleInvalidInput(InvalidInputException ex) {
      return ResponseEntity.badRequest()
              .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
      return ResponseEntity.badRequest().body(
              ex.getBindingResult().getAllErrors().get(0).getDefaultMessage()
      );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
      ex.printStackTrace();
      return ResponseEntity.status(500).body(ex.getMessage());
    }
  }

  // ==
  // 0. Test Homepage
  // ==
  @GetMapping("/")
  public String index() {
    return "Test Homepage";
  }

  // ============================================================
  // 1. PLAIN TEXT SUMMARIZATION
  // ============================================================
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Summarize plain text")
  public ResponseEntity<SummaryResponse> summarize(
          @Valid @RequestBody SummarizeRequest request) {

    SavedSummary saved = summarizationService.summarizeAndSave(
            request.getText(),
            request.getSummaryLength()
    );

    return ResponseEntity.status(HttpStatus.CREATED).body(SummaryResponse.from(saved));
  }

  // ============================================================
  // 2. FILE UPLOAD
  // ============================================================

  @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Summarize uploaded file (PDF, DOCX, TXT)")
  public ResponseEntity<SummaryResponse> summarizeFile(
          @RequestParam("file") MultipartFile file, @RequestParam("summaryLength") SummaryLength length) throws Exception {

    if (file == null || file.isEmpty()) {
      throw new InvalidInputException("File cannot be empty");
    }

    SavedSummary saved = summarizationService.summarizeFromFile(file, length);
    return ResponseEntity.status(HttpStatus.CREATED).body(SummaryResponse.from(saved));
  }

  // ============================================================
  // 3. URL SUMMARIZATION
  // ============================================================

  @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(summary = "Summarize content from URL")
  public ResponseEntity<SummaryResponse> summarizeUrl(
          @Valid @RequestBody UrlRequest request) throws IOException {

    String url = request.getUrl();

    if (url == null || url.trim().isEmpty()) {
      throw new InvalidInputException("URL cannot be empty");
    }
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
      throw new InvalidInputException("URL must start with http:// or https://");
    }
    SavedSummary saved = summarizationService.summarizeFromUrl(url, request.getSummaryLength());
    return ResponseEntity.status(HttpStatus.CREATED).body(SummaryResponse.from(saved));
  }

  // ============================================================
  // 4. STREAMING
  // ============================================================

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "Stream summary in real-time")
  public SseEmitter summarizeStream(
          @RequestParam String text,
          @RequestParam(defaultValue = "SHORT") SummaryLength length) {

    if (text == null || text.trim().isEmpty()) {
      throw new InvalidInputException("Text cannot be empty");
    }

    SseEmitter emitter = new SseEmitter(300000L);
    summarizationService.summarizeAsync(text, length, emitter);
    return emitter;
  }
  
  @GetMapping("/health")
  @Operation(summary = "Health check — confirms the API is reachable")
  public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of(
            "status", "ok",
            "service", "summarizer-api"
    ));
  }
}
