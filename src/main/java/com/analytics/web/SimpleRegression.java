package com.analytics.web;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleRegression implements Serializable {
  private final ExporterBean exporterBean;

  public SimpleRegression(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void simpleRegression() {
    try {
      exporterBean
          .setTableHeader("Simple Regression of dependant variable " + exporterBean.getSelectedDep()
              + " and independant variable " + exporterBean.getSelectedInDep());
      List<org.apache.commons.math3.stat.regression.SimpleRegression> decList
          = new LinkedList<org.apache.commons.math3.stat.regression.SimpleRegression>();

      int dependant = 0;
      int independant = 0;
      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedInDep())) {
          independant = i;
        }
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedDep())) {
          dependant = i;
        }
      }

      int j = 1;
      decList.add(new org.apache.commons.math3.stat.regression.SimpleRegression());
      for (DataValue value : exporterBean.getStatisticsValues()) {

        String ind = value.getValues(j, independant);
        String dep = value.getValues(j, dependant);
        if (ind != null && dep != null) {
          decList.get(0).addData(Double.valueOf(ind), Double.valueOf(dep));
        }

        j++;
      }

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Variable")));
      List<List<String>> properties = new ArrayList<List<String>>(decList.size());
      String[] propertyNames = new String[]{
          "Square Error", "Intercept", "Intercept Std Err", "Sum Of Cross Products",
          "Sum Squared Errors", "N", "Slope Std Err", "Slope", "R-Square",
          "Slope Confidence Interval", "Total Sum Squares"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0)
          .add("dependant variable " + exporterBean.getSelectedDep() + " and independant variable "
              + exporterBean.getSelectedInDep());
      for (int i = 0; i < decList.size(); i++) {

        org.apache.commons.math3.stat.regression.SimpleRegression simpleRegression = decList.get(i);
        double squareError = ExporterBean.roundTo2Decimals(simpleRegression.getMeanSquareError());
        properties.get(i).add(String.valueOf(squareError));
        double intercept = ExporterBean.roundTo2Decimals(simpleRegression.getIntercept());
        properties.get(i).add(String.valueOf(intercept));
        double interceptStdErr = ExporterBean
            .roundTo2Decimals(simpleRegression.getInterceptStdErr());
        properties.get(i).add(String.valueOf(interceptStdErr));
        double sumOfCrossProducts = ExporterBean
            .roundTo2Decimals(simpleRegression.getSumOfCrossProducts());
        properties.get(i).add(String.valueOf(sumOfCrossProducts));
        double sumSquaredErrors = ExporterBean
            .roundTo2Decimals(simpleRegression.getSumSquaredErrors());
        properties.get(i).add(String.valueOf(sumSquaredErrors));
        double n = simpleRegression.getN();
        properties.get(i).add(String.valueOf(n));
        double slopeStdErr = ExporterBean.roundTo2Decimals(simpleRegression.getSlopeStdErr());
        properties.get(i).add(String.valueOf(slopeStdErr));
        double slope = ExporterBean.roundTo2Decimals(simpleRegression.getSlope());
        properties.get(i).add(String.valueOf(slope));
        double rSquare = ExporterBean.roundTo2Decimals(simpleRegression.getRSquare());
        properties.get(i).add(String.valueOf(rSquare));
        double slopeConfidenceInterval = ExporterBean.roundTo2Decimals(
            simpleRegression.getSlopeConfidenceInterval());
        properties.get(i).add(String.valueOf(slopeConfidenceInterval));
        double totalSumSquares = ExporterBean
            .roundTo2Decimals(simpleRegression.getTotalSumSquares());
        properties.get(i).add(String.valueOf(totalSumSquares));
      }

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }

      exporterBean.setDescription("" +
          "<br><li> Square Error - the sum of squared errors divided by the degrees of freedom,\n" +
          "  usually abbreviated MSE." +
          "<br><li> Intercept -   the intercept of the estimated regression line, if" +
          "     intercept is true; otherwise 0." +
          "     <p>\n" +
          "     The least squares estimate of the intercept is computed using the\n" +
          "     <a href=\"http://www.xycoon.com/estimation4.htm\">normal equations</a>.\n" +
          "     The intercept is sometimes denoted b0.</p>\n" +
          "     <p>\n" +
          "     <strong>Preconditions</strong>: <ul>\n" +
          "     <li>At least two observations (with at least two different x values)\n" +
          "     must have been added before invoking this method." +
          "<br><li> Intercept Std Err -  the <a href=\"http://www.xycoon.com/standarderrorb0.htm\">\n"
          +
          "     standard error of the intercept estimate</a>,\n" +
          "     usually denoted s(b0).\n" +
          "     <p>" +
          "<br><li> Sum Of Cross Products - the sum of crossproducts, x<sub>i</sub>*y<sub>i</sub>" +
          "<br><li> Slope Std Err -   the <a href=\"http://www.xycoon.com/SumOfSquares.htm\">\n" +
          "     sum of squared errors</a> (SSE) associated with the regression\n" +
          "      model.\n" +
          "     <p>\n" +
          "     The sum is computed using the computational formula</p>\n" +
          "     <p>\n" +
          "     <code>SSE = SYY - (SXY * SXY / SXX)</code></p>\n" +
          "     <p>\n" +
          "     where <code>SYY</code> is the sum of the squared deviations of the y\n" +
          "     values about their mean, <code>SXX</code> is similarly defined and\n" +
          "     <code>SXY</code> is the sum of the products of x and y mean deviations.\n" +
          "     </p><p>\n" +
          "     <p>\n" +
          "     The return value is constrained to be non-negative - i.e., if due to\n" +
          "     rounding errors the computational formula returns a negative result,\n" +
          "     0 is returned.</p>\n" +
          "     <p>\n" +
          "     <strong>Preconditions</strong>: <ul>\n" +
          "     <li>At least two observations (with at least two different x values)\n" +
          "     must have been added before invoking this method." +
          "     returned.\n" +
          "     </li></ul></p>\n" +
          "     " +
          "<br><li> Slope - the slope of the estimated regression line.\n" +
          "    <p>\n" +
          "    The least squares estimate of the slope is computed using the\n" +
          "    <a href=\"http://www.xycoon.com/estimation4.htm\">normal equations</a>.\n" +
          "    The slope is sometimes denoted b1.</p>\n" +
          "    <p>\n" +
          "    <strong>Preconditions</strong>: <ul>\n" +
          "    <li>At least two observations (with at least two different x values)\n" +
          "    must have been added before invoking this method." +
          "    </li></ul></p>\n" +
          "    " +
          "<br><li> R-Square - the <a href=\"http://www.xycoon.com/coefficient1.htm\">\n" +
          "     coefficient of determination</a>,\n" +
          "     usually denoted r-square.\n" +
          "     <p>\n" +
          "     <strong>Preconditions</strong>: <ul>\n" +
          "     <li>At least two observations (with at least two different x values)\n" +
          "     must have been added before invoking this method. If this method is\n" +
          "     invoked before a model can be estimated, <code>Double,NaN</code> is\n" +
          "     returned.\n" +
          "     </li></ul></p>" +
          "<br><li> Slope Confidence Interval - the half-width of a 95% confidence interval for the slope\n"
          +
          "     estimate.\n" +
          "     <p>\n" +
          "     The 95% confidence interval is</p>\n" +
          "     <p>\n" +
          "     Slope - SlopeConfidenceInterval,\n" +
          "     Slope + SlopeConfidenceInterval</p>\n" +
          "     <p>\n" +
          "     If there are fewer that <strong>three</strong> observations in the\n" +
          "     model, or if there is no variation in x, this returns\n" +
          "     <code>Double.NaN</code>.</p>\n" +
          "     <p>\n" +
          "     <strong>Usage Note</strong>:<br>\n" +
          "     The validity of this statistic depends on the assumption that the\n" +
          "     observations included in the model are drawn from a\n" +
          "     <a href=\"http://mathworld.wolfram.com/BivariateNormalDistribution.html\">\n" +
          "     Bivariate Normal Distribution</a>.</p>" +
          "<br><li> Total Sum Squares - the sum of squared deviations of the y values about their mean.\n"
          +
          "     <p>\n" +
          "     This is defined as SSTO\n" +
          "     <a href=\"http://www.xycoon.com/SumOfSquares.htm\">here</a>.</p>\n" +
          "");
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}