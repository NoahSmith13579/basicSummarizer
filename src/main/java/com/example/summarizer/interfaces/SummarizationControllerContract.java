package com.example.summarizer.interfaces;

import com.example.summarizer.dto.request.SummarizeRequest;
import com.example.summarizer.dto.request.UrlRequest;
import com.example.summarizer.dto.response.SummaryResponse;
import com.example.summarizer.enums.SummaryLength;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface SummarizationControllerContract {

  // 0. Test Homepage
  String index();

  // 1. Plain text summarization
  ResponseEntity<SummaryResponse> summarize(SummarizeRequest request);

  // 2. File upload summarization
  ResponseEntity<SummaryResponse> summarizeFile(MultipartFile file) throws Exception;

  // 3. URL summarization
  ResponseEntity<SummaryResponse> summarizeUrl(UrlRequest request) throws IOException;

  // 4. Streaming summarization
  SseEmitter summarizeStream(String text, SummaryLength length);

  // Health check

}
