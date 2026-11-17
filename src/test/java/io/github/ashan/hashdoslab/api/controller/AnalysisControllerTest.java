
package io.github.ashan.hashdoslab.api.controller;

import io.github.ashan.hashdoslab.api.dto.request.AnalysisRequest;
import io.github.ashan.hashdoslab.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void shouldReturnAnalysisResponseForValidRequest() throws Exception {

        when(analysisService.compare(100))
                .thenReturn(new io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse(
                        100,
                        new io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse.InputMetrics(250),
                        new io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse.InputMetrics(4950),
                        4700,
                        19.8
                ));

        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "keyCount": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.keyCount").value(100))
                .andExpect(jsonPath("$.normal.traversalCount").value(250))
                .andExpect(jsonPath("$.collision.traversalCount").value(4950))
                .andExpect(jsonPath("$.traversalDifference").value(4700))
                .andExpect(jsonPath("$.traversalRatio").value(19.8));

        verify(analysisService, times(1)).compare(100);
    }

    @Test
    void shouldRejectRequestWhenKeyCountIsZero() throws Exception {

        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "keyCount": 0
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analysisService);
    }

    @Test
    void shouldRejectRequestWhenKeyCountExceedsMaximum() throws Exception {

        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "keyCount": 10001
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analysisService);
    }

    @Test
    void shouldRejectRequestWhenRequestBodyIsMissing() throws Exception {

        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(analysisService);
    }
}
