package com.example.loganalyser.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ChatSessionDto {
    private Long sessionId;
    private Instant createdAt;
    private Instant updatedAt;
    private List<ChatMessageDto> messages;
    private List<StoredAnalysisResultDto> analysisResults;
}
