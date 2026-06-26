package com.example.summarizer.controller;

import com.example.summarizer.dto.request.SummarizeRequest;
import com.example.summarizer.dto.request.UrlRequest;
import com.example.summarizer.dto.response.SummaryResponse;
import com.example.summarizer.enums.SummaryLength;
import com.example.summarizer.exception.InvalidInputException;
import com.example.summarizer.exception.ResourceNotFoundException;
import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.model.User;
import com.example.summarizer.interfaces.SummarizationService;
import com.example.summarizer.interfaces.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/summarize")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Summarization", description = "Content summarization endpoints")
//@SecurityRequirement(name = "bearer-jwt")
public class SummarizationController {

    private final SummarizationService summarizationService;
    private final UserService userService;

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
    public String index(){
        return "Test Homepage";
    }

    // ============================================================
    // 1. PLAIN TEXT SUMMARIZATION
    // ============================================================

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Summarize plain text")
    public ResponseEntity<SummaryResponse> summarize(
            @Valid @RequestBody SummarizeRequest request,
            @AuthenticationPrincipal UserDetails principal) {

        User domainUser = principal != null
                ? userService.getUserByUsername(principal.getUsername())
                : null;

        SavedSummary saved = summarizationService.summarizeAndSave(
                domainUser,
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
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) Authentication authentication) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new InvalidInputException("File cannot be empty");
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        SavedSummary saved = summarizationService.summarizeFromFile(user, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(SummaryResponse.from(saved));
    }

    // ============================================================
    // 3. URL SUMMARIZATION
    // ============================================================

    @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Summarize content from URL")
    public ResponseEntity<SummaryResponse> summarizeUrl(
            @Valid @RequestBody UrlRequest request,
            @Parameter(hidden = true) Authentication authentication) throws IOException {

        String url = request.getUrl();

        if (url == null || url.trim().isEmpty()) {
            throw new InvalidInputException("URL cannot be empty");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new InvalidInputException("URL must start with http:// or https://");
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        SavedSummary saved = summarizationService.summarizeFromUrl(user, url);
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

    // ============================================================
    // 5. GET ALL SUMMARIES
    // ============================================================

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get user's saved summaries")
    public ResponseEntity<List<SummaryResponse>> getSummaries(
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        List<SummaryResponse> response = summarizationService.getUserSummaries(user)
                .stream()
                .map(SummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 6. GET SPECIFIC SUMMARY
    // ============================================================

    @GetMapping("/{id}")
    @Operation(summary = "Get summary by ID")
    public ResponseEntity<SummaryResponse> getSummary(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        SavedSummary summary = summarizationService.getSummary(id, user);
        return ResponseEntity.ok(SummaryResponse.from(summary));
    }

    // ============================================================
    // 7. DELETE SUMMARY
    // ============================================================

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete summary")
    public ResponseEntity<Void> deleteSummary(
            @PathVariable Long id,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        summarizationService.deleteSummary(id, user);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // 8. FILTER BY SOURCE
    // ============================================================

    @GetMapping("/filter/{sourceType}")
    @Operation(summary = "Filter by source type (TEXT, FILE, URL)")
    public ResponseEntity<List<SummaryResponse>> filterBySource(
            @PathVariable String sourceType,
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        List<SummaryResponse> response = summarizationService
                .getUserSummariesBySourceType(user, sourceType)
                .stream()
                .map(SummaryResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // ============================================================
    // 9. STATISTICS
    // ============================================================

    @GetMapping("/stats/overview")
    @Operation(summary = "Get statistics")
    public ResponseEntity<?> getStatistics(
            @Parameter(hidden = true) Authentication authentication) {

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        return ResponseEntity.ok(summarizationService.getUserStatistics(user));
    }

    // ============================================================
    // HEALTH CHECK
    // ============================================================

    @GetMapping("/health")
    @Operation(summary = "Health check — confirms the API is reachable")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "service", "summarizer-api"
        ));
    }
}
