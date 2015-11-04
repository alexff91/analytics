package com.analytics.web;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
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

public class Covariance implements Serializable {
  private final ExporterBean exporterBean;

  public Covariance(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void covariance() {
    try {
      exporterBean
          .setTableHeader("Covariance of " + Arrays.toString(exporterBean.getMatrixDataCov()));
      List<org.apache.commons.math3.stat.correlation.Covariance> decList
          = new LinkedList<org.apache.commons.math3.stat.correlation.Covariance>();

      ArrayList<Integer> independant = new ArrayList<Integer>();
      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
        for (int ind = 0; ind < exporterBean.getMatrixDataCov().length; ind++) {
          if (exporterBean.getStatisticsColumnTemplate().get(i)
              .equals(exporterBean.getMatrixDataCov()[ind])) {
            independant.add(i);
          }
        }
      }

      int j = 1;
      decList.add(new org.apache.commons.math3.stat.correlation.Covariance());

      double[][] y = new double[exporterBean.getStatisticsValues().size()][independant.size()];
      for (DataValue value : exporterBean.getStatisticsValues()) {

        for (int i = 0; i < independant.size(); i++) {

          String values = value.getValues(j, independant.get(i));
          if (values != null) {
            y[j - 1][i] = Double.valueOf(values);
          }
        }
        j++;
      }

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Value")));
      List<List<String>> properties = new ArrayList<List<String>>(decList.size());
      String[] propertyNames = new String[]{
          "Covariance Matrix", "Pearsons Correlation",
          "Pearsons Correlation Matrix", "Pearsons Correlation PValues",
          "Pearsons Correlation Standard Errors"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0).add(Arrays.toString(exporterBean.getMatrixDataCov()));
      for (int i = 0; i < decList.size(); i++) {

        String covarianceMatrix = exporterBean.convertArrayToString(
            new org.apache.commons.math3.stat.correlation.Covariance(y).getCovarianceMatrix()
                .getData());
        properties.get(i).add(String.valueOf(covarianceMatrix));
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(y);
        String pearsonsCorrelationMatrix = exporterBean.convertArrayToString(
            pearsonsCorrelation.getCorrelationMatrix().getData());
        properties.get(i).add(String.valueOf(pearsonsCorrelationMatrix));
        String pearsonsCorrelationPValues = exporterBean.convertArrayToString(
            pearsonsCorrelation.getCorrelationPValues().getData());
        properties.get(i).add(String.valueOf(pearsonsCorrelationPValues));
        String pearsonsCorrelationStandardErrors = exporterBean.convertArrayToString(
            pearsonsCorrelation.getCorrelationStandardErrors().getData());
        properties.get(i).add(String.valueOf(pearsonsCorrelationStandardErrors));
      }

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
      exporterBean
          .setDescription("Computes covariances for pairs of arrays or columns of a matrix.\n" +
              " The columns of the input matrices are assumed to represent variable values.</p>\n" +
              " \n" +
              " <p>The  argument biasCorrected determines whether or\n" +
              " not computed covariances are bias-corrected.</p>\n" +
              " \n" +
              "  <p>Unbiased covariances are given by the formula</p>\n" +
              "  cov(X, Y) = &Sigma;[(x<sub>i</sub> - E(X))(y<sub>i</sub> - E(Y))] / (n - 1)\n" +
              "  where E(X) is the mean of X and E(Y)\n" +
              "  is the mean of the Y values.\n" +
              " \n" +
              "  <p>Non-bias-corrected estimates use n in place of n - 1" +
              "<br>Pearson Correlation - These numbers measure the strength and direction of the linear relationship"
              +
              "<br> between the two variables.  The correlation coefficient can range from -1 to +1, with -1 indicating"
              +
              " <br>a perfect negative correlation, +1 indicating a perfect positive correlation, and 0 indicating no"
              +
              "<br> correlation at all.  (A variable correlated with itself will always have a correlation coefficient of 1.)"
              +
              " <br> You can think of the correlation coefficient as telling you the extent to which you can guess the value"
              +
              "<br> of one variable given a value of the other variable.  From the scatterplot of the variables read and write"
              +
              "<br> below, we can see that the points tend along a line going from the bottom left to the upper right, which is"
              +
              "<br> the same as saying that the correlation is positive. The .597 is the numerical description of how tightly"
              +
              "<br> around the imaginary line the points lie. If the correlation was higher, the points would tend to be closer"
              +
              "<br> to the line; if it was smaller, they would tend to be further away from the line.  Also note that, by definition,"
              +
              "<br> any variable correlated with itself has a correlation of 1." +
              "<li> Pearsons Correlation PValues -  a matrix of p-values associated with the (two-sided) null\n"
              +
              "hypothesis that the corresponding correlation coefficient is zero.\n" +
              "<p>The values in the matrix are sometimes referred to as the\n" +
              " <i>significance</i> of the corresponding correlation coefficients.</p>\n" +
              " <li> Pearsons Correlation Standard Errors -  a matrix of standard errors associated with the estimates\n"
              +
              "     in the correlation matrix.<br/>\n" +
              "      <p>The formula used to compute the standard error is <br/>\n" +
              "      SE<sub>r</sub> = ((1 - r<sup>2</sup>) / (n - 2))<sup>1/2</sup>\n" +
              "      where r is the estimated correlation coefficient and\n" +
              "     * n is the number of observations in the source dataset.</p>   ");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}