package com.example.summarizer.dto.response;

import com.example.summarizer.model.SavedSummary;

import java.time.LocalDateTime;

public class SummaryResponse {
    private Long id;
    private String summary;
    private Integer originalWordCount;
    private Integer summaryWordCount;
    private Double compressionRatio;
    private String sourceType;
    private String sourceUrl;
    private String summaryLength;
    private LocalDateTime savedAt;

    public SummaryResponse() {}
    public SummaryResponse(Long id, String summary, Integer originalWordCount, Integer summaryWordCount,
                           Double compressionRatio, String sourceType, String sourceUrl, String summaryLength, LocalDateTime savedAt) {
        this.id = id;
        this.summary = summary;
        this.originalWordCount = originalWordCount;
        this.summaryWordCount = summaryWordCount;
        this.compressionRatio = compressionRatio;
        this.sourceType = sourceType;
        this.sourceUrl = sourceUrl;
        this.summaryLength = summaryLength;
        this.savedAt = savedAt;
    }

    public static SummaryResponse from(SavedSummary entity) {
        double ratio = entity.getOriginalWordCount() > 0
                ? (1 - (double) entity.getSummaryWordCount() / entity.getOriginalWordCount()) * 100
                : 0;
        return new SummaryResponse(entity.getId(), entity.getSummary(), entity.getOriginalWordCount(),
                entity.getSummaryWordCount(), Math.round(ratio * 100.0) / 100.0, entity.getSourceType(),
                entity.getSourceUrl(), entity.getSummaryLength(), entity.getCreatedAt());
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Integer getOriginalWordCount() { return originalWordCount; }
    public void setOriginalWordCount(Integer originalWordCount) { this.originalWordCount = originalWordCount; }
    public Integer getSummaryWordCount() { return summaryWordCount; }
    public void setSummaryWordCount(Integer summaryWordCount) { this.summaryWordCount = summaryWordCount; }
    public Double getCompressionRatio() { return compressionRatio; }
    public void setCompressionRatio(Double compressionRatio) { this.compressionRatio = compressionRatio; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getSummaryLength() { return summaryLength; }
    public void setSummaryLength(String summaryLength) { this.summaryLength = summaryLength; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}

