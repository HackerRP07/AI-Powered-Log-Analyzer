package com.example.loganalyser.repository;

import com.example.loganalyser.model.ChatAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatAnalysisResultRepository extends JpaRepository<ChatAnalysisResult, Long> {
    List<ChatAnalysisResult> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
}
