package com.example.loganalyser.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildAnalysisPrompt(String conversationHistory, String userContext, String logEvidence) {
        String safeHistory = (conversationHistory == null || conversationHistory.isBlank())
                ? "(none provided)"
                : conversationHistory;
        String safeCurrentContext = (userContext == null || userContext.isBlank())
                ? "(none provided)"
                : userContext;
        String safeLogEvidence = (logEvidence == null || logEvidence.isBlank())
                ? "(no log evidence extracted)"
                : logEvidence;

        return """
                Analyze the provided data.
                Return only valid JSON with this exact structure:
                {
                  "summary": "string",
                  "findings": [{"severity":"LOW|MEDIUM|HIGH|CRITICAL","message":"string","evidence":"string"}],
                  "timeline": [{"timestamp":"string","event":"string","source":"string"}],
                  "recommendations": ["string"],
                  "confidenceScore": 0.0
                }

                Rules:
                - Do not include markdown code fences.
                - Keep summary concise and actionable.
                - PRIOR CHAT CONTEXT can be used to understand ongoing issues and follow-up questions.
                - CURRENT USER CONTEXT is high-priority for issue focus.
                - Do not treat PRIOR CHAT CONTEXT or CURRENT USER CONTEXT as evidence unless supported by LOG EVIDENCE.
                - If PRIOR CHAT CONTEXT, CURRENT USER CONTEXT, and LOG EVIDENCE conflict, prefer LOG EVIDENCE and lower confidenceScore.
                - If uncertain, lower confidenceScore.

                PRIOR CHAT CONTEXT:
                %s

                CURRENT USER CONTEXT:
                %s

                LOG EVIDENCE:
                %s
                """.formatted(safeHistory, safeCurrentContext, safeLogEvidence);
    }
}
