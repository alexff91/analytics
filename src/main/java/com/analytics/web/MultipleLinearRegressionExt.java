package com.analytics.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import Jama.Matrix;
import multipleLinearRegression.Dependent;
import multipleLinearRegression.Feature;
import multipleLinearRegression.MultipleLinearRegression;

public class MultipleLinearRegressionExt extends MultipleLinearRegression {
  private int numberFeatures;

  private int numberDataPoints;

  private Feature[] features;

  private Dependent dependent;

  private double rss; //Residual Sum of Squares

  private double ess; //Error Sum of Squares

  private double tss; //Total Sum of Squares

  private double rsquared; //coefficient of determination (R²)

  private double adjustedRSquared; //Adjusted R²

  private double fstatistic; //F-Statistic

  private double residualStandardError;

  private double estimatorBias; //bias of the calculated estimates.

  private double unbiasedVariance;

  private double[] tStatistics;
      //vector that has the t-statistic values for each one of the coefficients.

  private double[] zScores; //vector that has the z-scores for each one of the coefficients.

  private Feature[] interactions; //array that contains all the create interactions.

  private double[][] slopeVector; //vector of slopes and intercepts

  private double[][] errorVector; //vector of the errors

  private double[][] unitVector;

  private double[][] unitMatrix;

  private double[][] residualsMatrix;

  private double[][] standardErrorMatrix;

  private double[][] correlationsMatrix; //matrix with the correlations between features.

  private double[][] pValuesFeatures;
      //matrix that contains the p-Values for each one of the features.

  //Messages HashMaps
  HashMap<String, String> logMessages;

  HashMap<String, String> errorMessages;

  //Files
  private String parentDir = new File("").getAbsolutePath();

  private String folderPath = parentDir + "/tests/datasets/";

  private String writerPath = parentDir + "/tests/results/";

  private String readingFileName;

  //Computational Time measurements
  private double readingDatasetTime;

  private double regressionCalculationsTime;

  private double writingToFileTime;

  public int getNumberFeatures() {
    return numberFeatures;
  }

  public void setNumberFeatures(int numberFeatures) {
    this.numberFeatures = numberFeatures;
  }

  public int getNumberDataPoints() {
    return numberDataPoints;
  }

  public void setNumberDataPoints(int numberDataPoints) {
    this.numberDataPoints = numberDataPoints;
  }

  public Feature[] getFeatures() {
    return features;
  }

  public void setFeatures(Feature[] features) {
    this.features = new Feature[features.length];
    for (int i = 0; i < features.length; i++) {
      this.features[i] = features[i];
    }
  }

  public Dependent getPredictor() {
    return dependent;
  }

  public void setPredictor(Dependent dependent) {
    this.dependent = dependent;
  }

  public Feature[] getInteractions() {
    return interactions;
  }

  public void setInteractions(Feature[] interactions) {
    this.interactions = interactions;
  }

  public double[][] getSlopeVector() {
    return slopeVector;
  }

  public void setSlopeVector(double[][] sV) {
    this.slopeVector = sV;
  }

  public double getRss() {
    return rss;
  }

  public void setRss(double rss) {
    this.rss = rss;
  }

  public double getEss() {
    return ess;
  }

  public void setEss(double ess) {
    this.ess = ess;
  }

  public double getTss() {
    return tss;
  }

  public void setTss(double tss) {
    this.tss = tss;
  }

  public double getRsquared() {
    return rsquared;
  }

  public void setRsquared(double rsquared) {
    this.rsquared = rsquared;
  }

  public double getAdjustedRSquared() {
    return adjustedRSquared;
  }

  public void setAdjustedRSquared(double adjustedRSquared) {
    this.adjustedRSquared = adjustedRSquared;
  }

  public double getFstatistic() {
    return fstatistic;
  }

  public void setFstatistic(double fstatistic) {
    this.fstatistic = fstatistic;
  }

  public double getResidualStandardError() {
    return residualStandardError;
  }

  public void setResidualStandardError(double errorStandardDev) {
    this.residualStandardError = errorStandardDev;
  }

  public double getReadingDatasetTime() {
    return readingDatasetTime;
  }

  public void setReadingDatasetTime(double readingDatasetTime) {
    this.readingDatasetTime = readingDatasetTime;
  }

  public double getRegressionCalculationsTime() {
    return regressionCalculationsTime;
  }

  public void setRegressionCalculationsTime(double regressionCalculationsTime) {
    this.regressionCalculationsTime = regressionCalculationsTime;
  }

  public double getWritingToFileTime() {
    return writingToFileTime;
  }

  public void setWritingToFileTime(double writingToFileTime) {
    this.writingToFileTime = writingToFileTime;
  }

  public String getReadingFileName() {
    return readingFileName;
  }

  public void setReadingFileName(String readingFileName) {
    this.readingFileName = readingFileName;
  }

  public String getPath() {
    return this.writerPath;
  }

  public void setPath(String s) {
    this.writerPath = s;
  }

  public double[][] getResidualsMatrix() {
    return residualsMatrix;
  }

  public void setResidualsMatrix(double[][] residualsMatrix) {
    this.residualsMatrix = residualsMatrix;
  }

  public double[][] getStandardErrorMatrix() {
    return standardErrorMatrix;
  }

  public void setStandardErrorMatrix(double[][] standardErrorMatrix) {
    this.standardErrorMatrix = standardErrorMatrix;
  }

  public double[][] getpValuesFeatures() {
    return pValuesFeatures;
  }

  public void setpValuesFeatures(double[][] pValuesFeatures) {
    this.pValuesFeatures = pValuesFeatures;
  }

  public double getEstimatorBias() {
    return estimatorBias;
  }

  public void setEstimatorBias(double estimatorBias) {
    this.estimatorBias = estimatorBias;
  }

  public double[] getzScores() {
    return zScores;
  }

  public void setzScores(double[] zScores) {
    this.zScores = zScores;
  }

  public double[][] getCorrelationsMatrix() {
    return correlationsMatrix;
  }

  public void setCorrelationsMatrix(double[][] correlationsMatrix) {
    this.correlationsMatrix = correlationsMatrix;
  }

  /*
   *
   */
  public MultipleLinearRegressionExt(String file)
      throws BiffException, IOException {
    super(file);
    String extension = file.substring(file.lastIndexOf(".") + 1, file.length());

    if (extension.equals("xls")) {
      //initializes computational time logging.
      long start = System.nanoTime();

      boolean keepGoingDown = true;
      boolean keepGoingRight = true;
      int nDataPoints = 0;
      int nFeatures = 0;
      double currentValue = 0;
      //FileInputStream fileStream = new FileInputStream(this.folderPath + file);
      Workbook workbook = Workbook.getWorkbook(new File(this.folderPath + file));
      this.readingFileName = file.substring(0, file.lastIndexOf("."));
      Sheet sheet = workbook.getSheet(0);
      Cell yNameCell = sheet.getCell(0, 0);
      String predictorName = yNameCell.getContents();
      if (predictorName.equals("y")) {

				/*
                 * first step, it will count the number of elements in the y vector.
				 */
        while (keepGoingDown == true) {
          Cell yValueCell;

          try {
            yValueCell = sheet.getCell(0, nDataPoints + 1);
          } catch (Exception ArrayIndexOutOfBoundsException) {
            keepGoingDown = false;
            break;
          }

          if (yValueCell.getType() == CellType.NUMBER) {
            nDataPoints++;
          }
        }
        this.numberDataPoints = nDataPoints;

				/*
         * second step, it'll count the number of features available in the dataset.
				 */
        while (keepGoingRight == true) {
          Cell featureNameCell;

          try {
            featureNameCell = sheet.getCell(nFeatures + 1, 0);
          } catch (Exception ArrayOutOfBoundsException) {
            keepGoingRight = false;
            break;
          }

          if (featureNameCell.getType() == CellType.LABEL) {
            nFeatures++;
          }
        }
        this.numberFeatures = nFeatures;

				/*
				 * third step, it'll go all the way down again and allocate those elements
				 * in the y vector.
				 */
        double[] y = new double[nDataPoints];
        nDataPoints = 0;
        while (nDataPoints < y.length) {
          Cell yValueCell;

          try {
            yValueCell = sheet.getCell(0, nDataPoints + 1);
          } catch (Exception ArrayOutOfBoundsException) {
            break;
          }

          if (yValueCell.getType() == CellType.NUMBER) {
            NumberCell nc = (NumberCell) yValueCell;
            currentValue = nc.getValue();
            y[nDataPoints] = currentValue;
            nDataPoints++;
          }
        }

				/*
				 * fourth step, creates the Predictor object with the data filled array.
				 */
        Dependent dependent = new Dependent(y);
        this.dependent = dependent;

				/*
				 * fifth step, it'll create a Feature object with the respective values
				 * for each one of the features available in the dataset.
				 */
        this.features = new Feature[nFeatures];
        this.features = this.addUnitVector(this.features, nDataPoints);
        int featureCounter = 1;

        for (int i = 0; i < nFeatures; i++) {
          Cell fNameIterator = sheet.getCell(i + 1, 0);
          String featureName = fNameIterator.getContents();
          double[] dataValues = new double[nDataPoints];
          for (int j = 0; j < nDataPoints; j++) {
            Cell currentCell = sheet.getCell(i + 1, j + 1);
            if (currentCell.getType() == CellType.NUMBER) {
              NumberCell nc = (NumberCell) currentCell;
              currentValue = nc.getValue();
              dataValues[j] = currentValue;
            }
          }
          Feature f = new Feature(i + 1, featureName, dataValues);
          this.features[featureCounter] = f;

          featureCounter++;
        }

				/*
				 * sixth, initializes the rest of the stuff.
				 */
        this.errorVector = new double[this.features[0].getDataValues().length][1];
        this.setSlopeVector(new double[this.features.length][1]);

        this.unitVector = new double[features[0].getDataValues().length][1];
        for (int i = 0; i < this.unitVector.length; i++) {
          unitVector[i][0] = 1;
        }
        unitMatrix = unitVector;

        workbook.close();

        double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
        this.readingDatasetTime = elapsedTimeInSec;
      } else {
        return;
      }
    }
  }

  /*
   *
   */
  public MultipleLinearRegressionExt(Feature[] features, Dependent dependent) {
    super(features, dependent);
    //initializes computational time logging.
    long start = System.nanoTime();

    this.dependent = dependent;
    this.numberDataPoints = features[0].getDataValues().length;
    this.numberFeatures = features.length;
    if (this.features == null) {
      this.features = new Feature[features.length + 1];
    } else {
      this.features = new Feature[this.features.length];
    }
    this.features = this.addUnitVector(features, dependent.getDataValues().length);

    for (int i = 1; i < this.features.length; i++) {
      this.features[i] = new Feature(features[i - 1].getId(),
          features[i - 1].getName(),
          features[i - 1].getDataValues());
    }

    this.errorVector = new double[features[0].getDataValues().length][1];
    this.setSlopeVector(new double[this.features.length][1]);

    this.unitVector = new double[features[0].getDataValues().length][1];
    for (int i = 0; i < this.unitVector.length; i++) {
      unitVector[i][0] = 1;
    }
    unitMatrix = unitVector;

    double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
    this.regressionCalculationsTime = elapsedTimeInSec;
  }

  /*
   * Adds the unit vector to the Features array,
   * in order for the multiple linear regression algorithm
   * to function properly.
   */
  public Feature[] addUnitVector(Feature[] features, int nDataPoints) {
    Feature[] f, copyF;
    f = features;
    copyF = new Feature[f.length + 1];
    double[] units = new double[nDataPoints];

    for (int i = 0; i < units.length; i++) {
      units[i] = 1;
    }

    Feature unitVector = new Feature(0, "", units);
    copyF[0] = unitVector;
    for (int i = 0; i < f.length; i++) {
      copyF[i + 1] = f[i];
    }
    return copyF;
  }

  /*
   * Adds one Feature to the features' attribute array.
   */
  public void addFeature(Feature f) {
    Feature[] tempFeatures = new Feature[this.features.length + 1];
    for (int i = 0; i < this.features.length; i++) {
      tempFeatures[i] = this.features[i];
    }
    tempFeatures[tempFeatures.length - 1] = f;

    this.setFeatures(tempFeatures);
  }

  /*
   * Adds all the elements of a Feature array passed as parameter
   * to the features' attribute array.
   */
  public void addFeatures(Feature[] f) {
    Feature[] tempFeatures = new Feature[this.features.length + f.length - 1];
    int counter = 0, index = 0;

    //starts with index 1 in order to remove the array filled with 1s.
    for (int i = 1; i < this.features.length; i++) {
      tempFeatures[i - 1] = this.features[i];
      counter++;
      index++;
    }
    for (int i = 0; i < tempFeatures.length - counter; i++) {
      tempFeatures[index] = f[i];
      index++;
    }
    this.setFeatures(tempFeatures);
  }

  /*
   * Removes one Feature of the features' attribute array.
   * The parameter is the Feature id.
   */
  public void removeFeature(int id) {
    Feature[] tempFeatures = new Feature[this.features.length - 1];
    Feature f = null;
    int index = 0;
    for (int i = 0; i < this.features.length; i++) {
      if (this.features[i].getId() == id) {
        f = this.features[i];
      } else {
        continue;
      }
    }

    if (f == null) {
      return;
    } else {
      for (int i = 0; i < this.features.length; i++) {
        if (this.features[i].getId() == f.getId()) {
          index = i;
          continue;
        } else {
          tempFeatures[index] = this.features[i];
          index++;
        }
      }
    }
    this.setFeatures(tempFeatures);
    System.out.println();
  }

  /*
   * Prints the array of the slopes and intercept.
   */
  public void printSlopeVector() {
    String debugPrinter = "Slope vector: \n [";
    for (int i = 0; i < this.slopeVector.length; i++) {
      if (i == this.slopeVector.length - 1) {
        debugPrinter += slopeVector[i][0];
      } else {
        debugPrinter += slopeVector[i][0] + ", ";
      }
    }
    debugPrinter += "]\n";
    System.out.println(debugPrinter);
  }

  /*
   * Prints the array of obtained errors.
   */
  public void printErrorVector() {
    String debugPrinter = "Error vector: \n [";
    for (int i = 0; i < this.errorVector.length; i++) {
      if (i == this.errorVector.length - 1) {
        debugPrinter += errorVector[i][0];
      } else {
        debugPrinter += errorVector[i][0] + ", ";
      }
    }
    debugPrinter += "]\n";
    System.out.println(debugPrinter);
  }

  /*
   * Prints results to an html file, as a report.
   */
  public String doRegressionHTMLReportExt()
      throws IOException {
    //initializes computational time measurement
    long start = System.nanoTime();
    String signal = " + ";
    double signalValue = 1;
    double meanErrorVector = 0;
    double varianceErrorVector = 0;

    //        BufferedWriter bw = new BufferedWriter(new FileWriter(this.writerPath + this.readingFileName + ".html"));
    String content = "";
		/*
		 * writes the model information to file.
		 */
    content += "<div id=\"modelInfo\">";
    content += "<p>";
    content += "<h1> Variables</h1>";
    for (int i = 1; i < this.features.length; i++) {
      content += "x" + i + " : " + this.features[i].getName() + "<br />";
    }
    content += "</p>";

    content += "<h1>Model Obtained</h1>"
        + "<p>y = " + this.slopeVector[0][0];
    for (int i = 1; i < this.features.length; i++) {
      if (i != this.features.length - 1) {
        if (this.slopeVector[i][0] < 0) {
          signal = " - ";
          signalValue = -1;
        } else {
          signal = " + ";
          signalValue = 1;
        }
        content += signal + signalValue * this.slopeVector[i][0] + " * " + "x" + i;
      } else {
        if (this.slopeVector[i][0] < 0) {
          signal = " - ";
          signalValue = -1;
        } else {
          signal = " + ";
          signalValue = 1;
        }
        content += signal + signalValue * this.slopeVector[i][0] + " * " + "x" + i;
      }
    }
    content += "</p></div>";

		/*
		 * writes the dataset information table to the file.
		 */
    content += "<div id=\"datasetTable\" >"
        + "<p><h1>Dataset Information </h1></p>"
        + "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\">"
        + "<tr><th>Entry ID </th>"
        + "<th>y</th>";
    for (int i = 0; i < this.features.length; i++) {
      content += "<th>" + this.features[i].getName() + "</th>";
    }
    content += "<th>Training Error</th>";
    content += "</tr>";

    //counts again the number of elements.
    int numberElementsPerFeature = 0;
    for (int i = 0; i < this.features[0].getDataValues().length; i++) {
      numberElementsPerFeature++;
    }

    //        for (int i = 0; i < numberElementsPerFeature; i++) {
    //            content += "<tr>";
    //            content += "<td style=\"border:1px solid black;\">" + (i + 1) + "</td>";
    //            content += "<td style=\"border:1px solid black;\">" + this.dependent.getDataValues()[i] + "</td>";
    //            for (int j = 0; j < this.features.length; j++) {
    //                content += "<td style=\"border:1px solid black;\">" + this.features[j].getDataValues()[i] + "</td>";
    //            }
    //            content += "<td style=\"border:1px solid black;\">" + this.errorVector[i][0] + "</td>";
    //            meanErrorVector += Math.abs(this.errorVector[i][0]);
    //            content += "</tr>";
    //        }
    //        meanErrorVector = meanErrorVector / (double) this.errorVector.length;
    //
    //        for (int i = 0; i < numberElementsPerFeature; i++) {
    //            varianceErrorVector += Math.pow(this.errorVector[i][0] - meanErrorVector, 2);
    //        }
    //        varianceErrorVector = varianceErrorVector / (double) this.errorVector.length;

    //Means Row
    content +=
        "<tr><td style=\"border:1px solid black;\"><b>Mean</b></td><td style=\"border:1px solid black;\">"
            + ExporterBean.roundTo2Decimals(this.dependent.getMean()) + "</td>";
    for (int i = 0; i < this.features.length; i++) {
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(this.features[i].getMean()) + "</td>";
    }
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(meanErrorVector) + "</td></tr>";

    //Variances Row
    content +=
        "<tr><td style=\"border:1px solid black;\"><b>Variance</b></td><td style=\"border:1px solid black;\">"
            + ExporterBean.roundTo2Decimals(this.dependent.getVariance()) + "</td>";
    for (int i = 0; i < this.features.length; i++) {
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(this.features[i].getVariance()) + "</td>";
    }
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(varianceErrorVector) + "</td></tr>";

    //Standard Deviation Row
    content +=
        "<tr><td style=\"border:1px solid black;\"><b>Standard Deviation</b></td><td style=\"border:1px solid black;\">"
            + ExporterBean.roundTo2Decimals(this.dependent.getStandardDev()) + "</td>";
    for (int i = 0; i < this.features.length; i++) {
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(this.features[i].getStandardDev()) + "</td>";
    }
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(Math.sqrt(varianceErrorVector)) + "</td>";
    content += "</table></div>";

		/*
		 * writes the information about the coefficients.
		 */
    content += "<div id=\"coefficientsTable\">";
    content += "<h1>Coefficients Summary</h1>";
    content +=
        "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\"><tr><td ></td><th>Coefficient</th>"
            + "<th>Std. Error</th>"
            + "<th>t-value</th>"
            + "<th>Z-Score</th>"
            + "<th>p-value Interval</th></tr>";
    content += "<tr><td style=\"border:1px solid black;\"><b>Intercept</b></td>";
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.slopeVector[0][0]) + "</td>";
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(Math.sqrt(this.standardErrorMatrix[0][0])) + "</td>";
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.tStatistics[0]) + "</td>";
    content += "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.zScores[0]) + "</td>";
    content += "<td style=\"border:1px solid black;\">[ " + ExporterBean
        .roundTo2Decimals(this.pValuesFeatures[0][0]) + ", " + ExporterBean
        .roundTo2Decimals(this.pValuesFeatures[0][1]) + " ]</td></tr>";
    for (int i = 0; i < this.numberFeatures; i++) {
      content += "<tr><td style=\"border:1px solid black;\"><b>" + this.features[i + 1].getName()
          + "</b></td>";
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(this.slopeVector[i + 1][0]) + "</td>";
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(Math.sqrt(this.standardErrorMatrix[i + 1][i + 1])) + "</td>";
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(this.tStatistics[i + 1]) + "</td>";
      content += "<td style=\"border:1px solid black;\">" + ExporterBean
          .roundTo2Decimals(this.zScores[i + 1]) + "</td>";
      content += "<td style=\"border:1px solid black;\">[ " + ExporterBean
          .roundTo2Decimals(this.pValuesFeatures[i + 1][0]) + ", " + ExporterBean
          .roundTo2Decimals(this.pValuesFeatures[i + 1][1]) + " ]</td></tr>";
    }
    content += "</table></div>";

		/*
		 * writes the correlations table.
		 */
    content += "<div id=\"correlations\">"
        + "<h1>Correlations between Variables</h1>";
    content
        += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\">";
    content += "<tr><td></td>";
    for (int i = 1; i < this.features.length; i++) {
      content += "<th>" + this.features[i].getName() + "</th>";
    }
    content += "</tr>";
    for (int i = 1; i < this.features.length; i++) {
      content += "<tr><td style=\"border:1px solid black;\"><b>" + this.features[i].getName()
          + "</b></td>";
      for (int j = 0; j < this.features.length - 1; j++) {
        content += "<td style=\"border:1px solid black;\">" + ExporterBean
            .roundTo2Decimals(this.correlationsMatrix[i - 1][j]) + "</td>";
      }
      content += "</td>";
    }
    content += "</table></div>";

		/*
		 * writes the statistical learning variables to a table.
		 */
    content += "<div id=\"statistics\">"
        + "<h1>Statistics</h1>"

    ;
    content
        += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\">";
    content += "<tr><td style=\"border:1px solid black;\"><b>Degrees of Freedom</b></td>"
        + "<td style=\"border:1px solid black;\">" + (this.numberDataPoints - this.numberFeatures
        - 1) + "</td>" + "</tr>"
        + "<td style=\"border:1px solid black;\"><b>Number of Elements</b></td>"
        + "<td style=\"border:1px solid black;\">" + this.features[0].getDataValues().length
        + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>RSS</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean.roundTo2Decimals(this.getRss())
        + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>ESS</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean.roundTo2Decimals(this.getEss())
        + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>TSS</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean.roundTo2Decimals(this.getTss())
        + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>Bias</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.getEstimatorBias()) + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>Variance</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.unbiasedVariance) + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>R-squared</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.getRsquared()) + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>Adjusted R-squared</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.getAdjustedRSquared()) + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>F-statistic</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.getFstatistic()) + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>RSE</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.getResidualStandardError()) + "</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>Reading Time</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.readingDatasetTime) + " seconds</td>" + "</tr>"
        + "<tr><td style=\"border:1px solid black;\"><b>Execution Time</b></td>"
        + "<td style=\"border:1px solid black;\">" + ExporterBean
        .roundTo2Decimals(this.regressionCalculationsTime) + " seconds</td>" + "</tr>";
    content += "</table></div>";

    return content;
  }

  /*
   * Computes the correlation between features and
   * returns a matrix with the correlation values.
   */
  public double[][] computeCorrelations() {
    double[][] correlationsMatrix = new double[this.numberFeatures + 1][this.numberFeatures + 1];
    double sxx = 0, syy = 0, sxy = 0, sigma = 0;
    double sumX, sumSquareX, sumY, sumSquareY, sumXY;
    for (int k = 1; k < this.numberFeatures + 1; k++) {
      for (int i = 1; i < this.numberFeatures + 1; i++) {
        sumX = 0;
        sumSquareX = 0;
        sumY = 0;
        sumSquareY = 0;
        sumXY = 0;
        for (int j = 0; j < this.numberDataPoints; j++) {
          sumX += this.features[k].getDataValues()[j];
          sumSquareX += Math.pow(this.features[k].getDataValues()[j], 2);
          sumY += this.features[i].getDataValues()[j];
          sumSquareY += Math.pow(this.features[i].getDataValues()[j], 2);
          sumXY += this.features[k].getDataValues()[j] * this.features[i].getDataValues()[j];
        }
        sxx = sumSquareX - (Math.pow(sumX, 2) / (double) this.numberDataPoints);
        syy = sumSquareY - (Math.pow(sumY, 2) / (double) this.numberDataPoints);
        sxy = sumXY - ((sumX * sumY) / (double) this.numberDataPoints);
        sigma = sxy / (Math.sqrt(sxx * syy));
        correlationsMatrix[k - 1][i - 1] = sigma;
      }
    }
    return correlationsMatrix;
  }

  /*
   * Does the multiple linear regression process and calculates
   * also all the necessary statistics so the user can infer
   * his/her own conclusions about his/her model.
   */
  public String doMultipleLinearRegressionExt() {
    //initializes computational time logging.
    long start = System.nanoTime();

    double[][] tempFeatures = new double[this.features.length][this.features[0]
        .getDataValues().length];

    for (int i = 0; i < tempFeatures.length; i++) {
      for (int j = 0; j < tempFeatures[0].length; j++) {
        tempFeatures[i][j] = this.features[i].getDataValues()[j];
      }
    }

		/*
		 * Calculates the vector b with the coefficients of
		 * the final multiple regression model.
		 */
    Matrix a = new Matrix(tempFeatures);
    Matrix aT = a.transpose();
    Matrix y = new Matrix(this.dependent.getDataValues(), this.dependent.getDataValues().length);

    Matrix partOne = (a.times(aT));
    partOne = partOne.inverse();

    Matrix partTwo = (a.times(y));

    a = partOne.times(partTwo);

    this.setSlopeVector(a.getArray());
    this.printSlopeVector();

		/*
		 * Calculates the errors and stores the values
		 * in the errorVector. The erroVector 'e' is calculated
		 * as follows:
		 *
		 * e = y - (x^T * b)
		 */
    Matrix betas = new Matrix(this.slopeVector);
    Matrix values = new Matrix(tempFeatures);
    Matrix errors = values.transpose().times(betas);
    errors = y.minus(errors);
    this.errorVector = errors.getArray();
    this.printErrorVector();

		/*
		 * Computes the correlations between features and
		 * stores each one of the correlation values in the
		 * correlationsMatrix attribute.
		 */
    this.correlationsMatrix = new double[this.numberFeatures][this.numberFeatures];
    this.correlationsMatrix = this.computeCorrelations();

		/*
		 * Calculates the RSS value:
		 * RSS = b^T * X^T * Y - (1/n)*(Y^T * U * U^T * Y)
		 */
    double n = this.getFeatures()[0].getDataValues().length;
    System.out.println("N: " + n);
    Matrix transposeModel = new Matrix(this.getSlopeVector());
    transposeModel = transposeModel.transpose();
    a = new Matrix(tempFeatures);
    Matrix temporaryLeft = transposeModel.times(a);
    temporaryLeft = temporaryLeft.times(y);
    Matrix temporaryRight = y.transpose();
    temporaryRight = temporaryRight.times(new Matrix(this.unitMatrix));
    temporaryRight = temporaryRight.times(new Matrix(this.unitMatrix).transpose());
    temporaryRight = temporaryRight.times(y);
    this.rss = temporaryLeft.get(0, 0) - (1 / n) * temporaryRight.get(0, 0);
    System.out.println("RSS = " + this.rss);

		/*
		 * Calculates the ESS value:
		 * ESS = Y^T * Y - b^T * X^T * Y
		 */
    temporaryLeft = y.transpose();
    temporaryLeft = temporaryLeft.times(y);
    temporaryRight = transposeModel.times(a);
    temporaryRight = temporaryRight.times(y);
    this.ess = temporaryLeft.get(0, 0) - temporaryRight.get(0, 0);
    System.out.println("ESS = " + this.ess);

		/*
		 * Calculates the TSS value:
		 * TSS = Y^T * Y - (1/n)*(Y^T * U * U^T * Y)
		 */
    temporaryRight = y.transpose();
    temporaryRight = temporaryRight.times(new Matrix(this.unitMatrix));
    temporaryRight = temporaryRight.times(new Matrix(this.unitMatrix).transpose());
    temporaryRight = temporaryRight.times(y);
    this.tss = temporaryLeft.get(0, 0) - (1 / n) * temporaryRight.get(0, 0);
    System.out.println("TSS = " + this.tss);

		/*
		 * Calculates the R² value.
		 * R² = RSS/TSS
		 */
    this.rsquared = this.rss / this.tss;
    System.out.println("\nR² = " + this.rsquared);

		/*
		 * Calculates the Adjusted R² value.
		 * Adjusted R² = R² - (1 - R²)*[p/(n-p-1)],
		 * where p is the number of coefficients (except the intercept)
		 * and n is the number of data points.
		 * Based on:
		 * Theil, Henri (1961). Economic Forecasts and Policy. Holland, Amsterdam: North.
		 */
    int denominator = this.numberDataPoints - (this.numberFeatures) - 1;
    this.adjustedRSquared = this.rsquared - (1 - this.rsquared) * (((double) this.numberFeatures)
        / (double) denominator);
    System.out.println("\nAdjusted R² = " + this.adjustedRSquared);

		/*
		 * Calculates the F-Statistic value.
		 * F = (RSS / k) / [ ESS / (n-k-1)]
		 */
    int k = this.getFeatures().length - 1;
    double left = this.rss / k;
    double right = (this.ess / (double) (n - k - 1));
    this.fstatistic = left / right;
    System.out.println("\nF = " + this.fstatistic);

    double elapsedTimeInSec = (System.nanoTime() - start) * 1.0e-9;
    this.regressionCalculationsTime = elapsedTimeInSec;

		/*
		 * Calculates the residuals matrix.
		 * r = Y - Ŷ = (I - X(X'X)^(-1)X')Y
		 */
    Matrix I = Matrix.identity(this.numberDataPoints, this.numberDataPoints);
    Matrix X = new Matrix(tempFeatures);
    Matrix finalMatrix = X.times(X.transpose());
    finalMatrix = finalMatrix.inverse();
    finalMatrix = X.transpose().times(finalMatrix);
    finalMatrix = finalMatrix.times(X);
    finalMatrix = I.minus(finalMatrix);
    finalMatrix = finalMatrix.times(y);
    this.residualsMatrix = finalMatrix.getArray();

		/*
		 * Calculates the Error Standard Deviation.
		 * theta = SQRT(sum(residualMatrix²)/(n-p-1))
		 */
    double sumSquaredResiduals = 0;
    double[][] residuals = this.residualsMatrix;
    for (int i = 0; i < residuals.length; i++) {
      sumSquaredResiduals += Math.pow(residuals[i][0], 2);
    }
    sumSquaredResiduals = sumSquaredResiduals / (double) (this.numberDataPoints
        - this.numberFeatures - 1);
    sumSquaredResiduals = Math.sqrt(sumSquaredResiduals);
    this.residualStandardError = sumSquaredResiduals;
    System.out.println("\nRSE = " + this.residualStandardError);

		/*
		 * Calculates the standard error matrix.
		 * theta²(X'X)⁽-¹⁾
		 */
    finalMatrix = X.times(X.transpose()).inverse();
    finalMatrix = finalMatrix.times(Math.pow(this.residualStandardError, 2));
    this.standardErrorMatrix = finalMatrix.getArray();

		/*
		 * Computes the Z-score.
		 * Z = b(j)/(var * sqrt(v_j)), where v_j is the diagonal element
		 * of (X'X)⁽-¹⁾.
		 */
    this.unbiasedVariance = 1 / (double) (this.numberDataPoints - this.numberFeatures - 1);
    double sumErrors = 0;
    for (int i = 0; i < this.errorVector.length; i++) {
      sumErrors += Math.pow(this.errorVector[i][0], 2);
    }
    this.unbiasedVariance = this.unbiasedVariance * (double) sumErrors;

    this.zScores = new double[slopeVector.length];
    for (int i = 0; i < this.slopeVector.length; i++) {
      this.zScores[i] = slopeVector[i][0] / (double) (this.unbiasedVariance * Math
          .sqrt(this.standardErrorMatrix[i][i]));
    }

		/*
		 * Does the t-statistic test for each one of the predictors
		 * in order to infer the p-value later.
		 * t(beta) = (beta-knownconstant)/s.e.(beta).
		 * The knownconstant will be 0 in this calculations, since
		 * it's not being calculated any hypothesis, just the t-statistic
		 * of the regressors.
		 */
    this.tStatistics = new double[slopeVector.length];
    for (int i = 0; i < slopeVector.length; i++) {
      this.tStatistics[i] = slopeVector[i][0] / (double) Math.sqrt(this.standardErrorMatrix[i][i]);
    }

		/*
		 * Calculates the p-Values for each one of the features.
		 */
    this.pValuesFeatures = new double[this.features.length][2];
    for (int i = 0; i < this.features.length; i++) {
      this.pValuesFeatures[i] = this.features[i].calculatePValueInterval(this.tStatistics[i],
          this.numberDataPoints - this.numberFeatures - 1);
    }

		/*
		 * Calculates the estimator bias.
		 * Bias = [ sum(Y')-sum(Y) ] / numberDataPoints;
		 */
    double sum = 0;
    double sumEstimates = 0;
    for (int i = 0; i < this.numberDataPoints; i++) {
      sum += this.dependent.getDataValues()[i];
    }
    betas = new Matrix(this.slopeVector);
    values = new Matrix(tempFeatures);
    Matrix estimates = values.transpose().times(betas);
    double[][] estimatesArr = estimates.getArray();
    for (int i = 0; i < estimatesArr.length; i++) {
      sumEstimates += estimatesArr[i][0];
    }
    this.estimatorBias = (sumEstimates - sum) / (double) this.numberDataPoints;
    System.out.println("Bias = " + this.estimatorBias);

		/*
		 * Writes to file the regression report.
		 */
    try {
      return this.doRegressionHTMLReportExt();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  /*
   * Creates one Interaction between 2 Features.
   */
  public Feature createInteraction(Feature f1, Feature f2) {
    if (f1.getDataValues().length != f2.getDataValues().length) {
      return null;
    }

    double[] newValues = new double[f1.getDataValues().length];
    for (int i = 0; i < f1.getDataValues().length; i++) {
      newValues[i] = f1.getDataValues()[i] * f2.getDataValues()[i];
    }

    Feature interaction = new Feature(f1.getId() * 10 + f2.getId() * 10,
        f1.getName() + " * " + f2.getName(),
        newValues);
    return interaction;
  }

  /*
   * Verifies if f1 and f2 have the same name (meaning, are the same Feature). If so,
   * returns false. In case they don't have the same name, it iterates through the set
   * in order to check if the name of the new interaction exists already in the featureSet.
   * In case it does, it returns false. In case it hasn't returned until this point,
   * it means that the interaction is valid, hence it returns true.
   */
  private boolean validateInteraction(Feature f1, Feature f2, LinkedHashSet<Feature> set) {
    if (f1.getName().equals(f2.getName())) {
      return false;
    } else {
      String name = f1.getName() + " * " + f2.getName();
      String mirroredName = f2.getName() + " * " + f1.getName();
      Iterator<Feature> it = set.iterator();
      while (it.hasNext()) {
        Feature f = it.next();
        if ((f.getName().equals(name)) || (f.getName().equals(mirroredName))) {
          return false;
        }
      }
    }
    return true;
  }

  /*
   * Counts all possible interactions between the Features available
   * in the features array.
   */
  private int countAllPossibleInteractions() {
    int numberInteractions = 0;
    LinkedHashSet<Feature> featureSet = new LinkedHashSet<Feature>();

    for (int i = 1; i < this.features.length; i++) {
      for (int j = 1; j < this.features.length; j++) {
        if (this.validateInteraction(this.features[i], this.features[j], featureSet)) {
          featureSet.add(this.createInteraction(this.features[i], this.features[j]));
          numberInteractions++;
        } else {
          continue;
        }
      }
    }
    return numberInteractions;
  }

  /*
   * Creates all possible interactions and stores them in the
   * interactions attribute.
   */
  public void createAllPossibleInteractions() {
    int totalNumberInteractions = this.countAllPossibleInteractions();
    this.interactions = new Feature[totalNumberInteractions];
    LinkedHashSet<Feature> featureSet = new LinkedHashSet<Feature>();
    int numberInteractions = 0;

    for (int i = 1; i < this.features.length; i++) {
      for (int j = 1; j < this.features.length; j++) {
        if (this.validateInteraction(this.features[i], this.features[j], featureSet)) {
          featureSet.add(this.createInteraction(this.features[i], this.features[j]));
          this.interactions[numberInteractions] = this
              .createInteraction(this.features[i], this.features[j]);
          numberInteractions++;
        } else {
          continue;
        }
      }
    }
  }
}
