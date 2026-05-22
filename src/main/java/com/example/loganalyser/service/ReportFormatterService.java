package com.example.loganalyser.service;

import com.example.loganalyser.dto.FindingDto;
import com.example.loganalyser.dto.TimelineEventDto;
import com.example.loganalyser.dto.AnalysisResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReportFormatterService {

    private final ObjectMapper objectMapper;

    public ReportFormatterService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AnalysisResponseDto format(String aiResult) {
        AnalysisResponseDto response = new AnalysisResponseDto();
        try {
            JsonNode root = objectMapper.readTree(aiResult);

            response.setSummary(root.path("summary").asText("No summary available."));
            response.setFindings(parseFindings(root.path("findings")));
            response.setTimeline(parseTimeline(root.path("timeline")));
            response.setRecommendations(parseRecommendations(root.path("recommendations")));
            response.setConfidenceScore(root.path("confidenceScore").asDouble(0.0));
        } catch (Exception e) {
            response.setSummary(aiResult);
            response.setFindings(List.of());
            response.setTimeline(List.of());
            response.setRecommendations(List.of("AI response could not be parsed as structured JSON."));
            response.setConfidenceScore(0.0);
        }
        return response;
    }

    private List<FindingDto> parseFindings(JsonNode findingsNode) {
        if (!findingsNode.isArray()) {
            return List.of();
        }
        List<FindingDto> findings = new ArrayList<>();
        for (JsonNode node : findingsNode) {
            FindingDto finding = new FindingDto();
            finding.setSeverity(node.path("severity").asText(""));
            finding.setMessage(node.path("message").asText(""));
            finding.setEvidence(node.path("evidence").asText(""));
            findings.add(finding);
        }
        return findings;
    }

    private List<TimelineEventDto> parseTimeline(JsonNode timelineNode) {
        if (!timelineNode.isArray()) {
            return List.of();
        }
        List<TimelineEventDto> timeline = new ArrayList<>();
        for (JsonNode node : timelineNode) {
            TimelineEventDto item = new TimelineEventDto();
            item.setTimestamp(node.path("timestamp").asText(""));
            item.setEvent(node.path("event").asText(""));
            item.setSource(node.path("source").asText(""));
            timeline.add(item);
        }
        return timeline;
    }

    private List<String> parseRecommendations(JsonNode recommendationsNode) {
        if (!recommendationsNode.isArray()) {
            return List.of();
        }
        List<String> recommendations = new ArrayList<>();
        for (JsonNode node : recommendationsNode) {
            recommendations.add(node.asText(""));
        }
        return recommendations;
    }
}
