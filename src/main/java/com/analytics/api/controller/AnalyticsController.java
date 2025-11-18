package com.analytics.api.controller;

import com.analytics.api.dto.AnalysisRequest;
import com.analytics.api.dto.AnalysisResponse;
import com.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Analytics REST Controller
 * Provides statistical analysis endpoints
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Statistical analysis APIs")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @PostMapping("/descriptive-statistics")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Descriptive statistics", description = "Calculate descriptive statistics for selected variables")
    public ResponseEntity<AnalysisResponse> descriptiveStatistics(@RequestBody AnalysisRequest request) {
        try {
            Map<String, Object> results = analyticsService.descriptiveStatistics(
                    request.getFileId(),
                    request.getVariables()
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Descriptive Statistics")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Descriptive Statistics")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/frequency-distribution")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Frequency distribution", description = "Calculate frequency distributions for selected variables")
    public ResponseEntity<AnalysisResponse> frequencyDistribution(@RequestBody AnalysisRequest request) {
        try {
            Map<String, Object> results = analyticsService.frequencyDistribution(
                    request.getFileId(),
                    request.getVariables()
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Frequency Distribution")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Frequency Distribution")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/correlation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Correlation analysis", description = "Calculate Pearson correlation matrix for selected variables")
    public ResponseEntity<AnalysisResponse> correlation(@RequestBody AnalysisRequest request) {
        try {
            Map<String, Object> results = analyticsService.correlation(
                    request.getFileId(),
                    request.getVariables()
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Correlation Analysis")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Correlation Analysis")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/simple-regression")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simple linear regression", description = "Perform simple linear regression analysis")
    public ResponseEntity<AnalysisResponse> simpleRegression(@RequestBody AnalysisRequest request) {
        try {
            String dependentVar = (String) request.getParameters().get("dependentVariable");
            String independentVar = (String) request.getParameters().get("independentVariable");

            Map<String, Object> results = analyticsService.simpleRegression(
                    request.getFileId(),
                    dependentVar,
                    independentVar
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Simple Linear Regression")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Simple Linear Regression")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/multiple-regression")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Multiple linear regression", description = "Perform multiple linear regression analysis")
    public ResponseEntity<AnalysisResponse> multipleRegression(@RequestBody AnalysisRequest request) {
        try {
            String dependentVar = (String) request.getParameters().get("dependentVariable");
            @SuppressWarnings("unchecked")
            List<String> independentVars = (List<String>) request.getParameters().get("independentVariables");

            Map<String, Object> results = analyticsService.multipleRegression(
                    request.getFileId(),
                    dependentVar,
                    independentVars
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Multiple Linear Regression")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Multiple Linear Regression")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/t-test")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "One-sample t-test", description = "Perform one-sample t-test")
    public ResponseEntity<AnalysisResponse> tTest(@RequestBody AnalysisRequest request) {
        try {
            String variable = request.getVariables().get(0);
            double mu = ((Number) request.getParameters().get("mu")).doubleValue();

            Map<String, Object> results = analyticsService.tTest(
                    request.getFileId(),
                    variable,
                    mu
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("One-Sample T-Test")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("One-Sample T-Test")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/two-sample-t-test")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Two-sample t-test", description = "Perform independent two-sample t-test")
    public ResponseEntity<AnalysisResponse> twoSampleTTest(@RequestBody AnalysisRequest request) {
        try {
            String variable1 = request.getVariables().get(0);
            String variable2 = request.getVariables().get(1);

            Map<String, Object> results = analyticsService.twoSampleTTest(
                    request.getFileId(),
                    variable1,
                    variable2
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Two-Sample T-Test")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Two-Sample T-Test")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/anova")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "One-Way ANOVA", description = "Perform one-way analysis of variance")
    public ResponseEntity<AnalysisResponse> anova(@RequestBody AnalysisRequest request) {
        try {
            Map<String, Object> results = analyticsService.anova(
                    request.getFileId(),
                    request.getVariables()
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("One-Way ANOVA")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("One-Way ANOVA")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/chi-square-test")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Chi-Square test", description = "Perform chi-square test of independence")
    public ResponseEntity<AnalysisResponse> chiSquareTest(@RequestBody AnalysisRequest request) {
        try {
            String variable1 = request.getVariables().get(0);
            String variable2 = request.getVariables().get(1);

            Map<String, Object> results = analyticsService.chiSquareTest(
                    request.getFileId(),
                    variable1,
                    variable2
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Chi-Square Test")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Chi-Square Test")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/covariance")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Covariance analysis", description = "Calculate covariance matrix for selected variables")
    public ResponseEntity<AnalysisResponse> covariance(@RequestBody AnalysisRequest request) {
        try {
            Map<String, Object> results = analyticsService.covariance(
                    request.getFileId(),
                    request.getVariables()
            );

            return ResponseEntity.ok(AnalysisResponse.builder()
                    .analysisType("Covariance Analysis")
                    .results(results)
                    .success(true)
                    .message("Analysis completed successfully")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                    .analysisType("Covariance Analysis")
                    .success(false)
                    .message("Error: " + e.getMessage())
                    .build());
        }
    }
}
