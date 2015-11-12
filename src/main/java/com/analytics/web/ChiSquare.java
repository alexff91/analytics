package com.analytics.web;

import org.apache.commons.math3.stat.inference.TestUtils;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChiSquare implements Serializable {
	private final ExporterBean exporterBean;

	public ChiSquare(ExporterBean exporterBean) {
		this.exporterBean = exporterBean;
	}

	public void chiSquare() {
		try {
			exporterBean.setBarModel(new BarChartModel());
			exporterBean.getBarModel().getAxis(AxisType.Y).setLabel(exporterBean.getSelectedYChi());
			exporterBean.getBarModel().setLegendPosition("s");
			exporterBean.getBarModel().setLegendCols(10);
			exporterBean.setTableHeader(
					"Chi-square test of " + exporterBean.getSelectedXChi() + " and " + exporterBean.getSelectedYChi());
			int xIndexes = 0;
			int yIndexes = 0;

			for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {

				if (exporterBean.getStatisticsColumnTemplate().get(i).equals(exporterBean.getSelectedXChi())) {
					xIndexes = i;
				}
				if (exporterBean.getStatisticsColumnTemplate().get(i).equals(exporterBean.getSelectedYChi())) {
					yIndexes = i;
				}
			}

			ArrayList<Double> xx = new ArrayList<Double>();
			ArrayList<Long> yy = new ArrayList<Long>();

			int rowInd = 1;
			int rowIndY = 1;
			for (DataValue value : exporterBean.getStatisticsValues()) {
				String xvalues = value.getValues(rowInd, xIndexes);
				String yvalues = value.getValues(rowIndY, yIndexes);
				if (xvalues != null) {
					xx.add(Double.valueOf(xvalues));
				}
				rowInd++;
				if (yvalues != null) {
					yy.add(Double.valueOf(yvalues).longValue());
				}
				rowIndY++;
			}

			exporterBean.getDataValues().clear();
			exporterBean.getColumns().clear();
			exporterBean.getColumnTemplate().clear();
			exporterBean.setColumnTemplate(new ArrayList<String>(Arrays.asList("Value")));
			List<List<String>> properties = new ArrayList<List<String>>(4);
			String[] propertyNames = new String[] { "Chi-square", "P-value  chi-square ",
					"Chi-square  using a fixed sign. level" };
			exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
			properties.add(new ArrayList<String>());
			properties.get(0).add(exporterBean.getSelectedXChi() + " and " + exporterBean.getSelectedYChi());
			double[] x = new double[xx.size() > yy.size() ? yy.size() : xx.size()];
			String[] xStrings = new String[xx.size() > yy.size() ? yy.size() : xx.size()];
			long[] y = new long[xx.size() > yy.size() ? yy.size() : xx.size()];
			String[] yStrings = new String[xx.size() <= yy.size() ? yy.size() : xx.size()];
			for (int i = 0; i < (xx.size() > yy.size() ? yy.size() : xx.size()); i++) {
				Double xDouble = xx.size() > i ? xx.get(i) : 0d;
				x[i] = (double) xDouble;
				xStrings[i] = new Double(xDouble.toString()).intValue() + "";
				Double yLong = yy.size() > i ? yy.get(i).doubleValue() : 0d;
				y[i] = yLong.longValue();
				yStrings[i] = new Double(yLong.toString()).intValue() + "";
			}

			/*
			 * writes the table.
			 */
			HashSet<String> xSet = new HashSet<String>();
			xSet.addAll(Arrays.asList(xStrings));
			HashSet<String> ySet = new HashSet<String>();
			ySet.addAll(Arrays.asList(yStrings));

			ChisqTestExtended crosstab = new ChisqTestExtended();
			double testStatistic = crosstab.testStatistic(xStrings, yStrings);
			double pValue = crosstab.pValue(xStrings, yStrings);
			exporterBean.setContent("<div id=\"correlations\">" + "<h1>Crosstable</h1>");
			exporterBean.setContent(exporterBean.getContent()
					+ "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\">");
			exporterBean.setContent(exporterBean.getContent() + "<tr><td style=\"border:1px solid black;\"></td>");

			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\" colspan = 4 >"
					+ " The contingency table" + "</th>");
			exporterBean.setContent(exporterBean.getContent() + "</tr>");
			exporterBean.setContent(exporterBean.getContent() + "<tr>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");
			exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\" colspan = "
					+ (xSet.size()) + ">" + exporterBean.getSelectedXChi() + "</td>");
			exporterBean
					.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"> Row Count </th>");
			exporterBean.setContent(exporterBean.getContent() + "</tr>");
			exporterBean.setContent(exporterBean.getContent() + "<tr>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");
			exporterBean
					.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\">Row Values</th>");
			Iterator<String> iteratorX = xSet.iterator();
			while (iteratorX.hasNext()) {
				LinkedList linkedList = new LinkedList(xSet);
				String next = iteratorX.next();
				exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\">"
						+ (exporterBean.getMapOfColumns().get(xIndexes) == null ? next
								: exporterBean.getMapOfColumns().get(xIndexes).get(next))
						+ "</td>");
			}
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");
			exporterBean.setContent(exporterBean.getContent() + "</tr>");
			List<Integer> columnCount = new LinkedList<Integer>();
			for (int i = 0; i < crosstab.table.length; i++) {
				for (int j = 0; j < crosstab.table[i].length; j++) {
					if (columnCount.size() < j + 1) {
						columnCount.add(0);
					}
					columnCount.set(j, (int) (columnCount.get(j) + crosstab.table[i][j]));
				}
			}
			List<ChartSeries> seriesList = new LinkedList<ChartSeries>();
			ChartSeries chartXSeries = new ChartSeries();
			chartXSeries.setLabel(exporterBean.getSelectedXChi());
			ChartSeries chartYSeries = new ChartSeries();
			chartYSeries.setLabel(exporterBean.getSelectedYChi());
			seriesList.add(chartXSeries);
			seriesList.add(chartYSeries);
			exporterBean.getBarModel().getSeries().addAll(seriesList);
			Iterator<String> iterator = ySet.iterator();
			for (int i = 0; i < crosstab.table.length; i++) {
				exporterBean.setContent(exporterBean.getContent() + "<tr>");
				if (i == 0) {
					exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\" rowspan="
							+ (crosstab.table.length * 4) + ">" + exporterBean.getSelectedYChi() + "</td>");
				}
				LinkedList linkedList = new LinkedList(ySet);
				String next = iterator.next();
				while (next == null && iterator.hasNext()) {
					next = iterator.next();
				}
				exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\"rowspan=" + 4
						+ ">" + (exporterBean.getMapOfColumns().get(yIndexes) == null ? next
								: exporterBean.getMapOfColumns().get(yIndexes).get(next))
						+ "</td>");
				int rowcount = 0;
				for (int j = 0; j < crosstab.table[i].length; j++) {
					exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\"> Count "
							+ crosstab.table[i][j] + "</td>");
					rowcount += crosstab.table[i][j];
					if (i == 0) {
						chartYSeries.set("ColumnCount " + exporterBean.getSelectedXChi() + " " + xSet.toArray()[j],
								columnCount.get(j));
					}
				}
				chartXSeries.set("Rowcount " + exporterBean.getSelectedYChi() + " " + ySet.toArray()[i], rowcount);
				exporterBean
						.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\" rowspan = 4 >"
								+ "Total row count " + rowcount + "</td>");
				exporterBean.setContent(exporterBean.getContent() + "</tr>");
				exporterBean.setContent(exporterBean.getContent() + "<tr>");
				for (int j = 0; j < crosstab.table[i].length; j++) {
					exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\" >"
							+ "Expected  count " + rowcount / crosstab.table[i].length + "</td>");
				}
				exporterBean.setContent(exporterBean.getContent() + "</tr>");
				exporterBean.setContent(exporterBean.getContent() + "<tr>");
				for (int j = 0; j < crosstab.table[i].length; j++) {
					exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\">"
							+ "Percent % within " + exporterBean.getSelectedXChi() + " " + ExporterBean
									.roundTo2Decimals(((crosstab.table[i][j]) / new Double(columnCount.get(j))) * 100d)
							+ "</td>");
				}
				exporterBean.setContent(exporterBean.getContent() + "</tr>");
				exporterBean.setContent(exporterBean.getContent() + "<tr>");
				for (int j = 0; j < crosstab.table[i].length; j++) {
					exporterBean.setContent(
							exporterBean.getContent() + "<td style=\"border:1px solid black;\">" + "Std.Residual "
									+ ExporterBean.roundTo2Decimals(
											(crosstab.table[i][j] - rowcount / crosstab.table[i].length)
													/ (Math.sqrt(rowcount / crosstab.table.length)))
									+ "</td>");
				}
				exporterBean.setContent(exporterBean.getContent() + "</tr>");
			}
			if (x.length == y.length) {
				properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.chiSquare(x, y))));
				properties.get(0).add(String.valueOf(ExporterBean.roundTo2Decimals(TestUtils.chiSquareTest(x, y))));
				properties.get(0)
						.add(String.valueOf(TestUtils.chiSquareTest(x, y, Double.valueOf(exporterBean.getAlpha()))));
			}

			Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

			for (int c = 1; c <= properties.size(); c++) {
				values.put(c, properties.get(c - 1));
				exporterBean.getDataValues().add(new DataValue(c, values));
			}
			exporterBean.createDynamicColumns();
			exporterBean.nullifyAll();
			exporterBean.setContent(exporterBean.getContent() + "<tr>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");

			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\"></th>");
			exporterBean.setContent(exporterBean.getContent() + "</tr>");
			exporterBean.setContent(exporterBean.getContent() + "<tr>");
			exporterBean.setContent(exporterBean.getContent() + "<td style=\"border:1px solid black;\" colspan = 5>"
					+ "Total column count " + columnCount + "</td>");
			exporterBean.setContent(exporterBean.getContent() + "</tr>");
			exporterBean.setContent(exporterBean.getContent() + "<tr><td style=\"border:1px solid black;\"></td>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\">" + "</th>");
			exporterBean.setContent(exporterBean.getContent() + "<th style=\"border:1px solid black;\" colspan = 3>"
					+ crosstab.output + "</th>");
			exporterBean.setContent(exporterBean.getContent() + "</tr>");
			exporterBean.setContent(exporterBean.getContent() + "</table></div>");

			exporterBean.setDescription(
					"<br>Chi-square - It is also known as Pearson chi-square test.  It compares the observed"
							+ "<br> frequencies with the expected frequencies collectively (considering the degree of freedom"
							+ "<br> for each of the variables).  The degrees of freedom for chi-square test is (R-1)*(C-1)"
							+ "<br> where R is the number of rows and C the number of columns of the table."
							+ "<br>  (In other words, the number of levels of each of the variables.)"
							+ "<br>  A large chi-square statistic will correspond to small p-value.  If the p-value is small"
							+ "<br> enough (say < 0.05), then we will reject the null hypothesis that the two variables are independent"
							+ "<br> and conclude that there is an association between the row and the column variables. "
							+ "<br><li> Table of - This is the title of the table.  The first variable listed will be the row variable and the second variable will be the column variable.\n"
							+ "\n"
							+ "<br><li> Frequency - This is the observed cell frequency.  It is also called count.  For example, there are 15 males (female=0) in the low socioeconomic status group.  The observed cell frequencies and the expected cell frequencies are used to test if the row and the column variables are independent.\n"
							+ "\n"
							+ "<br><li> Expected - This is the cell frequency expected under the null hypothesis that the row and column variables are independent.  This number is produced by using the option expected in the tables statement.  Comparing the expected cell frequency with the observed frequency we should have some idea about whether the row variable is independent of the column variable.\n"
							+ "\n"
							+ "<br><li> Row Pct - This gives the percent of observations in the row.  If there are 15 males (female=0) and 32 females (female=1) in low socioeconomic status group.  So the row percent for the first cell is 15/47*100=31.91.  n"
							+ "\n"
							+ "<br><li> Col Pct - This gives the percent of observations in the column.  If there are 91 males and there are 15 males in the low socioeconomic status group. So the column percent for the first cell is 15/91*100=16.48. \n"
							+ "\n"
							+ "<br><li> Total - This is the number of valid observations for the variable.  The total number of observations is the sum of N and the number of missing values.  If the sample size is not large enough, the test of independence of contingency tables such as Chi-square may not be accurate.\n"
							+ "\n" + "");
		} catch (Exception e) {
			e.printStackTrace();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e
					.getMessage() + " Preconditions:\n" + "\n" + "Expected counts must all be positive.\n"
					+ "Observed counts must all be ≥ 0.\n"
					+ "The observed and expected arrays must have the same length and their common length must be at least 2."));
		}
	}
}