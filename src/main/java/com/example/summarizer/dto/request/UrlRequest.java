package com.example.summarizer.dto.request;

import com.example.summarizer.enums.SummaryLength;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class UrlRequest {
  @NotBlank(message = "URL cannot be empty")
  private String url;
  
  private SummaryLength summaryLength;

  public UrlRequest() {
  }

  @JsonCreator
  public UrlRequest(@JsonProperty("url") String url, @JsonProperty("summaryLength") SummaryLength summaryLength) {
    this.url = url;
    this.summaryLength = summaryLength;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public SummaryLength getSummaryLength() {
    return summaryLength;
  }

  public void setSummaryLength(SummaryLength summaryLength) {
    this.summaryLength = summaryLength;
  }

}
