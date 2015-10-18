package com.analytics.web;

import java.util.List;
import java.util.Map;

/**
 * Created by Александр on 09.03.2015.
 */
public class DataValue {
  private final Integer id;

  private final Map<Integer, List<String>> values;

  private List<String> rowValues = null;

  public DataValue(Integer randomId, Map<Integer, List<String>> values) {

    this.id = randomId;
    this.values = values;
    for (Map.Entry<Integer, List<String>> entry : values.entrySet()) {
      rowValues = entry.getValue();
    }
  }

  public Integer getId() {
    return id;
  }

  public String getValues(Integer key, int index) {
    if (values != null && values.get(key) != null && index < values.get(key).size()) {
      return values.get(key).get(index);
    } else {
      return null;
    }
  }

  public void setValues(Integer key, int index, String value) {
    if (values != null && values.get(key) != null && index < values.get(key).size()) {
      values.get(key).set(index, value);
    }
  }

  public Map<Integer, List<String>> getValues() {
    return values;
  }

  public Map<Integer, List<String>> addValue(double v, int c) {
    values.get(c).add(String.valueOf(v));
    return values;
  }

  public List<String> getRowValues() {
    return rowValues;
  }

  public void setRowValues(List<String> rowValues) {
    this.rowValues = rowValues;
  }
}
