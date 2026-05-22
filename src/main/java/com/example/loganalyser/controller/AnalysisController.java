package com.example.loganalyser.controller;

import com.example.loganalyser.dto.AnalysisResponseDto;
import com.example.loganalyser.dto.ChatSessionDto;
import com.example.loganalyser.dto.PersistedAnalysisResponse;
import com.example.loganalyser.service.AnalysisOrchestratorService;
import com.example.loganalyser.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisOrchestratorService orchestratorService;
    private final ChatSessionService chatSessionService;

    @GetMapping
    public ResponseEntity<Map<String, String>> info() {
        return ResponseEntity.ok(Map.of(
                "service", "Log Analyser API",
                "status", "UP",
                "analyzeEndpoint", "POST /api/v1/analyze",
                "sessionDetailsEndpoint", "GET /api/v1/sessions/{sessionId}",
                "sessionDeleteEndpoint", "DELETE /api/v1/sessions/{sessionId}"
        ));
    }

    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResponseDto> analyze(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart(value = "text", required = false) String text,
            @RequestPart(value = "sessionId", required = false) Long sessionId) {
        PersistedAnalysisResponse persisted = orchestratorService.analyze(files, text, sessionId);
        return ResponseEntity.ok()
                .header("X-Chat-Session-Id", String.valueOf(persisted.sessionId()))
                .body(persisted.response());
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ChatSessionDto> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(chatSessionService.getSessionDetails(sessionId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long sessionId) {
        chatSessionService.deleteSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}
