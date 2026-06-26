package com.example.summarizer.controller;

import com.example.summarizer.config.SecurityConfig;
import com.example.summarizer.dto.request.SummarizeRequest;
import com.example.summarizer.dto.request.UrlRequest;
import com.example.summarizer.enums.SummaryLength;
import com.example.summarizer.exception.InvalidInputException;
import com.example.summarizer.exception.ResourceNotFoundException;
import com.example.summarizer.interfaces.SummarizationService;
import com.example.summarizer.interfaces.UserService;
import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for {@link SummarizationController}.
 *
 * <p>Uses {@code @WebMvcTest} to load only the web layer; all service
 * dependencies are replaced with Mockito mocks. Spring Security is kept active
 * so that authentication/authorisation behaviour is verified as well.
 *
 * <p>Test organisation mirrors the controller sections:
 * <ol>
 *   <li>Plain-text summarisation  — POST /api/v1/summarize</li>
 *   <li>File upload               — POST /api/v1/summarize/file</li>
 *   <li>URL summarisation         — POST /api/v1/summarize/url</li>
 *   <li>Streaming                 — GET  /api/v1/summarize/stream</li>
 *   <li>Get all summaries         — GET  /api/v1/summarize</li>
 *   <li>Get single summary        — GET  /api/v1/summarize/{id}</li>
 *   <li>Delete summary            — DELETE /api/v1/summarize/{id}</li>
 *   <li>Filter by source          — GET  /api/v1/summarize/filter/{sourceType}</li>
 *   <li>Statistics                — GET  /api/v1/summarize/stats/overview</li>
 *   <li>Health check              — GET  /api/v1/summarize/health</li>
 * </ol>
 */
@WebMvcTest(SummarizationController.class)
@Import({ObjectMapper.class, SecurityConfig.class})
@DisplayName("SummarizationController")
class SummarizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SummarizationService summarizationService;

    @MockitoBean
    private UserService userService;

    // ----------------------------------------------------------------
    // Shared fixtures
    // ----------------------------------------------------------------

    private static final String BASE_URL      = "/api/v1/summarize";
    // NOTE: @WithMockUser requires a compile-time string literal, so "alice" is
    // repeated in each annotation. TEST_USERNAME is still used in mock stubs.
    private static final String TEST_USERNAME = "alice";

    private User        testUser;
    private SavedSummary testSummary;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, TEST_USERNAME, "alice@example.com", "hashed");

        testSummary = new SavedSummary();
        testSummary.setId(42L);
        testSummary.setUser(testUser);
        testSummary.setOriginalText("The quick brown fox jumps over the lazy dog.");
        testSummary.setSummary("A fox jumps over a dog.");
        testSummary.setSourceType("TEXT");
        testSummary.setOriginalWordCount(9);
        testSummary.setSummaryWordCount(5);
        testSummary.setSummaryLength("SHORT");
        testSummary.setCreatedAt(LocalDateTime.of(2024, 6, 1, 12, 0));
    }

    // ================================================================
    // 1. PLAIN TEXT SUMMARIZATION  —  POST /api/v1/summarize
    // ================================================================

    @Nested
    @DisplayName("POST /api/v1/summarize — plain text")
    class PlainTextSummarization {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("201 with valid request and authenticated user")
        void summarize_validRequest_authenticated_returns201() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeAndSave(eq(testUser), anyString(), eq(SummaryLength.SHORT)))
                    .thenReturn(testSummary);

            SummarizeRequest request = new SummarizeRequest(
                    "The quick brown fox jumps over the lazy dog.",
                    SummaryLength.SHORT);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.summary").value("A fox jumps over a dog."))
                    .andExpect(jsonPath("$.sourceType").value("TEXT"))
                    .andExpect(jsonPath("$.originalWordCount").value(9))
                    .andExpect(jsonPath("$.summaryWordCount").value(5))
                    .andExpect(jsonPath("$.compressionRatio").value(44.44))
                    .andExpect(jsonPath("$.summaryLength").value("SHORT"))
                    .andExpect(jsonPath("$.savedAt").value("2024-06-01T12:00:00"))
                    .andExpect(jsonPath("$.sourceUrl").doesNotExist());

            verify(summarizationService).summarizeAndSave(
                    testUser,
                    "The quick brown fox jumps over the lazy dog.",
                    SummaryLength.SHORT);
        }

        @Test
        @DisplayName("201 with valid request and anonymous principal (null user saved)")
        void summarize_validRequest_anonymous_returns201() throws Exception {
            // No @WithMockUser → principal is null; service must be called with null user
            when(summarizationService.summarizeAndSave(isNull(), anyString(), any()))
                    .thenReturn(testSummary);

            SummarizeRequest request = new SummarizeRequest(
                    "The quick brown fox jumps over the lazy dog.",
                    SummaryLength.SHORT);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            verify(summarizationService).summarizeAndSave(
                    isNull(),
                    "The quick brown fox jumps over the lazy dog.",
                    SummaryLength.SHORT);
            verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when text is blank")
        void summarize_blankText_returns400() throws Exception {
            SummarizeRequest request = new SummarizeRequest("   ", SummaryLength.SHORT);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(summarizationService);
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when text is below minimum length (< 10 chars)")
        void summarize_textTooShort_returns400() throws Exception {
            SummarizeRequest request = new SummarizeRequest("Short.", SummaryLength.SHORT);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when summaryLength field is missing")
        void summarize_missingSummaryLength_returns400() throws Exception {
            String body = "{\"text\":\"The quick brown fox jumps over the lazy dog.\"}";

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("201 for each SummaryLength enum variant")
        void summarize_allLengthVariants_eachReturns201() throws Exception {
            for (SummaryLength length : SummaryLength.values()) {
                when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
                when(summarizationService.summarizeAndSave(eq(testUser), anyString(), eq(length)))
                        .thenReturn(testSummary);

                SummarizeRequest req = new SummarizeRequest(
                        "The quick brown fox jumps over the lazy dog.", length);

                mockMvc.perform(post(BASE_URL)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isCreated());
            }
        }
    }

    // ================================================================
    // 2. FILE UPLOAD  —  POST /api/v1/summarize/file
    // ================================================================

    @Nested
    @DisplayName("POST /api/v1/summarize/file — file upload")
    class FileUpload {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("201 with a valid PDF upload")
        void summarizeFile_validPdf_returns201() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeFromFile(eq(testUser), any()))
                    .thenReturn(testSummary);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE,
                    "PDF content bytes".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/file")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(42));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("201 with a plain-text file upload")
        void summarizeFile_validTxtFile_returns201() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeFromFile(eq(testUser), any()))
                    .thenReturn(testSummary);

            MockMultipartFile file = new MockMultipartFile(
                    "file", "document.txt", MediaType.TEXT_PLAIN_VALUE,
                    "Some meaningful text content here.".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/file")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when the uploaded file is empty")
        void summarizeFile_emptyFile_returns400() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);

            MockMultipartFile emptyFile = new MockMultipartFile(
                    "file", "empty.pdf", MediaType.APPLICATION_PDF_VALUE, new byte[0]);

            mockMvc.perform(multipart(BASE_URL + "/file")
                            .file(emptyFile)
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("500 when service throws a parse exception")
        void summarizeFile_serviceThrows_returns500() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeFromFile(eq(testUser), any()))
                    .thenThrow(new RuntimeException("Tika parse error"));

            MockMultipartFile file = new MockMultipartFile(
                    "file", "broken.pdf", MediaType.APPLICATION_PDF_VALUE,
                    "broken content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/file")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("401 when not authenticated")
        void summarizeFile_unauthenticated_returns401() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", MediaType.APPLICATION_PDF_VALUE,
                    "content".getBytes());

            mockMvc.perform(multipart(BASE_URL + "/file")
                            .file(file)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 3. URL SUMMARIZATION  —  POST /api/v1/summarize/url
    // ================================================================

    @Nested
    @DisplayName("POST /api/v1/summarize/url — URL summarisation")
    class UrlSummarization {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("201 with a valid HTTPS URL")
        void summarizeUrl_validHttpsUrl_returns201() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeFromUrl(eq(testUser), eq("https://example.com/article")))
                    .thenReturn(testSummary);

            UrlRequest request = new UrlRequest("https://example.com/article");

            mockMvc.perform(post(BASE_URL + "/url")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(42));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("201 with a valid HTTP URL")
        void summarizeUrl_validHttpUrl_returns201() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeFromUrl(eq(testUser), eq("http://example.com")))
                    .thenReturn(testSummary);

            UrlRequest request = new UrlRequest("http://example.com");

            mockMvc.perform(post(BASE_URL + "/url")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when URL scheme is not http/https (e.g. ftp://)")
        void summarizeUrl_invalidScheme_returns400() throws Exception {
            UrlRequest request = new UrlRequest("ftp://example.com/file");

            mockMvc.perform(post(BASE_URL + "/url")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when URL field is blank")
        void summarizeUrl_blankUrl_returns400() throws Exception {
            String body = "{\"url\":\"\"}";

            mockMvc.perform(post(BASE_URL + "/url")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("500 when service throws IOException (unreachable host)")
        void summarizeUrl_ioException_returns500() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.summarizeFromUrl(eq(testUser), anyString()))
                    .thenThrow(new IOException("Connection refused"));

            UrlRequest request = new UrlRequest("https://unreachable.example.com");

            mockMvc.perform(post(BASE_URL + "/url")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("401 when not authenticated")
        void summarizeUrl_unauthenticated_returns401() throws Exception {
            UrlRequest request = new UrlRequest("https://example.com");

            mockMvc.perform(post(BASE_URL + "/url")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 4. STREAMING  —  GET /api/v1/summarize/stream
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/summarize/stream — SSE streaming")
    class Streaming {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 / SSE connection opened for valid text")
        void stream_validText_returns200() throws Exception {
            doNothing().when(summarizationService)
                    .summarizeAsync(anyString(), any(), any());

            mockMvc.perform(get(BASE_URL + "/stream")
                            .param("text", "The quick brown fox jumps over the lazy dog.")
                            .param("length", "SHORT"))
                    .andExpect(status().isOk());

            verify(summarizationService).summarizeAsync(
                    eq("The quick brown fox jumps over the lazy dog."),
                    eq(SummaryLength.SHORT),
                    any());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("Defaults to SummaryLength.SHORT when length param is absent")
        void stream_defaultLength_usesShort() throws Exception {
            doNothing().when(summarizationService)
                    .summarizeAsync(anyString(), any(), any());

            mockMvc.perform(get(BASE_URL + "/stream")
                            .param("text", "Some reasonable text to summarise here."))
                    .andExpect(status().isOk());

            verify(summarizationService)
                    .summarizeAsync(anyString(), eq(SummaryLength.SHORT), any());
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("Service is never called when text is blank")
        void stream_blankText_serviceNotCalled() throws Exception {
            // The controller catches InvalidInputException and pushes the error to
            // the emitter without invoking the async service method.
            mockMvc.perform(get(BASE_URL + "/stream")
                            .param("text", "   "))
                    .andReturn(); // status may vary by exception-handler config

            verify(summarizationService, never())
                    .summarizeAsync(eq("   "), any(), any());
        }
    }

    // ================================================================
    // 5. GET ALL SUMMARIES  —  GET /api/v1/summarize
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/summarize — list user summaries")
    class GetAllSummaries {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with list of summaries for the authenticated user")
        void getSummaries_authenticated_returnsOkWithList() throws Exception {
            SavedSummary second = new SavedSummary();
            second.setId(43L);
            second.setUser(testUser);
            second.setSummary("Another summary.");
            second.setSourceType("URL");
            second.setOriginalWordCount(8);
            second.setSummaryWordCount(2);
            second.setSummaryLength("TLDR");
            second.setCreatedAt(LocalDateTime.now());

            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserSummaries(testUser))
                    .thenReturn(List.of(testSummary, second));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].id").value(42))
                    .andExpect(jsonPath("$[1].id").value(43));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with empty array when user has no summaries")
        void getSummaries_noSummaries_returnsEmptyList() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserSummaries(testUser)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("401 when not authenticated")
        void getSummaries_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 6. GET SPECIFIC SUMMARY  —  GET /api/v1/summarize/{id}
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/summarize/{id} — single summary")
    class GetSingleSummary {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 when summary exists and belongs to the calling user")
        void getSummary_exists_returnsOk() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getSummary(42L, testUser)).thenReturn(testSummary);

            mockMvc.perform(get(BASE_URL + "/42"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(42))
                    .andExpect(jsonPath("$.summary").value("A fox jumps over a dog."))
                    .andExpect(jsonPath("$.savedAt").value("2024-06-01T12:00:00"));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("404 when summary does not exist or belongs to another user")
        void getSummary_notFound_returns404() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getSummary(99L, testUser))
                    .thenThrow(new ResourceNotFoundException("Summary not found"));

            mockMvc.perform(get(BASE_URL + "/99"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 when not authenticated")
        void getSummary_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/42"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 7. DELETE SUMMARY  —  DELETE /api/v1/summarize/{id}
    // ================================================================

    @Nested
    @DisplayName("DELETE /api/v1/summarize/{id} — delete summary")
    class DeleteSummary {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("204 when summary is deleted successfully")
        void deleteSummary_exists_returns204() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            doNothing().when(summarizationService).deleteSummary(42L, testUser);

            mockMvc.perform(delete(BASE_URL + "/42").with(csrf()))
                    .andExpect(status().isNoContent());

            verify(summarizationService).deleteSummary(42L, testUser);
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("404 when summary does not exist")
        void deleteSummary_notFound_returns404() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            doThrow(new ResourceNotFoundException("Summary not found"))
                    .when(summarizationService).deleteSummary(99L, testUser);

            mockMvc.perform(delete(BASE_URL + "/99").with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("401 when not authenticated")
        void deleteSummary_unauthenticated_returns401() throws Exception {
            mockMvc.perform(delete(BASE_URL + "/42").with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 8. FILTER BY SOURCE  —  GET /api/v1/summarize/filter/{sourceType}
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/summarize/filter/{sourceType} — source-type filter")
    class FilterBySource {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with TEXT summaries")
        void filter_textSourceType_returnsMatchingList() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserSummariesBySourceType(testUser, "TEXT"))
                    .thenReturn(List.of(testSummary));

            mockMvc.perform(get(BASE_URL + "/filter/TEXT"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].sourceType").value("TEXT"));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with FILE summaries")
        void filter_fileSourceType_returnsMatchingList() throws Exception {
            SavedSummary fileSummary = new SavedSummary();
            fileSummary.setId(55L);
            fileSummary.setUser(testUser);
            fileSummary.setSummary("File summary.");
            fileSummary.setSourceType("FILE");
            fileSummary.setOriginalWordCount(10);
            fileSummary.setSummaryWordCount(3);
            fileSummary.setSummaryLength("SHORT");
            fileSummary.setCreatedAt(LocalDateTime.now());

            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserSummariesBySourceType(testUser, "FILE"))
                    .thenReturn(List.of(fileSummary));

            mockMvc.perform(get(BASE_URL + "/filter/FILE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sourceType").value("FILE"));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with URL summaries")
        void filter_urlSourceType_returnsMatchingList() throws Exception {
            SavedSummary urlSummary = new SavedSummary();
            urlSummary.setId(66L);
            urlSummary.setUser(testUser);
            urlSummary.setSummary("Web page summary.");
            urlSummary.setSourceType("URL");
            urlSummary.setSourceUrl("https://example.com");
            urlSummary.setOriginalWordCount(20);
            urlSummary.setSummaryWordCount(4);
            urlSummary.setSummaryLength("SHORT");
            urlSummary.setCreatedAt(LocalDateTime.now());

            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserSummariesBySourceType(testUser, "URL"))
                    .thenReturn(List.of(urlSummary));

            mockMvc.perform(get(BASE_URL + "/filter/URL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].sourceType").value("URL"));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("400 when an unrecognised source type is provided")
        void filter_invalidSourceType_returns400() throws Exception {
            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserSummariesBySourceType(testUser, "INVALID"))
                    .thenThrow(new InvalidInputException(
                            "Invalid source type. Must be TEXT, FILE, or URL"));

            mockMvc.perform(get(BASE_URL + "/filter/INVALID"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("401 when not authenticated")
        void filter_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/filter/TEXT"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 9. STATISTICS  —  GET /api/v1/summarize/stats/overview
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/summarize/stats/overview — statistics")
    class Statistics {

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with full statistics map for a user with summaries")
        void getStatistics_withData_returnsStats() throws Exception {
            Map<String, Object> stats = Map.of(
                    "totalSummaries",           5L,
                    "totalOriginalCharacters",  10000L,
                    "totalCharactersSaved",     7500L,
                    "averageCompressionRatio",  75.0,
                    "estimatedMinutesSaved",    12.5,
                    "totalSummarizations",      5L
            );

            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserStatistics(testUser)).thenReturn(stats);

            mockMvc.perform(get(BASE_URL + "/stats/overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSummaries").value(5))
                    .andExpect(jsonPath("$.averageCompressionRatio").value(75.0))
                    .andExpect(jsonPath("$.estimatedMinutesSaved").value(12.5));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with all-zero stats for a new user with no summaries")
        void getStatistics_newUser_returnsZeroStats() throws Exception {
            Map<String, Object> emptyStats = Map.of(
                    "totalSummaries",           0L,
                    "totalOriginalCharacters",  0L,
                    "totalCharactersSaved",     0L,
                    "averageCompressionRatio",  0.0,
                    "estimatedMinutesSaved",    0.0,
                    "totalSummarizations",      0L
            );

            when(userService.getUserByUsername(TEST_USERNAME)).thenReturn(testUser);
            when(summarizationService.getUserStatistics(testUser)).thenReturn(emptyStats);

            mockMvc.perform(get(BASE_URL + "/stats/overview"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalSummaries").value(0));
        }

        @Test
        @DisplayName("401 when not authenticated")
        void getStatistics_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get(BASE_URL + "/stats/overview"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ================================================================
    // 10. HEALTH CHECK  —  GET /api/v1/summarize/health
    // ================================================================

    @Nested
    @DisplayName("GET /api/v1/summarize/health — health check")
    class HealthCheck {

        @Test
        @DisplayName("200 with status=ok without any authentication")
        void health_noAuth_returnsOk() throws Exception {
            mockMvc.perform(get(BASE_URL + "/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"))
                    .andExpect(jsonPath("$.service").value("summarizer-api"));
        }

        @Test
        @WithMockUser(username = "alice")
        @DisplayName("200 with status=ok when authenticated")
        void health_authenticated_returnsOk() throws Exception {
            mockMvc.perform(get(BASE_URL + "/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("ok"));
        }
    }
}