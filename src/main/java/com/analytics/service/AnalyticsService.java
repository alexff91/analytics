package com.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.ChiSquareTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics Service
 * Provides statistical analysis functions
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final FileStorageService fileStorageService;

    /**
     * Perform descriptive statistics analysis
     */
    public Map<String, Object> descriptiveStatistics(Long fileId, List<String> variables) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);
        Map<String, Object> results = new LinkedHashMap<>();

        for (String variable : variables) {
            double[] values = extractNumericColumn(data, variable);
            if (values.length == 0) {
                log.warn("No numeric data found for variable: {}", variable);
                continue;
            }

            DescriptiveStatistics stats = new DescriptiveStatistics(values);

            Map<String, Object> variableStats = new LinkedHashMap<>();
            variableStats.put("n", stats.getN());
            variableStats.put("mean", stats.getMean());
            variableStats.put("median", stats.getPercentile(50));
            variableStats.put("std", stats.getStandardDeviation());
            variableStats.put("variance", stats.getVariance());
            variableStats.put("min", stats.getMin());
            variableStats.put("max", stats.getMax());
            variableStats.put("range", stats.getMax() - stats.getMin());
            variableStats.put("sum", stats.getSum());
            variableStats.put("skewness", stats.getSkewness());
            variableStats.put("kurtosis", stats.getKurtosis());
            variableStats.put("q1", stats.getPercentile(25));
            variableStats.put("q3", stats.getPercentile(75));
            variableStats.put("iqr", stats.getPercentile(75) - stats.getPercentile(25));

            results.put(variable, variableStats);
        }

        return results;
    }

    /**
     * Perform frequency distribution analysis
     */
    public Map<String, Object> frequencyDistribution(Long fileId, List<String> variables) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);
        Map<String, Object> results = new LinkedHashMap<>();

        for (String variable : variables) {
            List<Object> values = extractColumn(data, variable);
            Map<Object, Long> frequencies = values.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(v -> v, Collectors.counting()));

            // Sort by frequency descending
            List<Map<String, Object>> frequencyList = frequencies.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("value", entry.getKey());
                        item.put("frequency", entry.getValue());
                        item.put("percentage", (entry.getValue() * 100.0) / values.size());
                        return item;
                    })
                    .sorted((a, b) -> Long.compare((Long) b.get("frequency"), (Long) a.get("frequency")))
                    .collect(Collectors.toList());

            Map<String, Object> variableFreq = new LinkedHashMap<>();
            variableFreq.put("totalCount", values.size());
            variableFreq.put("uniqueValues", frequencies.size());
            variableFreq.put("frequencies", frequencyList);

            results.put(variable, variableFreq);
        }

        return results;
    }

    /**
     * Perform Pearson correlation analysis
     */
    public Map<String, Object> correlation(Long fileId, List<String> variables) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);
        Map<String, Object> results = new LinkedHashMap<>();

        // Extract all numeric columns
        Map<String, double[]> columnData = new LinkedHashMap<>();
        for (String variable : variables) {
            double[] values = extractNumericColumn(data, variable);
            if (values.length > 0) {
                columnData.put(variable, values);
            }
        }

        // Calculate correlation matrix
        List<Map<String, Object>> correlationMatrix = new ArrayList<>();
        for (String var1 : columnData.keySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("variable", var1);

            for (String var2 : columnData.keySet()) {
                PearsonsCorrelation correlation = new PearsonsCorrelation();
                double corr = correlation.correlation(columnData.get(var1), columnData.get(var2));
                row.put(var2, corr);
            }

            correlationMatrix.add(row);
        }

        results.put("correlationMatrix", correlationMatrix);
        results.put("variables", new ArrayList<>(columnData.keySet()));

        return results;
    }

    /**
     * Perform simple linear regression
     */
    public Map<String, Object> simpleRegression(Long fileId, String dependentVar, String independentVar) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);

        double[] xValues = extractNumericColumn(data, independentVar);
        double[] yValues = extractNumericColumn(data, dependentVar);

        if (xValues.length != yValues.length || xValues.length == 0) {
            throw new IllegalArgumentException("Invalid data for regression");
        }

        SimpleRegression regression = new SimpleRegression();
        for (int i = 0; i < xValues.length; i++) {
            regression.addData(xValues[i], yValues[i]);
        }

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("slope", regression.getSlope());
        results.put("intercept", regression.getIntercept());
        results.put("rSquared", regression.getRSquare());
        results.put("slopeStdError", regression.getSlopeStdErr());
        results.put("interceptStdError", regression.getInterceptStdErr());
        results.put("n", regression.getN());
        results.put("sumSquaredErrors", regression.getSumSquaredErrors());
        results.put("totalSumSquares", regression.getTotalSumSquares());
        results.put("regressionSumSquares", regression.getRegressionSumSquares());
        results.put("meanSquareError", regression.getMeanSquareError());
        results.put("significance", regression.getSignificance());

        // Equation
        results.put("equation", String.format("y = %.4f + %.4f * x",
                regression.getIntercept(), regression.getSlope()));

        return results;
    }

    /**
     * Perform multiple linear regression
     */
    public Map<String, Object> multipleRegression(Long fileId, String dependentVar, List<String> independentVars) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);

        double[] yValues = extractNumericColumn(data, dependentVar);
        double[][] xValues = new double[data.size()][independentVars.size()];

        for (int i = 0; i < independentVars.size(); i++) {
            double[] colValues = extractNumericColumn(data, independentVars.get(i));
            for (int j = 0; j < colValues.length; j++) {
                xValues[j][i] = colValues[j];
            }
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(yValues, xValues);

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("coefficients", regression.estimateRegressionParameters());
        results.put("standardErrors", regression.estimateRegressionParametersStandardErrors());
        results.put("rSquared", regression.calculateRSquared());
        results.put("adjustedRSquared", regression.calculateAdjustedRSquared());
        results.put("residualSumOfSquares", regression.calculateResidualSumOfSquares());
        results.put("totalSumOfSquares", regression.calculateTotalSumOfSquares());

        // Variable names
        List<String> parameterNames = new ArrayList<>();
        parameterNames.add("Intercept");
        parameterNames.addAll(independentVars);
        results.put("parameterNames", parameterNames);

        return results;
    }

    /**
     * Perform t-test
     */
    public Map<String, Object> tTest(Long fileId, String variable, double mu) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);
        double[] values = extractNumericColumn(data, variable);

        TTest tTest = new TTest();
        double tStatistic = tTest.t(mu, values);
        double pValue = tTest.tTest(mu, values);

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("tStatistic", tStatistic);
        results.put("pValue", pValue);
        results.put("significant", pValue < 0.05);
        results.put("mu", mu);
        results.put("sampleMean", StatUtils.mean(values));
        results.put("sampleSize", values.length);

        return results;
    }

    /**
     * Perform two-sample t-test
     */
    public Map<String, Object> twoSampleTTest(Long fileId, String variable1, String variable2) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);

        double[] sample1 = extractNumericColumn(data, variable1);
        double[] sample2 = extractNumericColumn(data, variable2);

        TTest tTest = new TTest();
        double tStatistic = tTest.t(sample1, sample2);
        double pValue = tTest.tTest(sample1, sample2);

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("tStatistic", tStatistic);
        results.put("pValue", pValue);
        results.put("significant", pValue < 0.05);
        results.put("sample1Mean", StatUtils.mean(sample1));
        results.put("sample2Mean", StatUtils.mean(sample2));
        results.put("sample1Size", sample1.length);
        results.put("sample2Size", sample2.length);

        return results;
    }

    /**
     * Perform One-Way ANOVA
     */
    public Map<String, Object> anova(Long fileId, List<String> variables) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);

        // Extract data for each variable
        Collection<double[]> categoryData = new ArrayList<>();
        for (String variable : variables) {
            double[] values = extractNumericColumn(data, variable);
            if (values.length > 0) {
                categoryData.add(values);
            }
        }

        OneWayAnova anova = new OneWayAnova();
        double fStatistic = anova.anovaFValue(categoryData);
        double pValue = anova.anovaPValue(categoryData);

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("fStatistic", fStatistic);
        results.put("pValue", pValue);
        results.put("significant", pValue < 0.05);
        results.put("groups", variables.size());

        return results;
    }

    /**
     * Perform Chi-Square test
     */
    public Map<String, Object> chiSquareTest(Long fileId, String variable1, String variable2) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);

        // Build contingency table
        Map<String, Map<String, Long>> contingencyMap = new HashMap<>();
        for (Map<String, Object> row : data) {
            Object val1 = row.get(variable1);
            Object val2 = row.get(variable2);
            if (val1 != null && val2 != null) {
                String key1 = val1.toString();
                String key2 = val2.toString();
                contingencyMap.computeIfAbsent(key1, k -> new HashMap<>())
                        .merge(key2, 1L, Long::sum);
            }
        }

        // Convert to long array
        List<String> cat1Values = new ArrayList<>(contingencyMap.keySet());
        List<String> cat2Values = contingencyMap.values().stream()
                .flatMap(m -> m.keySet().stream())
                .distinct()
                .collect(Collectors.toList());

        long[][] contingencyTable = new long[cat1Values.size()][cat2Values.size()];
        for (int i = 0; i < cat1Values.size(); i++) {
            Map<String, Long> rowMap = contingencyMap.get(cat1Values.get(i));
            for (int j = 0; j < cat2Values.size(); j++) {
                contingencyTable[i][j] = rowMap.getOrDefault(cat2Values.get(j), 0L);
            }
        }

        ChiSquareTest chiSquareTest = new ChiSquareTest();
        double chiSquare = chiSquareTest.chiSquare(contingencyTable);
        double pValue = chiSquareTest.chiSquareTest(contingencyTable);

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("chiSquare", chiSquare);
        results.put("pValue", pValue);
        results.put("significant", pValue < 0.05);
        results.put("contingencyTable", contingencyTable);
        results.put("category1Values", cat1Values);
        results.put("category2Values", cat2Values);

        return results;
    }

    /**
     * Perform Covariance analysis
     */
    public Map<String, Object> covariance(Long fileId, List<String> variables) throws IOException {
        List<Map<String, Object>> data = fileStorageService.parseFileData(fileId);

        // Extract all numeric columns
        double[][] matrix = new double[data.size()][variables.size()];
        for (int i = 0; i < variables.size(); i++) {
            double[] colValues = extractNumericColumn(data, variables.get(i));
            for (int j = 0; j < colValues.length; j++) {
                matrix[j][i] = colValues[j];
            }
        }

        Covariance covariance = new Covariance(matrix);
        double[][] covMatrix = covariance.getCovarianceMatrix().getData();

        Map<String, Object> results = new LinkedHashMap<>();
        results.put("covarianceMatrix", covMatrix);
        results.put("variables", variables);

        return results;
    }

    /**
     * Extract numeric column from data
     */
    private double[] extractNumericColumn(List<Map<String, Object>> data, String columnName) {
        return data.stream()
                .map(row -> row.get(columnName))
                .filter(Objects::nonNull)
                .filter(val -> val instanceof Number || isNumeric(val.toString()))
                .mapToDouble(val -> {
                    if (val instanceof Number) {
                        return ((Number) val).doubleValue();
                    }
                    try {
                        return Double.parseDouble(val.toString());
                    } catch (NumberFormatException e) {
                        return Double.NaN;
                    }
                })
                .filter(val -> !Double.isNaN(val))
                .toArray();
    }

    /**
     * Extract column from data (any type)
     */
    private List<Object> extractColumn(List<Map<String, Object>> data, String columnName) {
        return data.stream()
                .map(row -> row.get(columnName))
                .collect(Collectors.toList());
    }

    /**
     * Check if string is numeric
     */
    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
