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

public class TwoTest implements Serializable {
  private final ExporterBean exporterBean;

  public TwoTest(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void twotest() {
    try {
      exporterBean.setTableHeader(
          "Two samle T-test of " + exporterBean.getSelectedXTwoT() + " and " + exporterBean
              .getSelectedYTwoT());
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedXTwoT())) {
          xIndexes = i;
        }
        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedYTwoT())) {
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
          "T-statistic", "P-value  t-statistic", "T-test using a fixed sign. level"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0)
          .add(exporterBean.getSelectedXTwoT() + " and " + exporterBean.getSelectedYTwoT());

      properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.pairedT(x, y))));
      properties.get(0)
          .add(String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.pairedTTest(x, y))));
      properties.get(0).add(
          String.valueOf(TestUtils.pairedTTest(x, y, Double.valueOf(exporterBean.getAlpha()))));

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}