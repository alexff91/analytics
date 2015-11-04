package com.analytics.web;

import org.apache.commons.math3.stat.inference.TestUtils;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Kolmogorov implements Serializable {
  private final ExporterBean exporterBean;

  public Kolmogorov(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void kolmogorov() {
    try {
      exporterBean.setTableHeader(
          "Kolmogorov test of " + exporterBean.getSelectedXKolmogorov() + " and " + exporterBean
              .getSelectedYKolmogorov());
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedXKolmogorov())) {
          xIndexes = i;
        }
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedYKolmogorov())) {
          yIndexes = i;
        }
      }

      double[] x = new double[exporterBean.getStatisticsValues().size()];
      double[] y = new double[exporterBean.getStatisticsValues().size()];

      int rowInd = 1;
      int rowIndY = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String xvalues = value.getValues(rowInd, xIndexes);
        String yvalues = value.getValues(rowInd, yIndexes);
        if (xvalues != null) {
          x[rowInd - 1] = Double.valueOf(xvalues);
          rowInd++;
        }
        if (yvalues != null) {
          y[rowIndY - 1] = Double.valueOf(yvalues).longValue();
          rowIndY++;
        }
      }

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Value")));
      List<List<String>> properties = new ArrayList<List<String>>(4);
      String[] propertyNames = new String[]{
          "Kolmogorov-Smirnov Test", " Kolmogorov-Smirnov Statistic",
          "Kolmogorov-Smirnov Test strict"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0).add(
          exporterBean.getSelectedXKolmogorov() + " and " + exporterBean.getSelectedYKolmogorov());

      properties.get(0)
          .add(
              String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.kolmogorovSmirnovTest(x, y))));
      properties.get(0)
          .add(String
              .valueOf(ExporterBean.roundTo2Decimals(TestUtils.kolmogorovSmirnovStatistic(x, y))));
      properties.get(0)
          .add(String
              .valueOf(ExporterBean.roundTo2Decimals(TestUtils.kolmogorovSmirnovTest(x, y, true))));

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
      exporterBean.setDescription(
          "<br>A goodness-of-fit test for any statistical distribution. The test relies" +
              "<br> on the fact that the value of the sample cumulative density function is asymptotically normally distributed.\n"
              +
              "\n" +
              "To apply the Kolmogorov-Smirnov test, calculate the cumulative frequency (normalized by the sample size)"
              +
              "<br> of the observations as a function of class. Then calculate the cumulative frequency for a true "
              +
              "<br>distribution (most commonly, the normal distribution). Find the greatest discrepancy between the"
              +
              "<br> observed and expected cumulative frequencies, which is called the \"D-statistic.\" Compare this"
              +
              "<br> against the critical D-statistic for that sample size. If the calculated D-statistic is greater"
              +
              "<br> than the critical one, then reject the null hypothesis that the distribution is of the expected form. "
              +
              "<br>The test is an R-estimate." +
              " <br>\n" +
              "<b>F Value</b> and <b>Pr &gt; F</b> - These are the F Value and p-value, \n" +
              "respectively, testing the null hypothesis that an individual predictor \n" +
              "in the model does not explain a significant proportion of the variance, given \n" +
              "the other variables are in the model. \n" +
              "F Value is computed as MS<sub>Source Var\n" +
              "</sub>/ MS<sub>Error</sub>. Under the null \n" +
              "hypothesis, F Value follows a central F-distribution with numerator DF = DF<sub>Source \n"
              +
              "Var</sub>, where Source Var is the predictor variable of interest, and denominator DF =DF<sub>Error</sub>. \n"
              +
              "Following the point made in Source, superscript o, we focus only on the \n" +
              "interaction term.<br>" +
              "Kolmogorov-Smirnov Test - Given a double[] array data of values, to evaluate the null\n"
              +
              "hypothesis that the values are drawn from a unit normal distribution returns the p-value"
              +
              "<br> Kolmogorov-Smirnov Statistic returns the D-statistic" +
              "<br> Kolmogorov-Smirnov Test strict - exact computation of the p-value (overriding the selection of estimation method).");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}