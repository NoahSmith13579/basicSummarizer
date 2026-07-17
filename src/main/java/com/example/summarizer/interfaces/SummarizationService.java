package com.example.summarizer.interfaces;

import com.example.summarizer.enums.SummaryLength;
import com.example.summarizer.model.SavedSummary;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

public interface SummarizationService {
  String summarize(String text, SummaryLength length);

  SavedSummary summarizeAndSave(String text, SummaryLength length);

  SavedSummary summarizeFromFile(MultipartFile file, SummaryLength length) throws Exception;

  SavedSummary summarizeFromUrl(String url, SummaryLength length) throws IOException;

  void summarizeAsync(String text, SummaryLength length, SseEmitter emitter);
}
