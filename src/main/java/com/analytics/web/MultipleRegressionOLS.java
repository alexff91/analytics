package com.analytics.web;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultipleRegressionOLS implements Serializable {
  private final ExporterBean exporterBean;

  public MultipleRegressionOLS(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void multipleRegressionOls() {
    try {

      List<OLSMultipleLinearRegression> decList = new LinkedList<OLSMultipleLinearRegression>();

      int dependant = 0;
      ArrayList<Integer> independant = new ArrayList<Integer>();
      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
        for (int ind = 0; ind < exporterBean.getSelectedIndepVarsOLS().length; ind++) {
          if (exporterBean.getStatisticsColumnTemplate().get(i)
              .equals(exporterBean.getSelectedIndepVarsOLS()[ind])) {
            independant.add(i);
          }
        }

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedDepVarsOLS())) {
          dependant = i;
        }
      }

      decList.add(new OLSMultipleLinearRegression());

      double[] x = new double[exporterBean.getStatisticsValues().size()];
      double[][] y = new double[exporterBean.getStatisticsValues().size()][independant.size()];
      int j = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String values = value.getValues(j, dependant);
        if (values != null) {
          x[j - 1] = Double.valueOf(values);

          for (int i = 0; i < independant.size(); i++) {

            String yValue = value.getValues(j, independant.get(i));
            if (yValue != null) {
              y[j - 1][i] = Double.valueOf(yValue);
            }
          }
          j++;
        }
      }
      decList.get(0).newSampleData(x, y);

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Variables")));

      List<List<String>> properties = new ArrayList<List<String>>(decList.size());
      String[] propertyNames = new String[]{
          "Est. Regr. Std Error", "Est. Error Variance",
          "Est. Regr. Variance", "Calc. Residual Sum Of Squares",
          "Calc. Total Sum Of Squares", "Calc. Adjusted RSquared", "Calc. RSquared",
          "Est. Regr. Parameters"
          , "Est. Regr. Param. Variance"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));

      properties.add(new ArrayList<String>());
      String selectedIndep = Arrays.toString(exporterBean.getSelectedIndepVars());
      properties.get(0)
          .add("Independant variables : " + Arrays.toString(exporterBean.getSelectedIndepVarsOLS())
              + " and dependant variable " + exporterBean.getSelectedDepVarsOLS());

      for (int i = 0; i < decList.size(); i++) {

        OLSMultipleLinearRegression simpleRegression = decList.get(i);
        double estimateRegressionStandardError = ExporterBean.roundTo2Decimals(
            simpleRegression.estimateRegressionStandardError());
        properties.get(i).add(String.valueOf(estimateRegressionStandardError));
        double estimateErrorVariance = ExporterBean
            .roundTo2Decimals(simpleRegression.estimateErrorVariance());
        properties.get(i).add(String.valueOf(estimateErrorVariance));
        double estimateRegressandVariance = ExporterBean.roundTo2Decimals(
            simpleRegression.estimateRegressandVariance());
        properties.get(i).add(String.valueOf(estimateRegressandVariance));
        double calculateResidualSumOfSquares = ExporterBean.roundTo2Decimals(
            simpleRegression.calculateResidualSumOfSquares());
        properties.get(i).add(String.valueOf(calculateResidualSumOfSquares));
        double calculateTotalSumOfSquares = ExporterBean.roundTo2Decimals(
            simpleRegression.calculateTotalSumOfSquares());
        properties.get(i).add(String.valueOf(calculateTotalSumOfSquares));
        double calculateAdjustedRSquared = ExporterBean.roundTo2Decimals(
            simpleRegression.calculateAdjustedRSquared());
        properties.get(i).add(String.valueOf(calculateAdjustedRSquared));
        double calculateRSquared = ExporterBean
            .roundTo2Decimals(simpleRegression.calculateRSquared());
        properties.get(i).add(String.valueOf(calculateRSquared));
        String estimateRegressionParameters = Arrays
            .toString(
                exporterBean.getRoundedArray(simpleRegression.estimateRegressionParameters()));
        properties.get(i).add(String.valueOf(estimateRegressionParameters));
        String estimateRegressionParametersVariance = exporterBean.convertArrayToString(
            simpleRegression.estimateRegressionParametersVariance());
        properties.get(i).add(String.valueOf(estimateRegressionParametersVariance));
      }

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.setTableHeader("Multiple Regression OLS of independant variables " +
          Arrays.toString(exporterBean.getSelectedIndepVarsOLS()) + " and dependant variable "
          + exporterBean.getSelectedDepVarsOLS());
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
      exporterBean.setDescription(
          "<p>Implements ordinary least squares (OLS) to estimate the parameters of a\n" +
              "  multiple linear regression model.</p>\n" +
              " \n" +
              "  <p>The regression coefficients, <code>b</code>, satisfy the normal equations:\n" +
              "  <pre>X<sup>T</sup> X b = X<sup>T</sup> y </pre></p>\n" +
              " \n" +
              "  <p>To solve the normal equations, this implementation uses QR decomposition\n" +
              " <br> of the <b>X</b> matrix. (See  QRDecomposition for details on the\n" +
              " <br>  decomposition algorithm.) The <b>X</b> matrix, also known as the <i>design matrix,</i>\n"
              +
              " <br>  has rows corresponding to sample observations and columns corresponding to independent\n"
              +
              " <br>  variables.  When the model is estimated using an intercept term (i.e. when\n"
              +
              " <br>  Intercept is false as it is by default), the <b>X</b>\n" +
              " <br>  matrix includes an initial column identically equal to 1.  We solve the normal equations\n"
              +
              " <br>  as follows:\n" +
              " <br>  <pre><b> X<sup>T</sup>X b = X<sup>T</sup> y\n" +
              " <br>  (QR)<sup>T</sup> (QR) b = (QR)<sup>T</sup>y\n" +
              " <br>  R<sup>T</sup> (Q<sup>T</sup>Q) R b = R<sup>T</sup> Q<sup>T</sup> y\n" +
              " <br>  R<sup>T</sup> R b = R<sup>T</sup> Q<sup>T</sup> y\n" +
              " <br>  (R<sup>T</sup>)<sup>-1</sup> R<sup>T</sup> R b = (R<sup>T</sup>)<sup>-1</sup> R<sup>T</sup> Q<sup>T</sup> y\n"
              +
              " <br>  R b = Q<sup>T</sup> y </b></pre></p>" +
              "<br>Est. Regr. Std Error - Std. Error of the Estimate - The standard error of the estimate, "
              +
              "also called the root mean square error, is the standard deviation of the error term,<br>"
              +
              " and is the square root of the Mean Square Residual (or Error)." +
              "<br><li> Est. Error Variance - Estimates the variance of the error." +
              "<br><li> Est. Regr. Variance -  the variance of the regressand, ie Var(y)." +
              "<br><li> Calc. Residual Sum Of Squares - the sum of squared residuals" +
              "<br><li> Calc. Adjusted RSquared - the adjusted R-squared statistic, defined by the formula <pre>\n"
              +
              "      R<sup>2</sup><sub>adj</sub> = 1 - [SSR (n - 1)] / [SSTO (n - p)]\n" +
              "      </pre>\n" +
              "      where SSR is the sum of squared residuals},\n" +
              "      SSTO is the total sum of squares}, n is the number\n" +
              "      of observations and p is the number of parameters estimated (including the intercept).</p>\n"
              +
              "     \n" +
              "      <p>If the regression is estimated without an intercept term, what is returned is <pre>\n"
              +
              "      <b> 1 - (1 - calculateRSquared}) * (n / (n - p)) </b>\n" +
              "      </pre></p>" +
              "<br><li> Calc. Total Sum Of Squares -  the sum of squared deviations of Y from its mean.\n"
              +
              "     <br>  <p>If the model has no intercept term, <b>0</b> is used for the\n" +
              "    <br>  mean of Y - i.e., what is returned is the sum of the squared Y values.</p>\n"
              +
              "     <br>  <p>The value returned by this method is the SSTO value used in\n" +
              "    <br>  the calculateRSquared R-squared} computation.</p>" +
              "<br><li> Calc. RSquared -  Returns the R-Squared statistic, defined by the formula <pre>\n"
              +
              "      R<sup>2</sup> = 1 - SSR / SSTO\n" +
              "      </pre>\n" +
              "      where SSR is the ResidualSumOfSquares sum of squared residuals\n" +
              "      and SSTO is the TotalSumOfSquares total sum of squares" +
              "<br><li> Est. Regr. Parameters - Estimates the regression parameters b." +
              "<br><li> Est. Regr. Param. Variance - Estimates the variance of the regression parameters, ie Var(b)."
              +
              "");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}