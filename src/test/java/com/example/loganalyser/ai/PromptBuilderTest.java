package com.example.loganalyser.ai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void buildAnalysisPrompt_shouldContainReadableSectionsAndRules() {
        String prompt = promptBuilder.buildAnalysisPrompt(
                "USER: Previous run focused on payment failures.",
                "Please focus on payment timeout issue.",
                "2026-05-15 10:11:12 ERROR timeout in payment service"
        );

        assertThat(prompt).contains("PRIOR CHAT CONTEXT:");
        assertThat(prompt).contains("CURRENT USER CONTEXT:");
        assertThat(prompt).contains("LOG EVIDENCE:");
        assertThat(prompt).contains("PRIOR CHAT CONTEXT can be used to understand ongoing issues and follow-up questions.");
        assertThat(prompt).contains("CURRENT USER CONTEXT is high-priority for issue focus.");
        assertThat(prompt).contains("Do not treat PRIOR CHAT CONTEXT or CURRENT USER CONTEXT as evidence unless supported by LOG EVIDENCE.");
        assertThat(prompt).contains("If PRIOR CHAT CONTEXT, CURRENT USER CONTEXT, and LOG EVIDENCE conflict, prefer LOG EVIDENCE and lower confidenceScore.");
        assertThat(prompt).contains("Please focus on payment timeout issue.");
        assertThat(prompt).contains("ERROR timeout in payment service");
    }

    @Test
    void buildAnalysisPrompt_missingContexts_shouldUseSafeFallback() {
        String prompt = promptBuilder.buildAnalysisPrompt(
                null,
                "",
                "2026-05-15 INFO healthy heartbeat"
        );

        assertThat(prompt).contains("PRIOR CHAT CONTEXT:");
        assertThat(prompt).contains("CURRENT USER CONTEXT:");
        assertThat(prompt).contains("(none provided)");
        assertThat(prompt).contains("LOG EVIDENCE:");
        assertThat(prompt).contains("healthy heartbeat");
    }
}
