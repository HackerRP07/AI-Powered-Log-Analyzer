package com.example.loganalyser.dto;

import lombok.Data;

import java.util.List;

@Data
public class AnalysisResponseDto {
    private String summary;
    private List<FindingDto> findings;
    private List<TimelineEventDto> timeline;
    private List<String> recommendations;
    private Double confidenceScore;
}
