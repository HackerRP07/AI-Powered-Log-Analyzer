package com.example.loganalyser.dto;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class StoredAnalysisResultDto {
    private Long id;
    private String summary;
    private List<FindingDto> findings;
    private List<TimelineEventDto> timeline;
    private List<String> recommendations;
    private Double confidenceScore;
    private Instant createdAt;
}
