package com.example.loganalyser.service;

import com.example.loganalyser.ai.OpenAiClient;
import com.example.loganalyser.ai.PromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiAnalysisService {

    private final PromptBuilder promptBuilder;
    private final OpenAiClient openAiClient;

    public String analyze(String conversationHistory, String userContext, String logEvidence) {
        String prompt = promptBuilder.buildAnalysisPrompt(conversationHistory, userContext, logEvidence);
        return openAiClient.getAnalysis(prompt);
    }
}
