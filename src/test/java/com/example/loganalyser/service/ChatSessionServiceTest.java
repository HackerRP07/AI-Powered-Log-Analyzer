package com.example.loganalyser.service;

import com.example.loganalyser.dto.AnalysisResponseDto;
import com.example.loganalyser.dto.ChatSessionDto;
import com.example.loganalyser.dto.FindingDto;
import com.example.loganalyser.dto.TimelineEventDto;
import com.example.loganalyser.model.ChatSession;
import com.example.loganalyser.repository.ChatAnalysisResultRepository;
import com.example.loganalyser.repository.ChatMessageRepository;
import com.example.loganalyser.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
class ChatSessionServiceTest {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatAnalysisResultRepository chatAnalysisResultRepository;

    @BeforeEach
    void cleanDatabase() {
        chatAnalysisResultRepository.deleteAll();
        chatMessageRepository.deleteAll();
        chatSessionRepository.deleteAll();
    }

    @Test
    void shouldCreateChatSession() {
        ChatSession session = chatSessionService.getOrCreateSession(null);
        assertThat(session.getId()).isNotNull();
        assertThat(chatSessionRepository.existsById(session.getId())).isTrue();
    }

    @Test
    void shouldSaveUserContextMessage() {
        ChatSession session = chatSessionService.getOrCreateSession(null);
        chatSessionService.saveUserMessage(session, "focus on payment timeout");

        ChatSessionDto dto = chatSessionService.getSessionDetails(session.getId());
        assertThat(dto.getMessages()).hasSize(1);
        assertThat(dto.getMessages().get(0).getRole()).isEqualTo("USER");
        assertThat(dto.getMessages().get(0).getContent()).contains("payment timeout");
    }

    @Test
    void shouldSaveAssistantResponse() {
        ChatSession session = chatSessionService.getOrCreateSession(null);
        AnalysisResponseDto response = sampleResponse();

        chatSessionService.saveAssistantResponse(session, response, "{\"raw\":true}");

        ChatSessionDto dto = chatSessionService.getSessionDetails(session.getId());
        assertThat(dto.getAnalysisResults()).hasSize(1);
        assertThat(dto.getAnalysisResults().get(0).getSummary()).isEqualTo("Incident summary");
        assertThat(dto.getAnalysisResults().get(0).getConfidenceScore()).isEqualTo(0.82);
        assertThat(dto.getMessages().stream().anyMatch(m -> "ASSISTANT".equals(m.getRole()))).isTrue();
    }

    @Test
    void shouldReloadExistingSession() {
        ChatSession session = chatSessionService.getOrCreateSession(null);
        chatSessionService.saveUserMessage(session, "first context");
        chatSessionService.saveAssistantResponse(session, sampleResponse(), "{\"raw\":true}");

        ChatSessionDto dto = chatSessionService.getSessionDetails(session.getId());
        assertThat(dto.getSessionId()).isEqualTo(session.getId());
        assertThat(dto.getMessages()).isNotEmpty();
        assertThat(dto.getAnalysisResults()).hasSize(1);
    }

    @Test
    void shouldDeleteSessionWithRelatedData() {
        ChatSession session = chatSessionService.getOrCreateSession(null);
        chatSessionService.saveUserMessage(session, "context for delete");
        chatSessionService.saveAssistantResponse(session, sampleResponse(), "{\"raw\":true}");

        chatSessionService.deleteSession(session.getId());

        assertThat(chatSessionRepository.existsById(session.getId())).isFalse();
        assertThat(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())).isEmpty();
        assertThat(chatAnalysisResultRepository.findBySessionIdOrderByCreatedAtAsc(session.getId())).isEmpty();
    }

    private AnalysisResponseDto sampleResponse() {
        FindingDto finding = new FindingDto();
        finding.setSeverity("HIGH");
        finding.setMessage("Payment timeout detected");
        finding.setEvidence("line 42");

        TimelineEventDto timeline = new TimelineEventDto();
        timeline.setTimestamp("2026-05-15T10:10:00Z");
        timeline.setEvent("Service timeout");
        timeline.setSource("app.log");

        AnalysisResponseDto dto = new AnalysisResponseDto();
        dto.setSummary("Incident summary");
        dto.setFindings(List.of(finding));
        dto.setTimeline(List.of(timeline));
        dto.setRecommendations(List.of("Increase timeout"));
        dto.setConfidenceScore(0.82);
        return dto;
    }
}
