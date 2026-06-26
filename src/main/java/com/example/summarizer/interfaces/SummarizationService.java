package com.example.summarizer.interfaces;

import com.example.summarizer.enums.SummaryLength;
import com.example.summarizer.model.SavedSummary;
import com.example.summarizer.model.User;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public interface SummarizationService {
    String summarize(String text, SummaryLength length);

    SavedSummary summarizeAndSave(@Nullable User user, String text, SummaryLength length);
    SavedSummary summarizeFromFile(User user, MultipartFile file) throws Exception;
    SavedSummary summarizeFromUrl(User user, String url) throws IOException;
    void summarizeAsync(String text, SummaryLength length, SseEmitter emitter);
    List<SavedSummary> getUserSummaries(User user);
    SavedSummary getSummary(Long id, User user);
    void deleteSummary(Long id, User user);
    List<SavedSummary> getUserSummariesBySourceType(User user, String sourceType);
    Object getUserStatistics(User user);
}
