package com.example.summarizer.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name= "saved_summary")
public class SavedSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "original_text")
    private String originalText;
    @Column(name = "summary")
    private String summary;
    @Column(name = "source_type")
    private String sourceType;
    @Column(name = "source_url")
    private String sourceUrl;
    @Column(name = "original_word_count")
    private Integer originalWordCount;
    @Column(name = "summary_word_count")
    private Integer summaryWordCount;
    @Column(name = "summary_length")
    private String summaryLength;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public SavedSummary() {}
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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public Integer getOriginalWordCount() { return originalWordCount; }
    public void setOriginalWordCount(Integer originalWordCount) { this.originalWordCount = originalWordCount; }
    public Integer getSummaryWordCount() { return summaryWordCount; }
    public void setSummaryWordCount(Integer summaryWordCount) { this.summaryWordCount = summaryWordCount; }
    public String getSummaryLength() { return summaryLength; }
    public void setSummaryLength(String summaryLength) { this.summaryLength = summaryLength; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
