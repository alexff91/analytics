package com.analytics.web;

/**
 * Created by Александр on 02.04.2017.
 */
public class MappedVariable {
  String key;

  String value;

  String column;

  public MappedVariable(final String key, final String value, final String column) {
    this.key = key;
    this.value = value;
    this.column = column;
  }

  public String getKey() {
    return key;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  public String getColumn() {
    return column;
  }

  public void setColumn(final String column) {
    this.column = column;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final MappedVariable that = (MappedVariable) o;

    if (column != null ? !column.equals(that.column) : that.column != null) {
      return false;
    }
    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (column != null ? column.hashCode() : 0);
    return result;
  }
}
