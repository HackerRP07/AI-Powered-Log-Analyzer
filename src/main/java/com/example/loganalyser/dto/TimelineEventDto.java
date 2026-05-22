package com.example.loganalyser.dto;

import lombok.Data;

@Data
public class TimelineEventDto {
    private String timestamp;
    private String event;
    private String source;
}
