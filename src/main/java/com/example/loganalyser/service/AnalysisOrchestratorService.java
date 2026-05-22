package com.example.loganalyser.service;

import com.example.loganalyser.dto.AnalysisResponseDto;
import com.example.loganalyser.dto.PersistedAnalysisResponse;
import com.example.loganalyser.model.ChatSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisOrchestratorService {

    private final FileValidationService fileValidationService;
    private final LogParserService logParserService;
    private final NormalizationService normalizationService;
    private final AiAnalysisService aiAnalysisService;
    private final ReportFormatterService reportFormatterService;
    private final ChatSessionService chatSessionService;

    public PersistedAnalysisResponse analyze(List<MultipartFile> files) {
        return analyze(files, null);
    }

    public PersistedAnalysisResponse analyze(List<MultipartFile> files, String additionalContext) {
        return analyze(files, additionalContext, null);
    }

    public PersistedAnalysisResponse analyze(List<MultipartFile> files, String additionalContext, Long sessionId) {
        fileValidationService.validate(files);

        ChatSession session = chatSessionService.getOrCreateSession(sessionId);
        String history = sessionId == null
                ? ""
                : chatSessionService.buildConversationHistory(session.getId());

        String rawEvidenceText = logParserService.extractAll(files);
        String normalizedEvidence = normalizationService.normalize(rawEvidenceText);
        String normalizedUserContext = normalizeContext(additionalContext);

        chatSessionService.saveUserMessage(session, normalizedUserContext);
        String aiResult = aiAnalysisService.analyze(history, normalizedUserContext, normalizedEvidence);
        AnalysisResponseDto response = reportFormatterService.format(aiResult);
        chatSessionService.saveAssistantResponse(session, response, aiResult);
        return new PersistedAnalysisResponse(session.getId(), response);
    }

    private String normalizeContext(String additionalContext) {
        if (additionalContext == null || additionalContext.isBlank()) {
            return "";
        }
        return normalizationService.normalize(additionalContext);
    }
}
