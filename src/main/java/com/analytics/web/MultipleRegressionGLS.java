package com.analytics.web;

import org.apache.commons.math3.stat.regression.GLSMultipleLinearRegression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MultipleRegressionGLS implements Serializable {
  private final ExporterBean exporterBean;

  public MultipleRegressionGLS(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void multipleRegressionGls() {
    try {

      List<GLSMultipleLinearRegression> decList = new LinkedList<GLSMultipleLinearRegression>();

      int dependant = 0;
      ArrayList<Integer> independant = new ArrayList<Integer>();
      ArrayList<Integer> observations = new ArrayList<Integer>();
      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
        for (int ind = 0; ind < exporterBean.getSelectedIndepVars().length; ind++) {
          if (exporterBean.getStatisticsColumnTemplate().get(i)
              .equals(exporterBean.getSelectedIndepVars()[ind])) {
            independant.add(i);
          }
        }
        for (int ind = 0; ind < exporterBean.getSelectedObsVars().length; ind++) {
          if (exporterBean.getStatisticsColumnTemplate().get(i)
              .equals(exporterBean.getSelectedObsVars()[ind])) {
            observations.add(i);
          }
        }
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedDepVars())) {
          dependant = i;
        }
      }

      decList.add(new GLSMultipleLinearRegression());

      double[] x = new double[exporterBean.getStatisticsValues().size()];
      double[][] y = new double[exporterBean.getStatisticsValues().size()][independant.size()];
      double[][] o = new double[exporterBean.getStatisticsValues().size()][observations.size()];
      int rowInd = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String stringValue = value.getValues(rowInd, dependant);
        if (stringValue != null) {
          x[rowInd - 1] = Double.valueOf(stringValue);
          for (int i = 0; i < independant.size(); i++) {

            String values = value.getValues(rowInd, independant.get(i));
            if (values == null) {
              double[] smallerY = new double[y[i].length - 1];
              System.arraycopy(y[i], 0, smallerY, 0, rowInd - 1);
              y[i] = smallerY;
            } else {
              y[rowInd - 1][i] = Double.valueOf(values);
            }
          }
          for (int i = 0; i < observations.size(); i++) {

            String obsValue = value.getValues(rowInd, observations.get(i));
            if (obsValue != null) {
              o[rowInd - 1][i] = Double.valueOf(obsValue);
            }
          }
          rowInd++;
        }
      }
      decList.get(0).newSampleData(x, y, o);

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Variable")));

      List<List<String>> properties = new ArrayList<List<String>>(decList.size());
      String[] propertyNames = new String[]{
          "Est. Regr. Std Error", "Est. Error Variance",
          "Est. Regr. Variance", "Est. Regr. Param. Variance",
          "Est. Regr. Param. Std Errors", "Est. Residuals", "Est. Regr. Param."
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0).add("Independant variables " +
          Arrays.toString(exporterBean.getSelectedIndepVars()) + " and dependant variable "
          + exporterBean.getSelectedDepVars() + " and observed variables " + Arrays
          .toString(exporterBean.getSelectedObsVars()));

      for (int i = 0; i < decList.size(); i++) {

        GLSMultipleLinearRegression simpleRegression = decList.get(i);
        double estimateRegressionStandardError = ExporterBean.roundTo2Decimals(
            simpleRegression.estimateRegressionStandardError());
        properties.get(i).add(String.valueOf(estimateRegressionStandardError));
        double estimateErrorVariance = ExporterBean
            .roundTo2Decimals(simpleRegression.estimateErrorVariance());
        properties.get(i).add(String.valueOf(estimateErrorVariance));
        double estimateRegressandVariance = ExporterBean.roundTo2Decimals(
            simpleRegression.estimateRegressandVariance());
        properties.get(i).add(String.valueOf(estimateRegressandVariance));
        String estimateRegressionParametersVariance = exporterBean.convertArrayToString(
            simpleRegression.estimateRegressionParametersVariance());
        properties.get(i).add(String.valueOf(estimateRegressionParametersVariance));
        String estimateRegressionParametersStandardErrors = Arrays
            .toString(simpleRegression.estimateRegressionParametersStandardErrors());
        properties.get(i).add(String.valueOf(estimateRegressionParametersStandardErrors));
        String estimateResiduals = Arrays.toString(simpleRegression.estimateResiduals());
        properties.get(i).add(String.valueOf(estimateResiduals));
        String estimateRegressionParameters = Arrays
            .toString(simpleRegression.estimateRegressionParameters());
        properties.get(i).add(String.valueOf(estimateRegressionParameters));
      }

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.setTableHeader("Multiple Regression GLS of independant variables " +
          Arrays.toString(exporterBean.getSelectedIndepVars()) + " and dependant variable "
          + exporterBean.getSelectedDepVars() + " and observed variables " + Arrays
          .toString(exporterBean.getSelectedObsVars()));
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
      exporterBean.setDescription(" * The GLS implementation of multiple linear regression.\n" +
          " \n" +
          "  GLS assumes a general covariance matrix Omega of the error\n" +
          "  <pre>\n" +
          "  u ~ N(0, Omega)\n" +
          "  </pre>\n" +
          " \n" +
          "  Estimated by GLS,\n" +
          "  <pre>\n" +
          "  b=(X' Omega^-1 X)^-1X'Omega^-1 y\n" +
          "  </pre>\n" +
          "  whose variance is\n" +
          "  <pre>\n" +
          "  Var(b)=(X' Omega^-1 X)^-1\n" +
          "  </pre>" +
          "<br>Est. Regr. Std Error - Std. Error of the Estimate - The standard error of the estimate, "
          +
          "also called the root mean square error, is the standard deviation of the error term,<br>"
          +
          " and is the square root of the Mean Square Residual (or Error)." +
          "<br><li> Est. Error Variance - Estimates the variance of the error." +
          "<br><li> Est. Regr. Variance -  the variance of the regressand, ie Var(y)." +
          "<br><li> Calc. Residual Sum Of Squares - the sum of squared residuals" +
          "<br><li> Est. Regr. Parameters - Estimates the regression parameters b." +
          "<br><li> Est. Regr. Param. Variance - Estimates the variance of the regression parameters, ie Var(b)."
          +
          "<br><li> Est. Regr. Param. Std Errors - Returns the standard errors of the regression parameters."
          +
          "<br><li> Est. Residuals - Estimates the residuals, ie u = y - X*b." +
          "<br><li> Est. Regr. Param. - Estimates the regression parameters b.");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}