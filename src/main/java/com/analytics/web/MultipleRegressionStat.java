package com.analytics.web;

import multipleLinearRegression.Dependent;
import multipleLinearRegression.Feature;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultipleRegressionStat implements Serializable {
  private final ExporterBean exporterBean;

  public MultipleRegressionStat(ExporterBean exporterBean) {
    this.exporterBean = exporterBean;
  }

  public void multipleRegressionStat() {
    try {
      int dependant = 0;
      ArrayList<Integer> independant = new ArrayList<Integer>();
      for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
        for (int ind = 0; ind < exporterBean.getSelectedInDepVarMultipleReg().length; ind++) {
          if (exporterBean.getStatisticsColumnTemplate().get(i)
              .equals(exporterBean.getSelectedInDepVarMultipleReg()[ind])) {
            independant.add(i);
          }
        }

        if (exporterBean.getStatisticsColumnTemplate().get(i)
            .equals(exporterBean.getSelectedDepVarMultipleReg())) {
          dependant = i;
        }
      }

      double[][] y = new double[exporterBean.getStatisticsValues().size()][independant.size()];
      double[] x = new double[exporterBean.getStatisticsValues().size()];
      int j = 1;
      int z = 1;
      for (DataValue value : exporterBean.getStatisticsValues()) {
        String values = value.getValues(j, dependant);
        if (values != null) {
          x[j - 1] = Double.valueOf(values);
          j++;
        }

        for (int i = 0; i < independant.size(); i++) {

          String valuesy = value.getValues(z, independant.get(i));
          if (valuesy != null) {
            y[z - 1][i] = Double.valueOf(valuesy);
          }
        }
        z++;
      }
      LinkedList<Feature> features = new LinkedList<Feature>();
      List<List<Double>> listY = new LinkedList<List<Double>>();
      for (double[] array : y) {
        LinkedList<Double> doubles = new LinkedList<Double>();
        for (double d : array) {
          doubles.add(d);
        }
        listY.add(doubles);
      }
      List<List<Double>> transposed = ExporterBean.transpose(listY);
      for (int i = 0; i < transposed.size(); i++) {
        double[] objects = exporterBean.toDoubleArray(
            transposed.get(i).toArray(new Double[transposed.get(i).size()]));
        features.add(new Feature(i, exporterBean.getStatisticsColumnTemplate().get(i), objects));
      }

      Dependent dep = new Dependent(x);
      MultipleLinearRegressionExt reg = new MultipleLinearRegressionExt(
          features.toArray(new Feature[features.size()]), dep);
      exporterBean.setOutput(reg.doMultipleLinearRegressionExt());

      String thrVar = "AdjustedRSquared";
      if (exporterBean.getMethodReg().contains("RSS")) {
        thrVar = "RSS";
      }
      if (exporterBean.getMethodReg().contains("NFeatures")) {
        thrVar = "NFeatures";
      }
      StepwiseSelectorExt sel = new StepwiseSelectorExt(reg, thrVar);
      if (exporterBean.getMethodReg().contains("Back Propogation")) {
        exporterBean.setOutput(exporterBean.getOutput() + sel.backwardSelection());
      }
      if (exporterBean.getMethodReg().contains("Forward Propogation")) {
        exporterBean.setOutput(exporterBean.getOutput() + sel.forwardSelection());
      }

      //            reg.createAllPossibleInteractions();

      exporterBean.setDescription("<h3>Stepwise selection</h3>\n" +
          "\n" +
          "<br><li>Stepwise selection is a method that allows moves in either direction, dropping or adding variables"
          +
          "<br> at the various steps. Backward stepwise selection involves starting off in a backward approach and"
          +
          "<br> then potentially adding back variables if they later appear to be significant. The process is one of"
          +
          "<br> alternation between choosing the least significant variable to drop and then re-considering all dropped"
          +
          "<br> variables (except the most recently dropped) for re-introduction into the model. This means that two"
          +
          "<br> separate significance levels must be chosen for deletion from the model and for adding to the model."
          +
          " The second significance must be more stringent than the first.\n" +
          "\n" +
          "<p><br><li>Forward stepwise selection is also a possibility, though not as common. In the forward approach,"
          +
          " variables once entered may be dropped if they are no longer significant as other variables are added."
          +
          "<p><br><li> RSS -  the residual sum of squares (RSS), also known as the sum of squared residuals (SSR) or"
          +
          "<br> the sum of squared errors of prediction (SSE), is the sum of the squares of residuals (deviations "
          +
          "<br>of predicted from actual empirical values of data). It is a measure of the discrepancy between the"
          +
          "<br> data and an estimation model. A small RSS indicates a tight fit of the model to the data. It is used"
          +
          "<br> as an optimality criterion in parameter selection and model selection.\n" +
          "<br>In general, total sum of squares = explained sum of squares + residual sum of squares. For a proof of this"
          +
          "<br> in the multivariate ordinary least squares (OLS) case, see partitioning in the general OLS model."
          +
          "<p><br><li>AdjustedRSquared -  The adjusted R2 value indicates the ratio of variation that is explained by the model"
          +
          " <br>to the total variation in the model, adjusted downward to compensate for overfitting.\n"
          +
          "\n" +
          "<br>The larger the number of independent variables is compared to the number of observations, the lower the adjusted R2"
          +
          "<br> value will be. When using models with a larger set of independent variables, the aditional variables may be essentially"
          +
          " <br>modeling noise.\n" +
          "\n" +
          "<br>The unadjusted R2 value is returned by the RSquared property." +
          "<p><br><li> ESS - the explained sum of squares (ESS), alternatively known as the model" +
          "<br> sum of squares or sum of squares due to regression (\"SSR\" – not to be confused with"
          +
          "<br> the residual sum of squares RSS), is a quantity used in describing how well a model,"
          +
          "<br> often a regression model, represents the data being modelled. In particular, the explained sum "
          +
          "<br>of squares measures how much variation there is in the modelled values and this is compared to the total"
          +
          "<br> sum of squares, which measures how much variation there is in the observed data, and to the residual sum of"
          +
          "<br>squares, which measures the variation in the modelling errors." +
          "<p><br><li>TSS -  the total sum of squares (TSS or SST) is a quantity that appears as part of a standard way of"
          +
          "<br> presenting results of such analyses. It is defined as being the sum, over all observations, of"
          +
          "<br> the squared differences of each observation from the overall mean." +
          "<p><br><li> Bias -  the bias (or bias function) of an estimator is the difference between this estimator's"
          +
          "<br>  expected value and the true value of the parameter being estimated. An estimator or decision rule"
          +
          "<br>  with zero bias is called unbiased. Otherwise the estimator is said to be biased. In statistics, \"bias\""
          +
          "<br>  is an objective statement about a function, and while not a desired property, it is not pejorative,"
          +
          "<br>  unlike the ordinary English use of the term \"bias\"." +
          "<p><br><li> f-statistics - F-statistics (also known as fixation indices) describe the statistically expected"
          +
          "<br> level of heterozygosity in a population; more specifically the expected degree of (usually) a reduction"
          +
          "<br>  in heterozygosity when compared to Hardy–Weinberg expectation.");
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getLocalizedMessage()));
    }
  }
}