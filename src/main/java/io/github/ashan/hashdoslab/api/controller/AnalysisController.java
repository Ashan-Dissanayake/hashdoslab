
package io.github.ashan.hashdoslab.api.controller;

import io.github.ashan.hashdoslab.api.dto.request.AnalysisRequest;
import io.github.ashan.hashdoslab.api.dto.response.AnalysisResponse;
import io.github.ashan.hashdoslab.service.AnalysisService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/compare")
    public ResponseEntity<AnalysisResponse> compare(
            @Valid @RequestBody AnalysisRequest request) {

        AnalysisResponse response =
                analysisService.compare(request.keyCount());

        return ResponseEntity.ok(response);
    }
}
