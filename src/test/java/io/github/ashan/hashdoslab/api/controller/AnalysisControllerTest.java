
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


    @Test
    void shouldReturnProblemDetailForValidationError() throws Exception {

        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "keyCount": 0
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.type").value(
                        "https://hashdoslab.dev/problems/validation-error"
                ))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(
                        "Key count must be at least 1"
                ));

        verifyNoInteractions(analysisService);
    }

    @Test
    void shouldReturnProblemDetailForMaximumValidationError() throws Exception {

        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "keyCount": 10001
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(
                        "Key count cannot exceed 10000"
                ));

        verifyNoInteractions(analysisService);
    }


    @Test
    void shouldReturnProblemDetailForIllegalArgumentException() throws Exception {

        // Arrange
        when(analysisService.compare(100))
                .thenThrow(new IllegalArgumentException(
                        "Invalid analysis input"
                ));

        // Act & Assert
        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "keyCount": 100
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.type").value(
                        "https://hashdoslab.dev/problems/bad-request"
                ))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(
                        "Invalid analysis input"
                ));

        verify(analysisService, times(1)).compare(100);
    }

    @Test
    void shouldReturnProblemDetailForUnexpectedException() throws Exception {

        // Arrange
        when(analysisService.compare(100))
                .thenThrow(new RuntimeException(
                        "Sensitive internal error details"
                ));

        // Act & Assert
        mockMvc.perform(post("/api/v1/analysis/compare")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "keyCount": 100
                            }
                            """))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON
                ))
                .andExpect(jsonPath("$.type").value(
                        "https://hashdoslab.dev/problems/internal-error"
                ))
                .andExpect(jsonPath("$.title").value(
                        "Internal Server Error"
                ))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value(
                        "An unexpected error occurred."
                ))
                .andExpect(jsonPath("$.detail").value(
                        org.hamcrest.Matchers.not(
                                org.hamcrest.Matchers.containsString(
                                        "Sensitive internal error details"
                                )
                        )
                ));

        verify(analysisService, times(1)).compare(100);
    }


}
