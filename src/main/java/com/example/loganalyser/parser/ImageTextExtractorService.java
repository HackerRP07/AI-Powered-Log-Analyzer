package com.example.loganalyser.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Component
public class ImageTextExtractorService {

    private static final Logger log = LoggerFactory.getLogger(ImageTextExtractorService.class);

    @Value("${app.ocr.command:tesseract}")
    private String ocrCommand;

    @Value("${app.ocr.language:eng}")
    private String ocrLanguage;

    @Value("${app.ocr.timeout-seconds:30}")
    private long ocrTimeoutSeconds;

    public String extract(MultipartFile file) {
        Path tempImage = null;
        Path tempDir = null;
        try {
            String suffix = resolveSuffix(file.getOriginalFilename());
            tempImage = Files.createTempFile("loganalyser-", suffix);
            file.transferTo(tempImage);

            tempDir = Files.createTempDirectory("loganalyser-ocr-");
            String outputBase = tempDir.resolve("result").toAbsolutePath().toString();

            ProcessBuilder pb = new ProcessBuilder(
                    ocrCommand,
                    tempImage.toAbsolutePath().toString(),
                    outputBase,
                    "-l",
                    ocrLanguage
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean completed = process.waitFor(ocrTimeoutSeconds, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                log.warn("OCR timeout for file {}", file.getOriginalFilename());
                return "";
            }
            if (process.exitValue() != 0) {
                log.warn("OCR failed for file {} with exit code {}", file.getOriginalFilename(), process.exitValue());
                return "";
            }

            Path outputTextPath = tempDir.resolve("result.txt");
            String text = Files.exists(outputTextPath)
                    ? Files.readString(outputTextPath).trim()
                    : "";
            if (text.isBlank()) {
                log.debug("OCR produced no text for file {}", file.getOriginalFilename());
                return "";
            }
            return text;
        } catch (Exception ex) {
            log.warn("OCR unavailable for file {}", file.getOriginalFilename(), ex);
            return "";
        } finally {
            if (tempImage != null) {
                try {
                    Files.deleteIfExists(tempImage);
                } catch (IOException ex) {
                    log.debug("Unable to delete temporary OCR image file: {}", tempImage, ex);
                }
            }
            if (tempDir != null) {
                deleteDirectoryQuietly(tempDir);
            }
        }
    }

    private String resolveSuffix(String name) {
        if (name == null || !name.contains(".")) {
            return ".png";
        }
        return name.substring(name.lastIndexOf('.'));
    }

    private void deleteDirectoryQuietly(Path dir) {
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    log.debug("Unable to delete temporary OCR path: {}", path, ex);
                }
            });
        } catch (IOException ex) {
            log.debug("Unable to walk temporary OCR directory: {}", dir, ex);
        }
    }
}
