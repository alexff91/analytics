package com.analytics.web;

import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DescriptiveStatistics implements Serializable {
	private final ExporterBean exporterBean;

	public DescriptiveStatistics(ExporterBean exporterBean) {
		this.exporterBean = exporterBean;
	}

	public void descriptiveStatistics(String[] descrStatData) {
		try {
			exporterBean.setBarModel(new BarChartModel());
			exporterBean.getBarModel().setLegendPosition("s");
			exporterBean.getBarModel().getAxis(AxisType.Y).setLabel("Count of value");
			exporterBean.getBarModel().setLegendCols(10);
			exporterBean.setTableHeader("Descriptive statistics");
			List<org.apache.commons.math3.stat.descriptive.DescriptiveStatistics> decList = new LinkedList<org.apache.commons.math3.stat.descriptive.DescriptiveStatistics>();
			int j = 1;
			for (DataValue value : exporterBean.getStatisticsValues()) {
				for (int i = 0; i < value.getValues().get(j).size()
						&& decList.size() != value.getValues().get(j).size(); i++) {
					decList.add(new org.apache.commons.math3.stat.descriptive.DescriptiveStatistics());
				}
				for (int i = 0; i < value.getValues().get(j).size(); i++) {
					String values = value.getValues(j, i);
					if (values != null) {
						decList.get(i).addValue(Double.valueOf(values));
					}
				}
				j++;
			}

			exporterBean.getDataValues().clear();
			exporterBean.getColumns().clear();
			exporterBean.getColumnTemplate().clear();
			List<List<String>> properties = new ArrayList<List<String>>(decList.size());
			String[] propertyNames = new String[] { "Variables", "Mean", "Std Deviation", "Geom. Mean", "Kurtosis",
					"Maximum", "Minimum", "N", "Skewness", "Sum of Squares", "Population Variance", "Variance" };
			exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
			List<ChartSeries> seriesList = new LinkedList<ChartSeries>();
      ;
      for (int i = 0, propertiesCount = 0; i < exporterBean.getStatisticsColumnTemplate().size();
          i++) {
        if (Arrays.asList(descrStatData).contains(exporterBean.getStatisticsColumnTemplate().get(i))) {
					properties.add(new ArrayList<String>());
          properties.get(propertiesCount).add(exporterBean.getStatisticsColumnTemplate().get(i));
          ChartSeries chartSeries = new ChartSeries();
					chartSeries.setLabel(exporterBean.getStatisticsColumnTemplate().get(i));
					seriesList.add(chartSeries);
          propertiesCount++;
        }
			}
      for (int i = 0, propertiesCount = 0; i < decList.size(); i++) {
        if (Arrays.asList(descrStatData).contains(exporterBean.getStatisticsColumnTemplate().get(i))) {
					org.apache.commons.math3.stat.descriptive.DescriptiveStatistics deccriptor = decList.get(i);
					double mean = ExporterBean.roundTo2Decimals(deccriptor.getMean());
          properties.get(propertiesCount).add(String.valueOf(mean));
          double standardDeviation = ExporterBean.roundTo2Decimals(deccriptor.getStandardDeviation());
          properties.get(propertiesCount).add(String.valueOf(standardDeviation));
          double geomMean = ExporterBean.roundTo2Decimals(deccriptor.getGeometricMean());
          properties.get(propertiesCount).add(String.valueOf(geomMean));
          double kurtosis = ExporterBean.roundTo2Decimals(deccriptor.getKurtosis());
          properties.get(propertiesCount).add(String.valueOf(kurtosis));
          double max = ExporterBean.roundTo2Decimals(deccriptor.getMax());
          properties.get(propertiesCount).add(String.valueOf(max));
          double min = ExporterBean.roundTo2Decimals(deccriptor.getMin());
          properties.get(propertiesCount).add(String.valueOf(min));
          double n = deccriptor.getN();
          seriesList.get(propertiesCount).set("N", n);
          properties.get(propertiesCount).add(String.valueOf(n));
          double skewness = ExporterBean.roundTo2Decimals(deccriptor.getSkewness());
          properties.get(propertiesCount).add(String.valueOf(skewness));
          double sumsq = ExporterBean.roundTo2Decimals(deccriptor.getSumsq());
          properties.get(propertiesCount).add(String.valueOf(sumsq));
          double populationVariance = ExporterBean.roundTo2Decimals(deccriptor.getPopulationVariance());
          properties.get(propertiesCount).add(String.valueOf(populationVariance));
          double variance = ExporterBean.roundTo2Decimals(deccriptor.getVariance());
          properties.get(propertiesCount).add(String.valueOf(variance));
          propertiesCount++;
        }
			}

			Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

			for (int c = 1; c <= properties.size(); c++) {
				values.put(c, properties.get(c - 1));
				exporterBean.getDataValues().add(new DataValue(c, values));
			}
			exporterBean.createDynamicColumns();
			exporterBean.nullifyAll();
			exporterBean
					.setDescription("" + "<br><li> Mean - the <a href=\"http://www.xycoon.com/arithmetic_mean.htm\">\n"
							+ "   arithmetic mean </a> of the available values\n"
							+ "<br><li>  Std Deviation -  Standard deviation is the square root of the variance.  It measures the spread of a set of observations.  The larger the standard deviation is, the more spread out the observations are."
							+ "<br><li>  Geom. Mean - the <a href=\"http://www.xycoon.com/geometric_mean.htm\">\n"
							+ " geometric mean </a> of the available values" + "\n"
							+ "<br><li>  Kurtosis - the Kurtosis of the available values. Kurtosis is a\n"
							+ " measure of the \"peakedness\" of a distribution\n. Kurtosis - Kurtosis is a measure of the heaviness of the tails of a distribution. In SAS, a normal distribution has kurtosis 0. Extremely nonnormal distributions may have high positive or negative kurtosis values, while nearly normal distributions will have kurtosis values close to 0. Kurtosis is positive if the tails are \"heavier\" than for a normal distribution and negative if the tails are \"lighter\" than for a normal distribution."
							+ "\n" + "<br> Maximum - This is the maximum, or largest, value of the variable.\n"
							+ "<br> Minimum - the minimum of the available values.\n" + "\n"
							+ "<br><li>  N - the number of available values.\n"
							+ "<br><li>  Skewness - Skewness measures the degree and direction of asymmetry.  A symmetric distribution such as a normal distribution has a skewness of 0, and a distribution that is skewed to the left, e.g. when the mean is less than the median, has a negative skewness."
							+ "<br><li>  Sum of Squares - the sum of the squares of the available values.\n"
							+ "<br><li>  Population Variance - the <a href=\"http://en.wikibooks.org/wiki/Statistics/Summary/Variance\">\n"
							+ " population variance</a> of the available values.\n"
							+ "<br><li>  Variance - The variance is a measure of variability. It is the sum of the squared distances of data value from the mean divided by the variance divisor. The Corrected SS is the sum of squared distances of data value from the mean. Therefore, the variance is the corrected SS divided by N-1. We don't generally use variance as an index of spread because it is in squared units. Instead, we use standard deviation.\n"
							+ "");

			for (ChartSeries series : seriesList) {
				exporterBean.getBarModel().addSeries(series);
			}
		} catch (Exception e) {
			e.printStackTrace();
      exporterBean.setBarModel(null);
      FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
		}
	}
}