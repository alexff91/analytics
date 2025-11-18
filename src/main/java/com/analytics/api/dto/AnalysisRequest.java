package com.analytics.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Generic Analysis Request DTO
 */
@Data
public class AnalysisRequest {

    private Long fileId;
    private List<String> variables; // Column names to analyze
    private Map<String, Object> parameters; // Additional parameters for specific analyses
}
