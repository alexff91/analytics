package com.analytics.web;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
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

public class PearsonCorrelation implements Serializable {
  private final ExporterBean exporterBean;

  public PearsonCorrelation(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void pearsonsCorrelation() {
    try {

      List<Covariance> decList = new LinkedList<Covariance>();

      int dependant = 0;
      int independant = 0;

      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedXCovar())) {
          dependant = i;
        }
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedYCovar())) {
          independant = i;
        }
      }

      decList.add(new Covariance());

      double[] x = new double[exporterBean.getStatisticsValues().size()];
      double[] y = new double[exporterBean.getStatisticsValues().size()];
      int rowInd = 1;
      boolean containsNan = false;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String xvalues = value.getValues(rowInd, dependant);
        String valuesy = value.getValues(rowInd, independant);
        if (xvalues != null && valuesy != null) {
          x[rowInd - 1] = Double.valueOf(xvalues);
          y[rowInd - 1] = Double.valueOf(valuesy);
        }
        rowInd++;
      }
      double covariance = decList.get(0).covariance(x, y);
      double covarianceWithoutBias = decList.get(0).covariance(x, y, false);
      double covarianceWithBias = decList.get(0).covariance(x, y, true);
      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Variable")));
      List<List<String>> properties = new ArrayList<List<String>>(decList.size());
      String[] propertyNames = new String[]{
          "Covariance", "Covariance Without Bias", "Covariance With Bias",
          "Pearsons Correlation", "Spearmans Correlation", "Kendalls Correlation"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0).add(
          "Correlation of " + exporterBean.getSelectedYCovar() + " and " + exporterBean
              .getSelectedXCovar());
      for (int i = 0; i < decList.size(); i++) {

        Covariance simpleRegression = decList.get(i);
      }
      properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(covariance)));
      properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(covarianceWithoutBias)));
      properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(covarianceWithBias)));
      properties.get(0)
          .add(String
              .valueOf(ExporterBean.roundTo2Decimals(new PearsonsCorrelation().correlation(x, y))));
      if (!containsNan) {
        properties.get(0)
            .add(String.valueOf(
                ExporterBean.roundTo2Decimals(new SpearmansCorrelation().correlation(x, y))));
      }
      properties.get(0)
          .add(String
              .valueOf(ExporterBean.roundTo2Decimals(new KendallsCorrelation().correlation(x, y))));
      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.setTableHeader(
          "Covariance and Correlation of " + exporterBean.getSelectedYCovar() + " and "
              + exporterBean.getSelectedXCovar());
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
              "     n is the number of observations in the source dataset.</p>  " +
              "<br>  Implementation of Kendall's Tau-b rank correlation</a>.\n" +
              " <p>\n" +
              "  A pair of observations (x<sub>1</sub>, y<sub>1</sub>) and\n" +
              "  (x<sub>2</sub>, y<sub>2</sub>) are considered <i>concordant</i> if\n" +
              "  x<sub>1</sub> &lt; x<sub>2</sub> and y<sub>1</sub> &lt; y<sub>2</sub>\n" +
              "  or x<sub>2</sub> &lt; x<sub>1</sub> and y<sub>2</sub> &lt; y<sub>1</sub>.\n" +
              "  The pair is <i>discordant</i> if x<sub>1</sub> &lt; x<sub>2</sub> and\n" +
              " y<sub>2</sub> &lt; y<sub>1</sub> or x<sub>2</sub> &lt; x<sub>1</sub> and\n" +
              "  y<sub>1</sub> &lt; y<sub>2</sub>.  If either x<sub>1</sub> = x<sub>2</sub>\n" +
              "  or y<sub>1</sub> = y<sub>2</sub>, the pair is neither concordant nor\n" +
              " discordant.\n" +
              "  <p>\n" +
              "  Kendall's Tau-b is defined as:\n" +
              "  <pre>\n" +
              "  tau<sub>b</sub> = (n<sub>c</sub> - n<sub>d</sub>) / sqrt((n<sub>0</sub> - n<sub>1</sub>) * (n<sub>0</sub> - n<sub>2</sub>))\n"
              +
              "  </pre>\n" +
              "  <p>\n" +
              "  where:\n" +
              "  <ul>\n" +
              "     <li>n<sub>0</sub> = n * (n - 1) / 2</li>\n" +
              "     <li>n<sub>c</sub> = Number of concordant pairs</li>\n" +
              "     <li>n<sub>d</sub> = Number of discordant pairs</li>\n" +
              "     <li>n<sub>1</sub> = sum of t<sub>i</sub> * (t<sub>i</sub> - 1) / 2 for all i</li>\n"
              +
              "     <li>n<sub>2</sub> = sum of u<sub>j</sub> * (u<sub>j</sub> - 1) / 2 for all j</li>\n"
              +
              "     <li>t<sub>i</sub> = Number of tied values in the i<sup>th</sup> group of ties in x</li>\n"
              +
              "     <li>u<sub>j</sub> = Number of tied values in the j<sup>th</sup> group of ties in y</li>\n"
              +
              " </ul>\n" +
              " <p>\n" +
              " This implementation uses the O(n log n) algorithm described in\n" +
              " William R. Knight's 1966 paper \"A Computer Method for Calculating\n" +
              " Kendall's Tau with Ungrouped Data\" in the Journal of the American\n" +
              " Statistical Association.\n" +
              " \n" +
              "   <a href=\"http://en.wikipedia.org/wiki/Kendall_tau_rank_correlation_coefficient\">\n"
              +
              "  Kendall tau rank correlation coefficient (Wikipedia)</a>\n" +
              "  <a href=\"http://www.jstor.org/stable/2282833\">A Computer\n" +
              " Method for Calculating Kendall's Tau with Ungrouped Data</a>\n" +
              " " +
              " <br>  Spearman's rank correlation. This implementation performs a rank\n" +
              " transformation on the input data and then computes  PearsonsCorrelation\n" +
              " on the ranked data.\n" +
              " <p>\n" +
              "  By default, ranks are computed using NaturalRanking with default\n" +
              "  strategies for handling NaNs and ties in the data (NaNs maximal, ties averaged).\n"
              +
              "  The ranking algorithm can be set using a constructor argument.");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}