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

public class TTTest implements Serializable {
  private final ExporterBean exporterBean;

  public TTTest(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void ttest() {
    try {
      exporterBean.setTableHeader(
          "T-test of " + exporterBean.getSelectedXTTEST() + " with alpha = " + exporterBean
              .getAlpha() + " and Mu = "
              + exporterBean.getMuValue());
      int xIndexes = 0;

      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedXTTEST())) {
          xIndexes = i;
        }
      }

      double[] x = new double[exporterBean.getStatisticsValues().size()];

      int rowInd = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String xvalues = value.getValues(rowInd, xIndexes);
        if (xvalues != null) {
          x[rowInd - 1] = Double.valueOf(xvalues);
          rowInd++;
        }
      }

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();
      exporterBean.getColumnTemplate().clear();
      exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Value")));
      List<List<String>> properties = new ArrayList<List<String>>(4);
      String[] propertyNames = new String[]{
          " t-statistic associated with a one-sample t-test comparing the mean",
          " p-value associated with the null hypothesis", "test using a fixed significance level"
      };
      exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<String>());
      properties.get(0).add(
          exporterBean.getSelectedXTTEST() + " with alpha = " + exporterBean.getAlpha()
              + " and Mu = " + exporterBean.getMuValue());

      properties.get(0)
          .add(String.valueOf(ExporterBean
              .roundTo2Decimals(TestUtils.t(Double.valueOf(exporterBean.getMuValue()), x))));
      properties.get(0)
          .add(String.valueOf(ExporterBean
              .roundTo2Decimals(TestUtils.tTest(Double.valueOf(exporterBean.getMuValue()), x))));
      properties.get(0)
          .add(String.valueOf(TestUtils.tTest(Double.valueOf(exporterBean.getMuValue()), x,
              Double.valueOf(exporterBean.getAlpha()))));

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