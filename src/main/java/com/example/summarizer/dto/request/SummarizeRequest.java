package com.example.summarizer.dto.request;

import com.example.summarizer.enums.SummaryLength;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SummarizeRequest {
    @NotBlank(message = "Text cannot be empty")
    @Size(min = 10, max = 50000, message = "Text must be 10-50000 chars")
    private String text;

    @NotNull(message = "Must specify length type")
    private SummaryLength summaryLength;

    public SummarizeRequest() {}

    @JsonCreator
    public SummarizeRequest(
            @JsonProperty("text") String text,
            @JsonProperty("summaryLength") SummaryLength summaryLength) {
        this.text = text;
        this.summaryLength = summaryLength;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public SummaryLength getSummaryLength() { return summaryLength; }
    public void setSummaryLength(SummaryLength summaryLength) { this.summaryLength = summaryLength; }
}
