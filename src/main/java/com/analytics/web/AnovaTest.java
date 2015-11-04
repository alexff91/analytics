package com.analytics.web;

import org.apache.commons.math3.stat.inference.TestUtils;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AnovaTest implements Serializable {
  private final ExporterBean exporterBean;

  public AnovaTest(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void anova() {
    try {
      exporterBean.setTableHeader("Anova test");
      ArrayList<Integer> independant = new ArrayList<Integer>();
      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
        for (int ind = 0; ind < exporterBean.getTestsData().length; ind++) {
          if (exporterBean.getStatisticsColumnTemplate().get(i)
              .equals(exporterBean.getTestsData()[ind])) {
            independant.add(i);
          }
        }
      }

      List<List<Object>> y = new LinkedList<List<Object>>();
      for (int i = 0; i < independant.size(); i++) {
        int j = 1;
        int rowInd = 1;
        for (DataValue value : exporterBean.getStatisticsValues()) {

          String values = value.getValues(j, independant.get(i));
          if (values != null) {
            if (i == 0) {
              y.add(new LinkedList<Object>());
            }
            y.get(rowInd - 1).add(Double.valueOf(values));

            rowInd++;
          }
          j++;
        }
      }

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Result")));
      exporterBean.getColumnTemplate().add(0, "Property");
      List<List<String>> properties = new ArrayList<List<String>>(4);
      String[] propertyNames = new String[]{
          " ANOVA F-values", " ANOVA  p-values",
          "One-Way ANOVA test with significance level "
      };
      for (int i = 0; i < propertyNames.length; i++) {
        properties.add(new ArrayList<String>());
        properties.get(i).add(propertyNames[i]);
      }

      List<List<Object>> transposed = ExporterBean.transpose(y);
      List<double[]> classes = new ArrayList<double[]>();
      for (List<Object> var : transposed) {
        double[] array = new double[var.size()];
        for (int i = 0; i < array.length; i++) {
          if (var.get(i) != null) {
            array[i] = ((Double) var.get(i)).doubleValue();
          } else {
            array[i] = Double.NaN;
          }
        }
        classes.add(array);
      }
      double fStatistic = TestUtils.oneWayAnovaFValue(classes);
      properties.get(0).add(String.valueOf(fStatistic));

      double pValue = TestUtils.oneWayAnovaPValue(classes);
      properties.get(1).add(String.valueOf(pValue));

      double signifValue = TestUtils.oneWayAnovaPValue(classes);
      properties.get(2).add(String.valueOf(signifValue));
      TestUtils.oneWayAnovaTest(classes, Double.valueOf(exporterBean.getAlpha()));

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();

      exporterBean.setDescription(" <br>\n" +
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
          "One-Way ANOVA test with significance level set at 0.01  test will, assuming assumptions\n"
          +
          " are met, reject the null hypothesis incorrectly only about one in 100 times.");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}