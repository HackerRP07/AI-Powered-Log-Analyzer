package com.example.loganalyser.controller;

import com.example.loganalyser.dto.AnalysisResponseDto;
import com.example.loganalyser.dto.PersistedAnalysisResponse;
import com.example.loganalyser.service.AnalysisOrchestratorService;
import com.example.loganalyser.service.ChatSessionService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalysisOrchestratorService orchestratorService;

    @MockBean
    private ChatSessionService chatSessionService;

    @Test
    void analyze_filesOnly_shouldCallServiceWithNullText() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "sample.log", MediaType.TEXT_PLAIN_VALUE, "ERROR line".getBytes()
        );
        when(orchestratorService.analyze(anyList(), isNull(), isNull()))
                .thenReturn(new PersistedAnalysisResponse(10L, response("ok-files-only")));

        mockMvc.perform(multipart("/api/v1/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("ok-files-only"))
                .andExpect(header().string("X-Chat-Session-Id", "10"));

        verify(orchestratorService).analyze(anyList(), isNull(), isNull());
    }

    @Test
    void analyze_filesPlusText_shouldCallServiceWithText() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "sample.log", MediaType.TEXT_PLAIN_VALUE, "ERROR line".getBytes()
        );
        when(orchestratorService.analyze(anyList(), anyString(), isNull()))
                .thenReturn(new PersistedAnalysisResponse(11L, response("ok-files-text")));

                mockMvc.perform(multipart("/api/v1/analyze")
                        .file(file)
                        .param("text", "focus on payment timeout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("ok-files-text"))
                .andExpect(header().string("X-Chat-Session-Id", "11"));

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(orchestratorService).analyze(anyList(), textCaptor.capture(), isNull());
        assertThat(textCaptor.getValue()).isEqualTo("focus on payment timeout");
    }

    @Test
    void analyze_filesPlusEmptyText_shouldPassEmptyText() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "sample.log", MediaType.TEXT_PLAIN_VALUE, "ERROR line".getBytes()
        );
        when(orchestratorService.analyze(anyList(), anyString(), isNull()))
                .thenReturn(new PersistedAnalysisResponse(12L, response("ok-files-empty-text")));

                mockMvc.perform(multipart("/api/v1/analyze")
                        .file(file)
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("ok-files-empty-text"))
                .andExpect(header().string("X-Chat-Session-Id", "12"));

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(orchestratorService).analyze(anyList(), textCaptor.capture(), isNull());
        assertThat(textCaptor.getValue()).isEmpty();
    }

    @Test
    void analyze_missingFiles_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/analyze").param("text", "only text"))
                .andExpect(status().isBadRequest());

        verify(orchestratorService, never()).analyze(anyList(), anyString(), any(Long.class));
    }

    private AnalysisResponseDto response(String summary) {
        AnalysisResponseDto dto = new AnalysisResponseDto();
        dto.setSummary(summary);
        dto.setFindings(List.of());
        dto.setTimeline(List.of());
        dto.setRecommendations(List.of());
        dto.setConfidenceScore(0.0);
        return dto;
    }
}
