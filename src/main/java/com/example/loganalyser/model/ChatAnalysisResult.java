package com.example.loganalyser.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "chat_analysis_result",
        indexes = {
                @Index(name = "idx_chat_result_session_created", columnList = "session_id, created_at")
        }
)
@Getter
@Setter
public class ChatAnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Lob
    @Column(name = "summary", nullable = false)
    private String summary;

    @Lob
    @Column(name = "findings_json", nullable = false)
    private String findingsJson;

    @Lob
    @Column(name = "timeline_json", nullable = false)
    private String timelineJson;

    @Lob
    @Column(name = "recommendations_json", nullable = false)
    private String recommendationsJson;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Lob
    @Column(name = "raw_response", nullable = false)
    private String rawResponse;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
