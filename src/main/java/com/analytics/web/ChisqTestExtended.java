package com.analytics.web;

import javastat.inference.ChisqTest;

import java.util.Arrays;

/**
 * Created by Александр on 12.07.2015.
 */
public class ChisqTestExtended extends ChisqTest {
  double[] rowMean;

  double[] columnMean;

  public ChisqTestExtended() {
  }

  public double[] getRowMean() {
    return rowMean;
  }

  public void setRowMean(double[] rowMean) {
    this.rowMean = rowMean;
  }

  public double[] getColumnMean() {
    return columnMean;
  }

  public void setColumnMean(double[] columnMean) {
    this.columnMean = columnMean;
  }
}
