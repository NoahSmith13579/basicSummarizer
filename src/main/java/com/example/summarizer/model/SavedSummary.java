package com.example.summarizer.model;


import java.time.LocalDateTime;


public class SavedSummary {

  private Long id;

  private User user;

  private String originalText;

  private String summary;

  private String sourceType;

  private String sourceUrl;

  private Integer originalWordCount;

  private Integer summaryWordCount;

  private String summaryLength;

  private LocalDateTime createdAt;

  public SavedSummary() {
  }

  public SavedSummary(Long id, User user, String originalText, String summary, String sourceType,
                      String sourceUrl, Integer originalWordCount, Integer summaryWordCount,
                      String summaryLength, LocalDateTime createdAt) {
    this.id = id;
    this.user = user;
    this.originalText = originalText;
    this.summary = summary;
    this.sourceType = sourceType;
    this.sourceUrl = sourceUrl;
    this.originalWordCount = originalWordCount;
    this.summaryWordCount = summaryWordCount;
    this.summaryLength = summaryLength;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getOriginalText() {
    return originalText;
  }

  public void setOriginalText(String originalText) {
    this.originalText = originalText;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public Integer getOriginalWordCount() {
    return originalWordCount;
  }

  public void setOriginalWordCount(Integer originalWordCount) {
    this.originalWordCount = originalWordCount;
  }

  public Integer getSummaryWordCount() {
    return summaryWordCount;
  }

  public void setSummaryWordCount(Integer summaryWordCount) {
    this.summaryWordCount = summaryWordCount;
  }

  public String getSummaryLength() {
    return summaryLength;
  }

  public void setSummaryLength(String summaryLength) {
    this.summaryLength = summaryLength;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
