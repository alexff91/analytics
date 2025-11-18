package com.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot Application for Analytics Platform
 * Modern SPSS-like Statistical Analysis Platform
 *
 * Features:
 * - RESTful API for statistical analysis
 * - JWT-based authentication
 * - File upload and processing (Excel, CSV)
 * - Comprehensive statistical analysis tools
 * - Interactive data visualization
 * - Modern React UI
 */
@SpringBootApplication
@EnableJpaAuditing
public class AnalyticsPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsPlatformApplication.class, args);
    }
}
