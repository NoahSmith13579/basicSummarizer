package com.example.summarizer.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class UrlRequest {
    @NotBlank(message = "URL cannot be empty")
    private String url;

    public UrlRequest() {}

    @JsonCreator
    public UrlRequest(@JsonProperty("url") String url) {
        this.url = url;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
}
