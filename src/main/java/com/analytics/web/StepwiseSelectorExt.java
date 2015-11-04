package com.analytics.web;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import multipleLinearRegression.Feature;
import simpleLinearRegression.SimpleLinearRegression;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

public class StepwiseSelectorExt {
  private MultipleLinearRegressionExt regressionToSubset;

  private MultipleLinearRegressionExt[] regressions;

  private SimpleLinearRegression[] orderedSingleFeatureRegressions;

  private String thresholdVariable;

  private double thresholdValue;

  private String htmlReportContent = "";

  static private Comparator<SimpleLinearRegression> orderRss;

  static private Comparator<SimpleLinearRegression> orderRSquared;

  static private Comparator<MultipleLinearRegressionExt> orderRssMLR;

  static private Comparator<MultipleLinearRegressionExt> orderRSquaredMLR;

  static {
    orderRss = new Comparator<SimpleLinearRegression>() {
      @Override
      public int compare(SimpleLinearRegression o1,
          SimpleLinearRegression o2) {
        if (o1.getRss() == o2.getRss()) {
          return 0;
        } else if (o1.getRss() > o2.getRss()) {
          return 1;
        } else {
          return -1;
        }
      }
    };

    orderRssMLR = new Comparator<MultipleLinearRegressionExt>() {
      @Override
      public int compare(MultipleLinearRegressionExt o1,
          MultipleLinearRegressionExt o2) {
        if (o1.getRss() == o2.getRss()) {
          return 0;
        } else if (o1.getRss() > o2.getRss()) {
          return 1;
        } else {
          return -1;
        }
      }
    };

    orderRSquaredMLR = new Comparator<MultipleLinearRegressionExt>() {
      @Override
      public int compare(MultipleLinearRegressionExt o1,
          MultipleLinearRegressionExt o2) {
        if (o1.getAdjustedRSquared() == o2.getAdjustedRSquared()) {
          return 0;
        } else if (o1.getAdjustedRSquared() > o2.getAdjustedRSquared()) {
          return -1;
        } else {
          return 1;
        }
      }
    };

    orderRSquared = new Comparator<SimpleLinearRegression>() {
      @Override
      public int compare(SimpleLinearRegression o1,
          SimpleLinearRegression o2) {
        if (o1.getAdjustedRSquared() == o2.getAdjustedRSquared()) {
          return 0;
        } else if (o1.getAdjustedRSquared() > o2.getAdjustedRSquared()) {
          return -1;
        } else {
          return 1;
        }
      }
    };
  }

  public MultipleLinearRegressionExt getRegressionToSubset() {
    return regressionToSubset;
  }

  public void setRegressionToSubset(MultipleLinearRegressionExt regressionToSubset) {
    this.regressionToSubset = regressionToSubset;
  }

  public MultipleLinearRegressionExt[] getRegressions() {
    return regressions;
  }

  public void setRegressions(MultipleLinearRegressionExt[] regressions) {
    this.regressions = regressions;
  }

  public SimpleLinearRegression[] getOrderedSingleFeatureRegressions() {
    return orderedSingleFeatureRegressions;
  }

  public void setOrderedSingleFeatureRegressions(
      SimpleLinearRegression[] orderedSingleFeatureRegressions) {
    this.orderedSingleFeatureRegressions = orderedSingleFeatureRegressions;
  }

  public String getThresholdVariable() {
    return thresholdVariable;
  }

  public void setThresholdVariable(String thresholdVariable) {
    this.thresholdVariable = thresholdVariable;
  }

  public double getThresholdValue() {
    return thresholdValue;
  }

  public void setThresholdValue(double thresholdValue) {
    this.thresholdValue = thresholdValue;
  }

  public String getHtmlReportContent() {
    return htmlReportContent;
  }

  public void setHtmlReportContent(String htmlReportContent) {
    this.htmlReportContent = htmlReportContent;
  }

  /*
   * Orders the orderedSingleFeatureRegressions array in ascendent
   * order by its RSS value.
   */
  public void orderRSSAsc() {
    Arrays.sort(this.orderedSingleFeatureRegressions, orderRss);
  }

  /*
   * Orders the orderedSingleFeatureRegressions array in descendent
   * order by its Adjusted R-Squared value.
   */
  public void orderAdjRSquaredDesc() {
    Arrays.sort(this.orderedSingleFeatureRegressions, orderRSquared);
  }

  /*
   * Orders a MultipleLinearRegressionExt array passed as parameter
   * in ascendent order by its RSS value.
   */
  public void orderRssMultipleRegressionAsc(MultipleLinearRegressionExt[] mlr) {
    Arrays.sort(mlr, orderRssMLR);
  }

  /*
   * Orders a MultipleLinearRegressionExt array passed as parameter
   * in descendent order by its Adjusted R-Squared value.
   */
  public void orderAdjRSquaredMultipleRegressionDesc(MultipleLinearRegressionExt[] mlr) {
    Arrays.sort(mlr, orderRSquaredMLR);
  }

  /*
   * Constructor of the BestSubsetSelector object. Receives as parameters
   * a threshold variable and a threshold value. The possible threshold
   * variables are:
   * "RSS" - Residual Sum of Squares used as a threshold;
   * "AdjRSquared" - Adjusted R² used as a threshold;
   * "NFeatures" - number of features used as a threshold.
   *
   */
  public StepwiseSelectorExt(MultipleLinearRegressionExt reg, String thrVar) {
    this.regressionToSubset = reg;
    this.regressions = new MultipleLinearRegressionExt[this.regressionToSubset.getFeatures().length
        - 1];
    this.thresholdVariable = thrVar;
  }

  /*
   * Orders an array of SimpleLinearRegressions by the lowest RSS,
   * through the computing of n different Linear Regressions (each with
   * one Feature only), where n is the number of Features of the
   * MultipleLinearRegressionExt regressionToSubset object.
   */
  public void orderFeaturesByThreshold() {
    this.orderedSingleFeatureRegressions = new SimpleLinearRegression[
        this.regressionToSubset.getFeatures().length - 1];
    int regressionCounter = 0;
    for (int i = 1; i < this.regressionToSubset.getFeatures().length; i++) {
      this.orderedSingleFeatureRegressions[regressionCounter] = new SimpleLinearRegression(
          this.regressionToSubset.getFeatures()[i].getDataValues(),
          this.regressionToSubset.getPredictor().getDataValues(),
          this.regressionToSubset.getFeatures()[i].getName());
      this.orderedSingleFeatureRegressions[regressionCounter].doSimpleLinearRegression();

      regressionCounter++;
    }
    if (this.thresholdVariable.equals("RSS")) {
      this.orderRSSAsc();
    } else {
      this.orderAdjRSquaredDesc();
    }
  }

  /*
   * Returns an array of Features with the first set of Features
   * (done via SimpleLinearRegression) ordered by threshold.
   */
  private Feature[] getOrderedFeatures() {
    Feature[] features = new Feature[this.orderedSingleFeatureRegressions.length];
    for (int i = 0; i < this.orderedSingleFeatureRegressions.length; i++) {
      features[i] = new Feature((i + 1),
          this.orderedSingleFeatureRegressions[i].getFeatureName(),
          this.orderedSingleFeatureRegressions[i].getFeatureValues());
    }
    return features;
  }

  /*
   * Returns a matrix of Features, according to the step that the stepwise
   * selection process is in. For example, if the step is 2, it builds a
   * matrix like:
   * [x1,x1,x1,x1,x1]
   * [x2,x3,x4,x5,x6]
   * if the order of the features according to the threshold is x1->x2->x3
   * ->x4->x5->x6.
   */
  private Feature[][] buildCombinationOfFeatures(int step) {
    int numberFeatures = this.orderedSingleFeatureRegressions.length;
    Feature[][] featureCombination = new Feature[numberFeatures - step + 1][step];
    int featureCounter = step - 1;

    System.out
        .println("\nSize of Variable Combination: " + (numberFeatures - step + 1) + " x " + step);

    for (int i = 0; i < featureCombination.length; i++) {
      for (int j = 0; j < (step - 1); j++) {
        Feature f = new Feature((j + 1),
            this.orderedSingleFeatureRegressions[j].getFeatureName(),
            this.orderedSingleFeatureRegressions[j].getFeatureValues());
        featureCombination[i][j] = f;
      }

      for (int j = (step - 1); j < featureCombination[i].length; j++) {
        Feature f = new Feature((j + 1),
            this.orderedSingleFeatureRegressions[featureCounter].getFeatureName(),
            this.orderedSingleFeatureRegressions[featureCounter].getFeatureValues());
        featureCombination[i][j] = f;
        featureCounter++;
      }
    }
    return featureCombination;
  }

  /*
   * Receives a matrix of Features, resulting from the buildCombinationOfFeatures
   * method and returns an array of MultipleLinearRegressionExt objects, after
   * executing one Multiple Linear Regression per line of the Features matrix.
   */
  private MultipleLinearRegressionExt[] doRegressionsFromFeatureMatrix(Feature[][] featureMatrix) {
    MultipleLinearRegressionExt[] regressions
        = new MultipleLinearRegressionExt[featureMatrix.length];
    for (int i = 0; i < regressions.length; i++) {
      MultipleLinearRegressionExt mlr = new MultipleLinearRegressionExt(featureMatrix[i],
          this.regressionToSubset.getPredictor());
      mlr.doMultipleLinearRegressionExt();
      regressions[i] = mlr;
    }
    return regressions;
  }

  /*
   * Performs stepwise Forward Feature selection.
   * Returns the best MultipleLinearRegressionExt model.
   */
  public String forwardSelection() {
    /*
		 * HTML report header.
		 */

    this.orderFeaturesByThreshold();
    Feature[] orderedFeatures = this.getOrderedFeatures();
    int regressionCounter = 1;
    int numberIterations = orderedFeatures.length;

    this.htmlReportContent += "<div id=\"stepwiseforwardselection\">";
    this.htmlReportContent += "<h1>Forward Stepwise Variable Selection</h1>";

    for (int i = 0; i < numberIterations; i++) {
      Feature[][] featureCombination = this.buildCombinationOfFeatures(regressionCounter);
      MultipleLinearRegressionExt[] mlr = this.doRegressionsFromFeatureMatrix(featureCombination);

      if (this.thresholdVariable.equals("RSS")) {
        this.orderRssMultipleRegressionAsc(mlr);
      } else {
        this.orderAdjRSquaredMultipleRegressionDesc(mlr);
      }
			
			/*
			 * HTML report. Prints the content of the mlr
			 * array to the output html file.
			 */
      this.htmlReportContent += "<div id=\"iteration" + regressionCounter + "\">";
      this.htmlReportContent += "<h2>Variable Selection for " + regressionCounter
          + " Variables</h2>";
      this.htmlReportContent
          += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\"><tr><th>Variable</th>";
      if (this.thresholdVariable.equals("RSS")) {
        this.htmlReportContent += "<th>RSS</th></tr>";
      } else {
        this.htmlReportContent += "<th>Adjusted R-Squared</th></tr>";
      }
      for (int j = 0; j < mlr.length; j++) {
        String featureName = "";

        for (int k = 1; k < mlr[j].getFeatures().length; k++) {
          if (k != mlr[j].getFeatures().length - 1) {
            featureName += mlr[j].getFeatures()[k].getName() + " + ";
          } else {
            featureName += mlr[j].getFeatures()[k].getName();
          }
        }

        this.htmlReportContent += "<tr><td style=\"border:1px solid black;\">" + featureName
            + "</td>";
        if (this.thresholdVariable.equals("RSS")) {
          this.htmlReportContent += "<td style=\"border:1px solid black;\">" + ExporterBean
              .roundTo2Decimals(mlr[j].getRss()) + "</td></tr>";
        } else {
          this.htmlReportContent += "<td style=\"border:1px solid black;\">" + ExporterBean
              .roundTo2Decimals(mlr[j].getAdjustedRSquared()) + "</td></tr>";
        }
      }

      this.htmlReportContent += "</table></div>";

      this.regressions[i] = mlr[0];
      regressionCounter++;
    }

    this.htmlReportContent += "</div>";

    this.orderAdjRSquaredMultipleRegressionDesc(this.regressions);
		
		/* 
		 * HTML final model div.
		 */
    this.htmlReportContent += "<div id=\"finalModel\">";
    this.htmlReportContent += "<h1>Best Model found</h1>";
    this.htmlReportContent
        += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\"><tr><th>Features</th><th>RSS</th><th>Adjusted R-Squared</th></tr>";

    String name = "";
    for (int i = 1; i < this.regressions[0].getFeatures().length; i++) {
      if (i != this.regressions[0].getFeatures().length - 1) {
        name += this.regressions[0].getFeatures()[i].getName() + " + ";
      } else {
        name += this.regressions[0].getFeatures()[i].getName();
      }
    }

    this.htmlReportContent += "<tr><td style=\"border:1px solid black;\">" + name
        + "</td><td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.regressions[0].getRss()) + "</td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.regressions[0].getAdjustedRSquared()) + "</td></tr>";
    this.htmlReportContent += "</table></div>";
		
		/*
		 * Writes HTML content to file passed as parameter.
		 */

    return this.htmlReportContent;
  }

  /*
   * Performs Backward Feature Selection.
   * Returns the best MultipleLinearRegressionExt model.
   */
  public String backwardSelection() {
		/*
		 * HTML report header.
		 */

    this.orderFeaturesByThreshold();
    Feature[] orderedFeatures = this.getOrderedFeatures();
    int regressionCounter = orderedFeatures.length;
    int numberIterations = 0;

    this.htmlReportContent += "<div id=\"stepwiseforwardselection\">";
    this.htmlReportContent += "<h1>Backward Stepwise Variable Selection</h1>";

    for (int i = regressionCounter; i > numberIterations; i--) {
      Feature[][] featureCombination = this.buildCombinationOfFeatures(regressionCounter);
      MultipleLinearRegressionExt[] mlr = this.doRegressionsFromFeatureMatrix(featureCombination);

      if (this.thresholdVariable.equals("RSS")) {
        this.orderRssMultipleRegressionAsc(mlr);
      } else {
        this.orderAdjRSquaredMultipleRegressionDesc(mlr);
      }
			
			/*
			 * HTML report. Prints the content of the mlr
			 * array to the output html file.
			 */
      this.htmlReportContent += "<div id=\"iteration" + regressionCounter + "\">";
      this.htmlReportContent += "<h2>Variable Selection for " + regressionCounter
          + " Variable</h2>";
      this.htmlReportContent
          += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\"><tr><th>Variable</th>";
      if (this.thresholdVariable.equals("RSS")) {
        this.htmlReportContent += "<th>RSS</th></tr>";
      } else {
        this.htmlReportContent += "<th>Adjusted R-Squared</th></tr>";
      }
      for (int j = 0; j < mlr.length; j++) {
        String featureName = "";

        for (int k = 1; k < mlr[j].getFeatures().length; k++) {
          if (k != mlr[j].getFeatures().length - 1) {
            featureName += mlr[j].getFeatures()[k].getName() + " + ";
          } else {
            featureName += mlr[j].getFeatures()[k].getName();
          }
        }

        this.htmlReportContent += "<tr><td style=\"border:1px solid black;\">" + featureName
            + "</td>";
        if (this.thresholdVariable.equals("RSS")) {
          this.htmlReportContent += "<td style=\"border:1px solid black;\">" + ExporterBean
              .roundTo2Decimals(mlr[j].getRss()) + "</td></tr>";
        } else {
          this.htmlReportContent += "<td style=\"border:1px solid black;\">" + ExporterBean
              .roundTo2Decimals(mlr[j].getAdjustedRSquared()) + "</td></tr>";
        }
      }

      this.htmlReportContent += "</table></div>";

      this.regressions[i - 1] = mlr[0];
      regressionCounter--;
    }

    this.htmlReportContent += "</div>";

    //for the final model, it always orders according to the Adjusted R-Squared value.
    this.orderAdjRSquaredMultipleRegressionDesc(this.regressions);
		
		/* 
		 * HTML final model div.
		 */
    this.htmlReportContent += "<div id=\"finalModel\">";
    this.htmlReportContent += "<h1>Best Model found</h1>";
    this.htmlReportContent
        += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\"><tr><th>Variables</th><th>RSS</th><th>Adjusted R-Squared</th></tr>";

    String name = "";
    for (int i = 1; i < this.regressions[0].getFeatures().length; i++) {
      if (i != this.regressions[0].getFeatures().length - 1) {
        name += this.regressions[0].getFeatures()[i].getName() + " + ";
      } else {
        name += this.regressions[0].getFeatures()[i].getName();
      }
    }

    this.htmlReportContent += "<tr><td style=\"border:1px solid black;\">" + name
        + "</td><td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.regressions[0].getRss()) + "</td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.regressions[0].getAdjustedRSquared()) + "</td></tr>";
    this.htmlReportContent += "</table></div>";

    return this.htmlReportContent;
  }

  /*
   * Prints the parameter String to a file.
   */
  public void doHTMLReport(String name)
      throws IOException {
    BufferedWriter bw = new BufferedWriter(new FileWriter(this.regressionToSubset.getPath() +
        this.regressionToSubset.getReadingFileName() + " - " + name + ".html"));
    bw.write(this.htmlReportContent);
    bw.close();
  }
}