package com.example.summarizer.interfaces;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentParsingService {
  public String parseFile(MultipartFile file) throws Exception;
}
