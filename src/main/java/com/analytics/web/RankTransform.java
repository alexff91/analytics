package com.analytics.web;

import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RankTransform implements Serializable {
  private final ExporterBean exporterBean;

  public RankTransform(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void rankTransform() {
    try {
      exporterBean.setTableHeader("Rank Transform");
      LinkedList<LinkedList<Double>> decList = new LinkedList<LinkedList<Double>>();
      int j = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        for (int i = 0;
            i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j)
                .size(); i++) {
          decList.add(new LinkedList<Double>());
        }
        for (int i = 0; i < value.getValues().get(j).size(); i++) {
          String values = value.getValues(j, i);
          if (values != null) {
            decList.get(i).add(Double.valueOf(values));
          }
        }
        j++;
      }
      List<List<String>> properties = new ArrayList<List<String>>(
          exporterBean.getColumnTemplate().size());
      for (int i = 0; i < decList.size(); i++) {

        LinkedList<Double> deccriptor = decList.get(i);
        Double[] ts = deccriptor.toArray(new Double[deccriptor.size()]);
        properties.add(exporterBean.toStringRepr(
            new NaturalRanking(NaNStrategy.valueOf(exporterBean.getNanStrategy()),
                TiesStrategy.valueOf(exporterBean.getTieStrategy()))
                .rank(exporterBean.toDoubleArray(ts))));
      }

      exporterBean.getDataValues().clear();
      exporterBean.getColumns().clear();

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();
      int maxSize = 0;
      for (int c = 0; c < properties.size(); c++) {
        if (properties.get(c).size() > maxSize) {
          maxSize = properties.get(c).size();
        }
      }
      for (int c = 1; c <= properties.size(); c++) {
        if (properties.get(c - 1).size() < maxSize) {
          for (int p = properties.get(c - 1).size(); p < maxSize; p++) {
            properties.get(c - 1).add(null);
          }
        }
      }
      properties = ExporterBean.transpose(properties);
      for (int c = 1; c <= properties.size(); c++) {

        values.put(c, properties.get(c - 1));
        exporterBean.getDataValues().add(new DataValue(c, values));
      }
      exporterBean.getColumnTemplate().clear();
      exporterBean
          .setColumnTemplate(new ArrayList<String>(exporterBean.getStatisticsColumnTemplate()));
      exporterBean.createDynamicColumns();
      exporterBean.nullifyAll();
      exporterBean.setDescription("<p > Ranking based on the natural ordering on doubles.</p>\n " +
          " <p>NaNs are treated according to the configured NaNStrategy and ties\n" +
          "  are handled using the selected TiesStrategy.\n" +
          "  Configuration settings are supplied in optional constructor arguments.\n" +
          "  Defaults are NaNStrategy#FAILED and TiesStrategy#AVERAGE,\n" +
          "  respectively.</p>\n" +
          "  <p>Examples:\n" +
          "  <table border=\"1\" cellpadding=\"3\">\n" +
          "  <tr><th colspan=\"3\">\n" +
          "  Input data: (20, 17, 30, 42.3, 17, 50, Double.NaN, Double.NEGATIVE_INFINITY, 17)\n" +
          "  </th></tr>\n" +
          "  <tr><th>NaNStrategy</th><th>TiesStrategy</th>\n" +
          "  <th><code>rank(data)</code></th>\n" +
          "  <tr>\n" +
          "  <td>default (NaNs maximal)</td>\n" +
          "  <td>default (ties averaged)</td>\n" +
          "  <td>(5, 3, 6, 7, 3, 8, 9, 1, 3)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>default (NaNs maximal)</td>\n" +
          "  <td>MINIMUM</td>\n" +
          "  <td>(5, 2, 6, 7, 2, 8, 9, 1, 2)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>MINIMAL</td>\n" +
          "  <td>default (ties averaged)</td>\n" +
          "  <td>(6, 4, 7, 8, 4, 9, 1.5, 1.5, 4)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>REMOVED</td>\n" +
          "  <td>SEQUENTIAL</td>\n" +
          "  <td>(5, 2, 6, 7, 3, 8, 1, 4)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>MINIMAL</td>\n" +
          "  <td>MAXIMUM</td>\n" +
          "  <td>(6, 5, 7, 8, 5, 9, 2, 2, 5)</td></tr></table></p>\n" +
          "   Strategies for handling NaN values in rank transformations.\n" +
          " <ul>\n" +
          " <li>MINIMAL - NaNs are treated as minimal in the ordering, equivalent to\n" +
          "  (that is, tied with) Double.NEGATIVE_INFINITY.</li>\n" +
          "  <li>MAXIMAL - NaNs are treated as maximal in the ordering, equivalent to\n" +
          "  Double.POSITIVE_INFINITY</li>\n" +
          "  <li>REMOVED - NaNs are removed before the rank transform is applied</li>\n" +
          "  <li>FIXED - NaNs are left \"in place,\" that is the rank transformation is\n" +
          "  applied to the other elements in the input array, but the NaN elements\n" +
          "  are returned unchanged.</li>\n" +
          "  <li>FAILED - If any NaN is encountered in the input array, an appropriate\n" +
          "  exception is thrown</li>\n" +
          " </ul>\n" +
          "  Strategies for handling tied values in rank transformations.\n" +
          "  <ul>\n" +
          "  <li>SEQUENTIAL - Ties are assigned ranks in order of occurrence in the original array,\n"
          +
          "  for example (1,3,4,3) is ranked as (1,2,4,3)</li>\n" +
          "  <li>MINIMUM - Tied values are assigned the minimum applicable rank, or the rank\n" +
          "  of the first occurrence. For example, (1,3,4,3) is ranked as (1,2,4,2)</li>\n" +
          "  <li>MAXIMUM - Tied values are assigned the maximum applicable rank, or the rank\n" +
          "  of the last occurrence. For example, (1,3,4,3) is ranked as (1,3,4,3)</li>\n" +
          "  <li>AVERAGE - Tied values are assigned the average of the applicable ranks.\n" +
          "  For example, (1,3,4,3) is ranked as (1,2.5,4,2.5)</li>\n" +
          " <li>RANDOM - Tied values are assigned a random integer rank from among the\n" +
          " applicable values. The assigned rank will always be an integer, (inclusively)\n" +
          " between the values returned by the MINIMUM and MAXIMUM strategies.</li>\n" +
          " </ul> ");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }
}