package io.github.ashan.hashdoslab.api.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AnalysisRequest(

        @Min(value = 1, message = "Key count must be at least 1")
        @Max(value = 10000, message = "Key count cannot exceed 10000")
        int keyCount

) {
}