package com.analytics.web;

import org.apache.commons.math3.stat.Frequency;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.ChartSeries;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class FrequencyDistribution implements Serializable {
	private final ExporterBean exporterBean;

	public FrequencyDistribution(ExporterBean exporterBean) {
		this.exporterBean = exporterBean;
	}

	public void frequencyDistributions(String[] freqDistribData) {
		try {
      if (exporterBean.getBarModel() != null) {
        exporterBean.getBarModel().getAxis(AxisType.Y)
            .setLabel
                ("Valid percentage");
      }
      exporterBean.setTableHeader("Frequency Distribution");
			List<Frequency> decList = new LinkedList<Frequency>();
			List<Integer> missedValues = new ArrayList<Integer>();
			List<Integer> validValdues = new ArrayList<Integer>();
			int j = 1;
			for (DataValue value : exporterBean.getStatisticsValues()) {
				for (int i = 0; i < value.getValues().get(j).size()
						&& decList.size() != value.getValues().get(j).size(); i++) {
					decList.add(new Frequency());
					missedValues.add(0);
					validValdues.add(0);
				}
				for (int i = 0; i < value.getValues().get(j).size(); i++) {
					if (value.getValues(j, i) != null) {
						decList.get(i).addValue(Double.valueOf(value.getValues(j, i)));
						validValdues.set(i, validValdues.get(i) + 1);
					} else {
						missedValues.set(i, missedValues.get(i) + 1);
					}
				}
				j++;
			}
			List<ChartSeries> seriesList = exporterBean.getChartSerieses();
			List<List<String>> properties = new ArrayList<>(decList.size());
			String[] propertyNames = new String[] { "Sum. freq.", "Unique count", "Valid pct.", "Missed pct.", "Value",
					"Freq. count", "Cum. freq.", "Cum. pct.", "Freq. pct." };
			exporterBean.getColumnTemplate().addAll(Arrays.asList(propertyNames));
			for (int i = 0; i < exporterBean.getStatisticsColumnTemplate().size(); i++) {
				if (Arrays.asList(freqDistribData).contains(exporterBean.getStatisticsColumnTemplate().get(i))) {
					properties.add(new ArrayList<String>());
				}
			}
			int s = 0;
			for (int i = 0; i < decList.size(); i++) {
				if (Arrays.asList(freqDistribData).contains(exporterBean.getStatisticsColumnTemplate().get(i))) {
					properties.get(s).add(exporterBean.getStatisticsColumnTemplate().get(i));
					Frequency frequency = decList.get(i);
					double sumFreq = frequency.getSumFreq();
					properties.get(s).add(String.valueOf(sumFreq));
					double uniqueCount = frequency.getUniqueCount();
					properties.get(s).add(String.valueOf(uniqueCount));
					int fullSumOfCount = validValdues.get(i) + missedValues.get(i);
					properties.get(s).add(
							"" + ExporterBean.roundTo2Decimals((validValdues.get(i) * 100.0) / fullSumOfCount) + "%");
					seriesList.get(i).set("Valid pct.",
							ExporterBean.roundTo2Decimals((validValdues.get(i) * 100.0) / fullSumOfCount));
					properties.get(s).add(
							"" + ExporterBean.roundTo2Decimals((missedValues.get(i) * 100.0) / fullSumOfCount) + "%");
					Iterator<Comparable<?>> iterator = frequency.valuesIterator();
					Stack<Double> stack = new Stack<Double>();
					while (iterator.hasNext()) {
						stack.add((Double) iterator.next());
						properties.add(new ArrayList<String>());
					}
					s = s + 1;
					for (Double stackValue : stack) {
						properties.get(s).add("");
						properties.get(s).add("");
						properties.get(s).add("");
						properties.get(s).add("");
						properties.get(s).add("");
						properties.get(s).add(stackValue.toString());
						double frequencyCount = frequency.getCount(stackValue);
						properties.get(s).add("" + frequencyCount);
						double cumFreq = frequency.getCumFreq(stackValue);
						properties.get(s).add("" + ExporterBean.roundTo2Decimals(cumFreq));
						double cumPct = frequency.getCumPct(stackValue);
						properties.get(s).add("" + ExporterBean.roundTo2Decimals(cumPct));
						double frequencyPct = ExporterBean.roundTo2Decimals(frequency.getPct(stackValue));
						properties.get(s).add("" + frequencyPct);
						properties.get(s).add("" + frequencyPct);

						s++;
					}
				}
			}
			exporterBean.setDescription("" + "<br><li> Sum. freq. - the  sum of all frequencies."
					+ "<br><li> Unique count -  the number of unique values that have been added to the frequency table."
          + "<br><li> Valid pct. - the percentage of valid values to the total count."
          + "<br><li> Missed pct. - the percentage of missed values to the total count."
					+ "<br><li> Freq. count - the number of values equal to value."
					+ "<br><li> Cum. freq. - the cumulative frequency of values less than or equal to v."
					+ "<br><li> Cum. pct. - the cumulative percentage of values less than or equal to value."
					+ " (as a proportion between 0 and 1)."
					+ "<br><li> Freq. pct. - the percentage of values that are equal to v."
					+ "   (as a proportion between 0 and 1)." + "");

			Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

			for (int c = 1; c <= properties.size(); c++) {
				values.put(c, properties.get(c - 1));
				exporterBean.getDataValues().add(new DataValue(c, values));
			}
			exporterBean.createDynamicColumns();
			exporterBean.nullifyAll();
		} catch (Exception e) {
			e.printStackTrace();
      exporterBean.setBarModel(null);
      FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
		}
	}
}