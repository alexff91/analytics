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

public class GTest implements Serializable {
  private final ExporterBean exporterBean;

  public GTest(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void gtest() {
    try {
      exporterBean.setTableHeader(
          "G test of" + exporterBean.getSelectedXGTest() + " and " + exporterBean
              .getSelectedYGTest());
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedXGTest())) {
          xIndexes = i;
        }
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedYGTest())) {
          yIndexes = i;
        }
      }

      ArrayList<Double> xList = new ArrayList<Double>();
      ArrayList<Long> yList = new ArrayList<Long>();

      int rowInd = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String xvalues = value.getValues(rowInd, xIndexes);
        String yvalues = value.getValues(rowInd, yIndexes);
        if (xvalues != null) {
          xList.add(Double.valueOf(xvalues));
        }
        rowInd++;
        if (yvalues != null) {
          yList.add(Double.valueOf(yvalues).longValue());
        }
      }
      double[] x = new double[xList.size()];
      long[] y = new long[yList.size()];
      for (int i = 0; i < xList.size(); i++) {
        x[i] = (double) xList.get(i);
        y[i] = (long) yList.get(i);
      }
      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Value")));
      List<List<String>> properties = new ArrayList<List<String>>(4);
      String[] propertyNames = new String[]{
          "G-test", " p-value  G-test", "G-test using a fixed significance level"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0)
          .add(exporterBean.getSelectedXGTest() + " and " + exporterBean.getSelectedYGTest());

      properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.g(x, y))));
      properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.gTest(x, y))));
      properties.get(0)
          .add(String.valueOf(TestUtils.gTest(x, y, Double.valueOf(exporterBean.getAlpha()))));

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
      exporterBean.setDescription(
          "<br>G tests are an alternative to chi-square tests that are recommended when observed" +
              "<br> counts are small and / or incidence probabilities for some cells are small." +
              "<br> See Ted Dunning's paper, Accurate Methods for the Statistics of Surprise and Coincidence(<href>http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.14.5962</href>)"
              +
              "<br> for background and an empirical analysis showing now chi-square statistics can be misleading"
              +
              "<br> in the presence of low incidence probabilities. " +
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
              "To test the null hypothesis that observed conforms to expected with alpha siginficance level"
              +
              "<br> (equiv. 100 * (1-alpha)% confidence) where 0 < alpha < 1 use: alpha value");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}