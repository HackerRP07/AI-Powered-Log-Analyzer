package com.example.loganalyser.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ChatMessageDto {
    private Long id;
    private String role;
    private String content;
    private Instant createdAt;
}
