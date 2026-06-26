package com.example.summarizer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentParsingServiceImpl implements com.example.summarizer.interfaces.DocumentParsingService {

    private final Tika tika = new Tika();

    public String parseFile(MultipartFile file) throws Exception {
        log.info("Parsing file: {}", file.getOriginalFilename());

        String text = tika.parseToString(file.getInputStream());
        log.info("Parsed {} characters", text.length());

        return text.trim();
    }
}
