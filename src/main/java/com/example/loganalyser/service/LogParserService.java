package com.example.loganalyser.service;

import com.example.loganalyser.parser.ImageTextExtractorService;
import com.example.loganalyser.parser.TextLogParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogParserService {

    private final TextLogParser textLogParser;
    private final ImageTextExtractorService imageTextExtractorService;

    public String extractAll(List<MultipartFile> files) {
        StringBuilder sb = new StringBuilder();
        for (MultipartFile file : files) {
            String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
            if (name.endsWith(".txt") || name.endsWith(".log")) {
                sb.append(textLogParser.parse(file)).append("\n");
            } else {
                String ocrText = imageTextExtractorService.extract(file);
                if (!ocrText.isBlank()) {
                    sb.append(ocrText).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
