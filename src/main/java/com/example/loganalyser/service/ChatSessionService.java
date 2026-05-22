package com.example.loganalyser.service;

import com.example.loganalyser.dto.AnalysisResponseDto;
import com.example.loganalyser.dto.ChatMessageDto;
import com.example.loganalyser.dto.ChatSessionDto;
import com.example.loganalyser.dto.FindingDto;
import com.example.loganalyser.dto.StoredAnalysisResultDto;
import com.example.loganalyser.dto.TimelineEventDto;
import com.example.loganalyser.exception.ResourceNotFoundException;
import com.example.loganalyser.model.ChatAnalysisResult;
import com.example.loganalyser.model.ChatMessage;
import com.example.loganalyser.model.ChatSession;
import com.example.loganalyser.model.MessageRole;
import com.example.loganalyser.repository.ChatAnalysisResultRepository;
import com.example.loganalyser.repository.ChatMessageRepository;
import com.example.loganalyser.repository.ChatSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatAnalysisResultRepository chatAnalysisResultRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatSession getOrCreateSession(Long sessionId) {
        if (sessionId == null) {
            return chatSessionRepository.save(new ChatSession());
        }
        return chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));
    }

    @Transactional
    public void saveUserMessage(ChatSession session, String contextText) {
        ChatMessage message = new ChatMessage();
        message.setSession(session);
        message.setRole(MessageRole.USER);
        message.setContent(contextText == null || contextText.isBlank()
                ? "(no additional user context provided)"
                : contextText);
        chatMessageRepository.save(message);
        touchSession(session);
    }

    @Transactional
    public void saveAssistantResponse(ChatSession session, AnalysisResponseDto response, String rawResponse) {
        ChatAnalysisResult result = new ChatAnalysisResult();
        result.setSession(session);
        result.setSummary(defaultIfBlank(response.getSummary(), "No summary available."));
        result.setFindingsJson(toJson(response.getFindings()));
        result.setTimelineJson(toJson(response.getTimeline()));
        result.setRecommendationsJson(toJson(response.getRecommendations()));
        result.setConfidenceScore(response.getConfidenceScore() == null ? 0.0 : response.getConfidenceScore());
        result.setRawResponse(defaultIfBlank(rawResponse, "{}"));
        chatAnalysisResultRepository.save(result);

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSession(session);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setContent(defaultIfBlank(response.getSummary(), "Analysis completed."));
        chatMessageRepository.save(assistantMessage);
        touchSession(session);
    }

    @Transactional(readOnly = true)
    public String buildConversationHistory(Long sessionId) {
        List<ChatMessage> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        if (messages.isEmpty()) {
            return "";
        }
        int keepLast = Math.max(0, messages.size() - 12);
        return messages.subList(keepLast, messages.size()).stream()
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));
    }

    @Transactional(readOnly = true)
    public ChatSessionDto getSessionDetails(Long sessionId) {
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found: " + sessionId));

        List<ChatMessageDto> messages = chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toMessageDto)
                .toList();
        List<StoredAnalysisResultDto> results = chatAnalysisResultRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::toStoredResultDto)
                .toList();

        ChatSessionDto dto = new ChatSessionDto();
        dto.setSessionId(session.getId());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
        dto.setMessages(messages);
        dto.setAnalysisResults(results);
        return dto;
    }

    @Transactional
    public void deleteSession(Long sessionId) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Chat session not found: " + sessionId);
        }
        chatAnalysisResultRepository.deleteAll(chatAnalysisResultRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
        chatMessageRepository.deleteAll(chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId));
        chatSessionRepository.deleteById(sessionId);
    }

    private void touchSession(ChatSession session) {
        session.setUpdatedAt(java.time.Instant.now());
        chatSessionRepository.save(session);
    }

    private ChatMessageDto toMessageDto(ChatMessage message) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(message.getId());
        dto.setRole(message.getRole().name());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    private StoredAnalysisResultDto toStoredResultDto(ChatAnalysisResult result) {
        StoredAnalysisResultDto dto = new StoredAnalysisResultDto();
        dto.setId(result.getId());
        dto.setSummary(result.getSummary());
        dto.setFindings(fromJson(result.getFindingsJson(), new TypeReference<List<FindingDto>>() {}));
        dto.setTimeline(fromJson(result.getTimelineJson(), new TypeReference<List<TimelineEventDto>>() {}));
        dto.setRecommendations(fromJson(result.getRecommendationsJson(), new TypeReference<List<String>>() {}));
        dto.setConfidenceScore(result.getConfidenceScore());
        dto.setCreatedAt(result.getCreatedAt());
        return dto;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? List.of() : value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize analysis result for persistence.", e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            String raw = (json == null || json.isBlank()) ? "[]" : json;
            return objectMapper.readValue(raw, typeReference);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize stored analysis result.", e);
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value;
    }
}
