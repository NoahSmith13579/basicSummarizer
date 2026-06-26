package com.example.summarizer.enums;

public enum SummaryLength {
    TLDR("1-2 sentences", 0.1),
    SHORT("3-5 sentences", 0.2),
    MEDIUM("5-10 sentences", 0.4),
    DETAILED("Comprehensive", 0.6);

    private final String description;
    private final double targetRatio;

    SummaryLength(String description, double targetRatio) {
        this.description = description;
        this.targetRatio = targetRatio;
    }

    public String getDescription() { return description; }
    public double getTargetRatio() { return targetRatio; }
}
