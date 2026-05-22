package com.example.loganalyser.service;

import com.example.loganalyser.dto.AnalysisResponseDto;
import com.example.loganalyser.dto.PersistedAnalysisResponse;
import com.example.loganalyser.model.ChatSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisOrchestratorServiceTest {

    @Mock
    private FileValidationService fileValidationService;
    @Mock
    private LogParserService logParserService;
    @Mock
    private NormalizationService normalizationService;
    @Mock
    private AiAnalysisService aiAnalysisService;
    @Mock
    private ReportFormatterService reportFormatterService;
    @Mock
    private ChatSessionService chatSessionService;

    @InjectMocks
    private AnalysisOrchestratorService orchestratorService;

    @Test
    void analyze_newSessionWithoutPriorContext_shouldPassEmptyHistoryAndContext() {
        List<MultipartFile> files = List.of(new MockMultipartFile(
                "files", "sample.log", "text/plain", "ERROR entry".getBytes()
        ));
        ChatSession session = new ChatSession();
        session.setId(101L);
        AnalysisResponseDto formatted = response("summary-1");

        when(chatSessionService.getOrCreateSession(null)).thenReturn(session);
        when(logParserService.extractAll(files)).thenReturn("raw-evidence");
        when(normalizationService.normalize("raw-evidence")).thenReturn("normalized-evidence");
        when(aiAnalysisService.analyze("", "", "normalized-evidence")).thenReturn("{\"summary\":\"summary-1\"}");
        when(reportFormatterService.format("{\"summary\":\"summary-1\"}")).thenReturn(formatted);

        PersistedAnalysisResponse result = orchestratorService.analyze(files, null, null);

        assertThat(result.sessionId()).isEqualTo(101L);
        assertThat(result.response().getSummary()).isEqualTo("summary-1");
        verify(fileValidationService).validate(files);
        verify(chatSessionService, never()).buildConversationHistory(101L);
        verify(chatSessionService).saveUserMessage(session, "");
        verify(aiAnalysisService).analyze("", "", "normalized-evidence");
        verify(chatSessionService).saveAssistantResponse(session, formatted, "{\"summary\":\"summary-1\"}");
    }

    @Test
    void analyze_reusedSessionWithPriorAndCurrentContext_shouldPassBothSeparately() {
        List<MultipartFile> files = List.of(new MockMultipartFile(
                "files", "sample.log", "text/plain", "WARN entry".getBytes()
        ));
        ChatSession session = new ChatSession();
        session.setId(55L);
        AnalysisResponseDto formatted = response("summary-2");

        when(chatSessionService.getOrCreateSession(55L)).thenReturn(session);
        when(chatSessionService.buildConversationHistory(55L)).thenReturn("USER: previous issue context");
        when(logParserService.extractAll(files)).thenReturn("raw-log-data");
        when(normalizationService.normalize("raw-log-data")).thenReturn("normalized-log-data");
        when(normalizationService.normalize("focus on db timeouts")).thenReturn("focus on db timeouts");
        when(aiAnalysisService.analyze("USER: previous issue context", "focus on db timeouts", "normalized-log-data"))
                .thenReturn("{\"summary\":\"summary-2\"}");
        when(reportFormatterService.format("{\"summary\":\"summary-2\"}")).thenReturn(formatted);

        PersistedAnalysisResponse result = orchestratorService.analyze(files, "focus on db timeouts", 55L);

        assertThat(result.sessionId()).isEqualTo(55L);
        assertThat(result.response().getSummary()).isEqualTo("summary-2");
        verify(chatSessionService).buildConversationHistory(55L);
        verify(chatSessionService).saveUserMessage(session, "focus on db timeouts");
        verify(aiAnalysisService).analyze("USER: previous issue context", "focus on db timeouts", "normalized-log-data");

        ArgumentCaptor<String> aiRawCaptor = ArgumentCaptor.forClass(String.class);
        verify(chatSessionService).saveAssistantResponse(eq(session), eq(formatted), aiRawCaptor.capture());
        assertThat(aiRawCaptor.getValue()).isEqualTo("{\"summary\":\"summary-2\"}");
    }

    private AnalysisResponseDto response(String summary) {
        AnalysisResponseDto dto = new AnalysisResponseDto();
        dto.setSummary(summary);
        dto.setFindings(List.of());
        dto.setTimeline(List.of());
        dto.setRecommendations(List.of());
        dto.setConfidenceScore(0.0);
        return dto;
    }
}
