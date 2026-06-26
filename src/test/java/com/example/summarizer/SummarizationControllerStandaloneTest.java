package com.example.summarizer;

import com.example.summarizer.SummarizerApplication;
import com.example.summarizer.controller.SummarizationController;
import com.example.summarizer.enums.SummaryLength;
import com.example.summarizer.interfaces.DocumentParsingService;
import com.example.summarizer.interfaces.SummarizationService;
import com.example.summarizer.interfaces.UserService;
import com.example.summarizer.interfaces.WebScrapingService;
import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true", classes = SummarizerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import({SummarizationControllerStandaloneTest.TestSecurityConfig.class, SummarizationControllerStandaloneTest.TestExceptionHandler.class})
class SummarizationControllerStandaloneTest {

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(com.example.summarizer.exception.InvalidInputException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        void handleInvalid() {}

        @ExceptionHandler(com.example.summarizer.exception.ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        void handleNotFound() {}
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SummarizationService summarizationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WebScrapingService webScrapingService;

    @MockitoBean
    private DocumentParsingService documentParsingService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(summarizationService.summarizeAndSave(any(), anyString(), any(SummaryLength.class)))
                .thenReturn(new SavedSummary());
    }

    @Test
    void springObjectMapperCanDeserializeSummarizeRequest() throws Exception {
        var json = "{\"text\":\"Some long text to summarize.\",\"summaryLength\":\"SHORT\"}";
        var request = objectMapper.readValue(json, com.example.summarizer.dto.request.SummarizeRequest.class);

        assertThat(request).isNotNull();
        assertThat(request.getText()).isEqualTo("Some long text to summarize.");
        assertThat(request.getSummaryLength()).isEqualTo(com.example.summarizer.enums.SummaryLength.SHORT);
    }

    @Test
    @WithMockUser(username = "testuser")
    void simpleSummarizeJsonRequest() throws Exception {
        mockMvc.perform(post("/api/v1/summarize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Some long text to summarize.\",\"summaryLength\":\"SHORT\"}"))
                .andExpect(status().isCreated());
    }
}
