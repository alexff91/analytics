package com.analytics.web;


import java.util.*;

import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.apache.commons.math3.stat.regression.GLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.*;

import com.monitorjbl.xlsx.StreamingReader;
import org.primefaces.model.UploadedFile;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

@ManagedBean(name = "bBean")
@SessionScoped
public class ExporterBean implements Serializable {

    private ArrayList<String> columnTemplate = new ArrayList<String>(Arrays.asList("id"));
    private ArrayList<String> statisticsColumnTemplate = new ArrayList<String>();

    private List<ColumnModel> columns = new ArrayList<>();

    private List<DataValue> dataValues = new ArrayList<>();

    private List<DataValue> statisticsValues = new ArrayList<>();

    private List<DataValue> filteredDataValues;

    private String selectedDepVars;
    private String[] selectedIndepVars;
    private String[] selectedObsVars;

    private String selectedDep;
    private String selectedInDep;
    private String selectedX;
    private String selectedY;
    private String tieStrategy;
    private String nanStrategy;
    private String[] matrixDataCov;
    private String[] testsData;
    private String operation;
    private String varName;
    private String muValue;
    private String alpha;

    @PostConstruct
    public void init() {

    }


    public void nullifyAll() {
        this.filteredDataValues = null;
        this.selectedDepVars = null;
        this.selectedIndepVars = null;
        this.selectedObsVars = null;
        this.selectedDep = null;
        this.selectedInDep = null;
        this.selectedX = null;
        this.selectedY = null;
        this.tieStrategy = null;
        this.nanStrategy = null;
        this.matrixDataCov = null;
        this.testsData = null;
        this.operation = null;
        this.varName = null;
        this.muValue = null;
        this.alpha = null;
    }

    public List<DataValue> getDataValues() {
        return dataValues;
    }

    public List<DataValue> getFilteredDataValues() {
        return filteredDataValues;
    }

    public void setFilteredDataValues(List<DataValue> filteredDataValues) {
        this.filteredDataValues = filteredDataValues;
    }


    public List<ColumnModel> getColumns() {
        return columns;
    }

    private void createDynamicColumns() {
        int i = 0;
        for (String columnKey : columnTemplate) {
            String key = columnKey.trim();

            columns.add(new ColumnModel(columnKey.toUpperCase(), columnKey, i));
            i++;
        }
    }

    public void updateColumns() {
        //reset table state
        UIComponent table = FacesContext.getCurrentInstance().getViewRoot().findComponent(":form:dataValues");
//        table.setValueExpression("sortBy", null);

        //update columns
        createDynamicColumns();
        nullifyAll();
    }

    public String[] getTestsData() {
        return testsData;
    }

    public void setTestsData(String[] testsData) {
        this.testsData = testsData;
    }


    static public class ColumnModel implements Serializable {

        private String header;
        private String property;
        private int index;


        public ColumnModel(String header, String property, int index) {
            this.header = header;
            this.property = property;
            this.index = index;

        }

        public String getHeader() {
            return header;
        }

        public String getProperty() {
            return property;
        }

        public int getIndex() {
            return index;
        }


    }

    public void handleFileUpload(FileUploadEvent event) {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            UploadedFile file1 = event.getFile();
            if (file1.equals(null)) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File is null", null));
            }
            String fileName = file1.getFileName();
            String format = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            if (format.equals(".xlsx")) {
                InputStream inputStream;
                StreamingReader reader = null;
                try {
                    inputStream = file1.getInputstream();
                    File file = new File(fileName);
                    OutputStream out = new FileOutputStream(file);

                    IOUtils.copy(inputStream, out);
                    inputStream.close();
                    out.close();
                    InputStream is = new FileInputStream(file);
                    reader = StreamingReader.builder()
                            .rowCacheSize(500)    // number of rows to keep in memory (defaults to 10)
                            .bufferSize(8096)     // buffer size to use when reading InputStream to file (defaults to 1024)
                            .sheetIndex(0)        // index of sheet to use (defaults to 0)
                            .read(is);            // InputStream or File for XLSX file (required)


                } catch (IOException e) {
                    facesContext
                            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reading file" + e, null));
                }


                Iterator<Row> rowIterator = reader.iterator();
                int i = 0;
                dataValues.clear();
                columnTemplate.clear();
                columns.clear();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    Iterator<Cell> cellIterator = row.cellIterator();
                    ArrayList<String> cells = new ArrayList<>();

                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();

                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                if (i != 0) {
                                    if (cells.size() < cell.getColumnIndex()) {
                                        for (int j = cells.size(); j < cell.getColumnIndex(); j++) {
                                            cells.add(null);
                                        }
                                    }
                                    cells.add(cell.getColumnIndex(), "" + cell.getNumericCellValue());
                                } else {
                                    cells.add("id");
                                }

                                break;
                            case Cell.CELL_TYPE_STRING:
                                if (i == 0) {

                                    columnTemplate.add(cell.getStringCellValue());
                                }
                                break;
                        }

                    }
                    if (i != 0) {
                        if (cells.size() > 0) {
                            Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();
                            LinkedList<String> rowValues = new LinkedList<String>();
                            for (int j = 0; j < columnTemplate.size(); j++) {
                                rowValues.add(cells.get(j));
                            }
                            values.put(i, rowValues);
                            dataValues.add(new DataValue(i, values));
                        }
                    }
                    i++;
                }
            } else if (format.equals(".csv")) {
                ICsvListReader listReader = null;
                try {
                    InputStream inputStream;
                    inputStream = file1.getInputstream();
                    File file = new File(fileName);
                    OutputStream out = new FileOutputStream(file);

                    IOUtils.copy(inputStream, out);
                    inputStream.close();
                    out.close();
                    listReader = new CsvListReader(new FileReader(file), CsvPreference.TAB_PREFERENCE);

                    String[] listReaderHeader = listReader.getHeader(false); // skip the header (can't be used with CsvListReader)
                    int amountOfColumns = listReader.length();
                    CellProcessor[] processor = new CellProcessor[amountOfColumns];
                    List<Object> dataList;
                    dataValues.clear();
                    columnTemplate.clear();
                    columns.clear();
                    int i = 0;
                    while ((dataList = listReader.read(processor)) != null) {
                        ArrayList<String> array = new ArrayList<String>();
                        for (Object value : dataList) {
                            array.addAll(Arrays.asList(((String) value).split(",")));
                        }
                        if (i == 0) {
                            columnTemplate.addAll(Arrays.asList((listReaderHeader[0]).split(",")));
                        } else {
                            Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();
                            LinkedList<String> rowValues = new LinkedList<>();

                            rowValues.addAll(array);

                            values.put(i, rowValues);
                            dataValues.add(new DataValue(i, values));
                        }
                        i++;
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (listReader != null) {
                        try {
                            listReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            statisticsValues = new ArrayList<>(dataValues);


            String[] ts = columnTemplate.toArray(new String[columnTemplate.size()]);
            statisticsColumnTemplate = Lists.newArrayList(ts);
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            nullifyAll();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void original() {
        try {
            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            dataValues = new ArrayList<>(statisticsValues);
            columnTemplate = new ArrayList<>(statisticsColumnTemplate);

            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void addVariable() {
        try {
            int dependant = 0;
            int independant = 0;

            for (int i = 0; i < columnTemplate.size(); i++) {

                if (columnTemplate.get(i).equals(selectedX)) {
                    dependant = i;
                }
                if (columnTemplate.get(i).equals(selectedY)) {
                    independant = i;
                }
            }


            double[] x = new double[statisticsValues.size()];
            double[] y = new double[statisticsValues.size()];
            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                String xvalues = value.getValues(rowInd, dependant);
                if (xvalues != null) {
                    x[rowInd - 1] = Double.valueOf(xvalues);
                    String valuesy = value.getValues(rowInd, independant);
                    if (valuesy != null)
                        y[rowInd - 1] = Double.valueOf(valuesy);

                    rowInd++;
                }
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();


            statisticsColumnTemplate.add(varName);
            for (int c = 0; c < statisticsValues.size(); c++) {
                if (operation.equals("add")) {
                    statisticsValues.set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] + y[c], c + 1)));
                } else if (operation.equals("multiply")) {
                    statisticsValues.set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] * y[c], c + 1)));
                } else if (operation.equals("divide")) {
                    statisticsValues.set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] / y[c], c + 1)));
                } else if (operation.equals("extract")) {
                    statisticsValues.set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] - y[c], c + 1)));
                }


            }

            dataValues = new ArrayList<>(statisticsValues);
            columnTemplate = new ArrayList<>(statisticsColumnTemplate);
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void descriptiveStatistics() {
        try {
            List<DescriptiveStatistics> decList = new LinkedList<>();
            int j = 1;
            for (DataValue value : statisticsValues) {
                for (int i = 0; i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j).size(); i++) {
                    decList.add(new DescriptiveStatistics());
                }
                for (int i = 0; i < value.getValues().get(j).size(); i++) {
                    String values = value.getValues(j, i);
                    if (values != null) {
                        decList.get(i).addValue(Double.valueOf(values));
                    }
                }
                j++;
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(statisticsColumnTemplate);
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "mean", "standardDeviation", "geomMean", "kurtosis", "max", "min", "n", "skewness", "sumsq", "populationVariance", "variance"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }
            for (int i = 0; i < decList.size(); i++) {

                DescriptiveStatistics deccriptor = decList.get(i);
                double mean = deccriptor.getMean();
                properties.get(0).add(String.valueOf(mean));
                double standardDeviation = deccriptor.getStandardDeviation();
                properties.get(1).add(String.valueOf(standardDeviation));
                double geomMean = deccriptor.getGeometricMean();
                properties.get(2).add(String.valueOf(geomMean));
                double kurtosis = deccriptor.getKurtosis();
                properties.get(3).add(String.valueOf(kurtosis));
                double max = deccriptor.getMax();
                properties.get(4).add(String.valueOf(max));
                double min = deccriptor.getMin();
                properties.get(5).add(String.valueOf(min));
                double n = deccriptor.getN();
                properties.get(6).add(String.valueOf(n));
                double skewness = deccriptor.getSkewness();
                properties.get(7).add(String.valueOf(skewness));
                double sumsq = deccriptor.getSumsq();
                properties.get(8).add(String.valueOf(sumsq));
                double populationVariance = deccriptor.getPopulationVariance();
                properties.get(9).add(String.valueOf(populationVariance));
                double variance = deccriptor.getVariance();
                properties.get(10).add(String.valueOf(variance));

            }

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void frequencyDistributions() {
        try {
            List<Frequency> decList = new LinkedList<>();
            int j = 1;
            for (DataValue value : statisticsValues) {
                for (int i = 0; i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j).size(); i++) {
                    decList.add(new Frequency());
                }
                for (int i = 0; i < value.getValues().get(j).size(); i++) {
                    if (value.getValues(j, i) != null) {
                        decList.get(i).addValue(Double.valueOf(value.getValues(j, i)));
                    }
                }
                j++;
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(statisticsColumnTemplate);
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "sumFreq", "uniqueCount"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }

            for (int i = 0; i < decList.size(); i++) {

                Frequency frequency = decList.get(i);
                double sumFreq = frequency.getSumFreq();
                properties.get(0).add(String.valueOf(sumFreq));
                double uniqueCount = frequency.getUniqueCount();
                properties.get(1).add(String.valueOf(uniqueCount));

                Iterator<Comparable<?>> iterator = frequency.valuesIterator();
                Stack<Double> stack = new Stack<>();
                while (iterator.hasNext()) {
                    stack.add((Double) iterator.next());
                }
                int s = properties.size();
                int existedValue = -1;
                for (Double stackValue : stack) {
                    for (int ind = 2; ind < properties.size(); ind++) {
                        ArrayList<String> array = new ArrayList<>(properties.get(ind));

                        if (array.size() > 0 && array.get(0).equals("frequencyCount of " + stackValue)) {
                            existedValue = ind;
                        } else {
                            properties.get(ind).add("");
                        }
                    }
                    propertyNames = new String[]{
                            "frequencyCount of " + stackValue, "cumFreq of " + stackValue, "cumPct of " + stackValue, "frequencyPct of " + stackValue
                    };
                    if (existedValue == -1) {
                        for (int p = s; p < propertyNames.length + s; p++) {
                            properties.add(new ArrayList<>());
                            properties.get(p).add(propertyNames[p - s]);
                        }
                    } else {
                        s = existedValue;
                    }
                    double frequencyCount = frequency.getCount(stackValue);
                    for (int z = 0; z < i; z++) {
                        properties.get(s).add("");
                    }
                    properties.get(s++).add(String.valueOf(frequencyCount));
                    double cumFreq = frequency.getCumFreq(stackValue);
                    for (int z = 0; z < i; z++) {
                        properties.get(s).add("");
                    }
                    properties.get(s++).add(String.valueOf(cumFreq));
                    double cumPct = frequency.getCumPct(stackValue);
                    for (int z = 0; z < i; z++) {
                        properties.get(s).add("");
                    }
                    properties.get(s++).add(String.valueOf(cumPct));
                    double frequencyPct = frequency.getPct(stackValue);
                    for (int z = 0; z < i; z++) {
                        properties.get(s).add("");
                    }
                    properties.get(s++).add(String.valueOf(frequencyPct));
                }


            }

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void ttest() {
        try {
            int xIndexes = 0;


            for (int i = 0; i < columnTemplate.size(); i++) {

                if (columnTemplate.get(i).equals(selectedX)) {
                    xIndexes = i;
                }
            }


            double[] x = new double[statisticsValues.size()];

            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                String xvalues = value.getValues(rowInd, xIndexes);
                if (xvalues != null) {
                    x[rowInd - 1] = Double.valueOf(xvalues);
                    rowInd++;
                }
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(4);
            String[] propertyNames = new String[]{
                    " t-statistic associated with a one-sample t-test comparing the mean", " p-value associated with the null hypothesis", "test using a fixed significance level"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }

            properties.get(0).add(String.valueOf(TestUtils.t(Double.valueOf(muValue), x)));
            properties.get(1).add(String.valueOf(TestUtils.tTest(Double.valueOf(muValue), x)));
            properties.get(2).add(String.valueOf(TestUtils.tTest(Double.valueOf(muValue), x, Double.valueOf(alpha))));

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void twotest() {
        try {
            int xIndexes = 0;
            int yIndexes = 0;

            for (int i = 0; i < columnTemplate.size(); i++) {

                if (columnTemplate.get(i).equals(selectedX)) {
                    xIndexes = i;
                }
                if (columnTemplate.get(i).equals(selectedY)) {
                    yIndexes = i;
                }
            }


            double[] x = new double[statisticsValues.size()];
            double[] y = new double[statisticsValues.size()];


            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                String xvalues = value.getValues(rowInd, xIndexes);
                String yvalues = value.getValues(rowInd, yIndexes);
                if (xvalues != null && yvalues != null) {
                    x[rowInd - 1] = Double.valueOf(xvalues);
                    y[rowInd - 1] = Double.valueOf(yvalues);
                    rowInd++;
                }
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(4);
            String[] propertyNames = new String[]{
                    "t-statistic", " p-value  t-statistic", "t-test using a fixed significance level"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }

            properties.get(0).add(String.valueOf(TestUtils.pairedT(x, y)));
            properties.get(1).add(String.valueOf(TestUtils.pairedTTest(x, y)));
            properties.get(2).add(String.valueOf(TestUtils.pairedTTest(x, y, Double.valueOf(alpha))));

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void chiSquare() {
        try {
            int xIndexes = 0;
            int yIndexes = 0;

            for (int i = 0; i < columnTemplate.size(); i++) {

                if (columnTemplate.get(i).equals(selectedX)) {
                    xIndexes = i;
                }
                if (columnTemplate.get(i).equals(selectedY)) {
                    yIndexes = i;
                }
            }


            double[] x = new double[statisticsValues.size()];
            long[] y = new long[statisticsValues.size()];


            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                String xvalues = value.getValues(rowInd, xIndexes);
                String yvalues = value.getValues(rowInd, yIndexes);
                if (xvalues != null && yvalues != null) {
                    x[rowInd - 1] = Double.valueOf(xvalues);
                    y[rowInd - 1] = Double.valueOf(yvalues).longValue();
                    rowInd++;
                }
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(4);
            String[] propertyNames = new String[]{
                    "chi-square ", " p-value  chi-square ", "chi-square  using a fixed significance level"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }

            properties.get(0).add(String.valueOf(TestUtils.chiSquare(x, y)));
            properties.get(1).add(String.valueOf(TestUtils.chiSquareTest(x, y)));
            properties.get(2).add(String.valueOf(TestUtils.chiSquareTest(x, y, Double.valueOf(alpha))));

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void gtest() {
        try {
            int xIndexes = 0;
            int yIndexes = 0;

            for (int i = 0; i < columnTemplate.size(); i++) {

                if (columnTemplate.get(i).equals(selectedX)) {
                    xIndexes = i;
                }
                if (columnTemplate.get(i).equals(selectedY)) {
                    yIndexes = i;
                }
            }


            double[] x = new double[statisticsValues.size()];
            long[] y = new long[statisticsValues.size()];


            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                String xvalues = value.getValues(rowInd, xIndexes);
                String yvalues = value.getValues(rowInd, yIndexes);
                if (xvalues != null && yvalues != null) {
                    x[rowInd - 1] = Double.valueOf(xvalues);
                    y[rowInd - 1] = Double.valueOf(yvalues).longValue();
                    rowInd++;
                }
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(4);
            String[] propertyNames = new String[]{
                    "chi-square ", " p-value  chi-square ", "chi-square  using a fixed significance level"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }

            properties.get(0).add(String.valueOf(TestUtils.g(x, y)));
            properties.get(1).add(String.valueOf(TestUtils.gTest(x, y)));
            properties.get(2).add(String.valueOf(TestUtils.gTest(x, y, Double.valueOf(alpha))));

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void simpleRegression() {
        try {
            List<SimpleRegression> decList = new LinkedList<>();

            int dependant = 0;
            int independant = 0;
            for (int i = 0; i < columnTemplate.size(); i++) {
                if (columnTemplate.get(i).equals(selectedInDep)) {
                    independant = i;
                }
                if (columnTemplate.get(i).equals(selectedDep)) {
                    dependant = i;
                }
            }

            int j = 1;
            decList.add(new SimpleRegression());
            for (DataValue value : statisticsValues) {

                String ind = value.getValues(j, independant);
                String dep = value.getValues(j, dependant);
                if (ind != null && dep != null)
                    decList.get(0).addData(Double.valueOf(ind), Double.valueOf(dep));

                j++;
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "squareError", "intercept", "interceptStdErr", "sumOfCrossProducts", "sumSquaredErrors", "n", "slopeStdErr", "slope", "rSquare", "slopeConfidenceInterval", "totalSumSquares"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }
            for (int i = 0; i < decList.size(); i++) {

                SimpleRegression simpleRegression = decList.get(i);
                double squareError = simpleRegression.getMeanSquareError();
                properties.get(0).add(String.valueOf(squareError));
                double intercept = simpleRegression.getIntercept();
                properties.get(1).add(String.valueOf(intercept));
                double interceptStdErr = simpleRegression.getInterceptStdErr();
                properties.get(2).add(String.valueOf(interceptStdErr));
                double sumOfCrossProducts = simpleRegression.getSumOfCrossProducts();
                properties.get(3).add(String.valueOf(sumOfCrossProducts));
                double sumSquaredErrors = simpleRegression.getSumSquaredErrors();
                properties.get(4).add(String.valueOf(sumSquaredErrors));
                double n = simpleRegression.getN();
                properties.get(5).add(String.valueOf(n));
                double slopeStdErr = simpleRegression.getSlopeStdErr();
                properties.get(6).add(String.valueOf(slopeStdErr));
                double slope = simpleRegression.getSlope();
                properties.get(7).add(String.valueOf(slope));
                double rSquare = simpleRegression.getRSquare();
                properties.get(8).add(String.valueOf(rSquare));
                double slopeConfidenceInterval = simpleRegression.getSlopeConfidenceInterval();
                properties.get(9).add(String.valueOf(slopeConfidenceInterval));
                double totalSumSquares = simpleRegression.getTotalSumSquares();
                properties.get(10).add(String.valueOf(totalSumSquares));


            }

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void multipleRegressionOls() {
        try {
            List<OLSMultipleLinearRegression> decList = new LinkedList<OLSMultipleLinearRegression>();

            int dependant = 0;
            ArrayList<Integer> independant = new ArrayList<>();
            for (int i = 0; i < columnTemplate.size(); i++) {
                for (int ind = 0; ind < selectedIndepVars.length; ind++) {
                    if (columnTemplate.get(i).equals(selectedIndepVars[ind])) {
                        independant.add(i);
                    }
                }

                if (columnTemplate.get(i).equals(selectedDepVars)) {
                    dependant = i;
                }
            }

            int j = 1;
            decList.add(new OLSMultipleLinearRegression());

            double[] x = new double[statisticsValues.size()];
            double[][] y = new double[statisticsValues.size()][independant.size()];
            for (DataValue value : statisticsValues) {
                x[j - 1] = Double.valueOf(value.getValues(j, dependant));

                for (int i = 0; i < independant.size(); i++) {

                    y[j - 1][i] = Double.valueOf(value.getValues(j, independant.get(i)));
                }
                j++;
            }
            decList.get(0).newSampleData(x, y);

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "estimateRegressionStandardError", "estimateErrorVariance",
                    "estimateRegressandVariance", "calculateResidualSumOfSquares",
                    "calculateTotalSumOfSquares", "calculateAdjustedRSquared", "calculateRSquared", "estimateRegressionParameters"
                    , "estimateRegressionParametersVariance", "estimateResiduals"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }
            for (int i = 0; i < decList.size(); i++) {

                OLSMultipleLinearRegression simpleRegression = decList.get(i);
                double estimateRegressionStandardError = simpleRegression.estimateRegressionStandardError();
                properties.get(0).add(String.valueOf(estimateRegressionStandardError));
                double estimateErrorVariance = simpleRegression.estimateErrorVariance();
                properties.get(1).add(String.valueOf(estimateErrorVariance));
                double estimateRegressandVariance = simpleRegression.estimateRegressandVariance();
                properties.get(2).add(String.valueOf(estimateRegressandVariance));
                double calculateResidualSumOfSquares = simpleRegression.calculateResidualSumOfSquares();
                properties.get(3).add(String.valueOf(calculateResidualSumOfSquares));
                double calculateTotalSumOfSquares = simpleRegression.calculateTotalSumOfSquares();
                properties.get(4).add(String.valueOf(calculateTotalSumOfSquares));
                double calculateAdjustedRSquared = simpleRegression.calculateAdjustedRSquared();
                properties.get(5).add(String.valueOf(calculateAdjustedRSquared));
                double calculateRSquared = simpleRegression.calculateRSquared();
                properties.get(6).add(String.valueOf(calculateRSquared));
                String estimateRegressionParameters = Arrays.toString(simpleRegression.estimateRegressionParameters());
                properties.get(7).add(String.valueOf(estimateRegressionParameters));
                String estimateRegressionParametersVariance = convertArrayToString(simpleRegression.estimateRegressionParametersVariance());
                properties.get(8).add(String.valueOf(estimateRegressionParametersVariance));
                String estimateResiduals = Arrays.toString(simpleRegression.estimateResiduals());
                properties.get(9).add(String.valueOf(estimateResiduals));


            }

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }


    public void multipleRegressionGls() {
        try {
            List<GLSMultipleLinearRegression> decList = new LinkedList<>();

            int dependant = 0;
            ArrayList<Integer> independant = new ArrayList<>();
            ArrayList<Integer> observations = new ArrayList<>();
            for (int i = 0; i < columnTemplate.size(); i++) {
                for (int ind = 0; ind < selectedIndepVars.length; ind++) {
                    if (columnTemplate.get(i).equals(selectedIndepVars[ind])) {
                        independant.add(i);
                    }
                }
                for (int ind = 0; ind < getSelectedObsVars().length; ind++) {
                    if (columnTemplate.get(i).equals(getSelectedObsVars()[ind])) {
                        observations.add(i);
                    }
                }
                if (columnTemplate.get(i).equals(selectedDepVars)) {
                    dependant = i;
                }
            }


            decList.add(new GLSMultipleLinearRegression());

            double[] x = new double[statisticsValues.size()];
            double[][] y = new double[statisticsValues.size()][independant.size()];
            double[][] o = new double[statisticsValues.size()][observations.size()];
            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                x[rowInd - 1] = Double.valueOf(value.getValues(rowInd, dependant));
                for (int i = 0; i < independant.size(); i++) {

                    String values = value.getValues(rowInd, independant.get(i));
                    if (values == null) {
                        double[] smallerY = new double[y[i].length - 1];
                        System.arraycopy(y[i], 0, smallerY, 0, rowInd - 1);
                        y[i] = smallerY;
                    } else {
                        y[rowInd - 1][i] = Double.valueOf(values);
                    }
                }
                for (int i = 0; i < observations.size(); i++) {

                    o[rowInd - 1][i] = Double.valueOf(value.getValues(rowInd, observations.get(i)));
                }
                rowInd++;
            }
            decList.get(0).newSampleData(x, y, o);

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "estimateRegressionStandardError", "estimateErrorVariance",
                    "estimateRegressandVariance", "estimateRegressionParametersVariance",
                    "estimateRegressionParametersStandardErrors", "estimateResiduals", "estimateRegressionParameters"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }
            for (int i = 0; i < decList.size(); i++) {

                GLSMultipleLinearRegression simpleRegression = decList.get(i);
                double estimateRegressionStandardError = simpleRegression.estimateRegressionStandardError();
                properties.get(0).add(String.valueOf(estimateRegressionStandardError));
                double estimateErrorVariance = simpleRegression.estimateErrorVariance();
                properties.get(1).add(String.valueOf(estimateErrorVariance));
                double estimateRegressandVariance = simpleRegression.estimateRegressandVariance();
                properties.get(2).add(String.valueOf(estimateRegressandVariance));
                String estimateRegressionParametersVariance = convertArrayToString(simpleRegression.estimateRegressionParametersVariance());
                properties.get(3).add(String.valueOf(estimateRegressionParametersVariance));
                String estimateRegressionParametersStandardErrors = Arrays.toString(simpleRegression.estimateRegressionParametersStandardErrors());
                properties.get(4).add(String.valueOf(estimateRegressionParametersStandardErrors));
                String estimateResiduals = Arrays.toString(simpleRegression.estimateResiduals());
                properties.get(5).add(String.valueOf(estimateResiduals));
                String estimateRegressionParameters = Arrays.toString(simpleRegression.estimateRegressionParameters());
                properties.get(6).add(String.valueOf(estimateRegressionParameters));
            }

            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public static double[] toDoubleArray(Double[] arr) {
        double[] tempArray = new double[arr.length];
        int i = 0;
        for (Double d : arr) {
            tempArray[i] = d.doubleValue();
            i++;
        }
        return tempArray;
    }

    public static LinkedList<String> toStringRepr(double[] arr) {
        LinkedList<String> tempArray = new LinkedList<String>();
        int i = 0;
        for (Double d : arr) {
            tempArray.add(d.toString());
            i++;
        }
        return tempArray;
    }

    public void rankTransform() {
        try {
            LinkedList<LinkedList<Double>> decList = new LinkedList<>();
            int j = 1;
            for (DataValue value : statisticsValues) {
                for (int i = 0; i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j).size(); i++) {
                    decList.add(new LinkedList<Double>());
                }
                for (int i = 0; i < value.getValues().get(j).size(); i++) {
                    String values = value.getValues(j, i);
                    if (values != null)
                        decList.get(i).add(Double.valueOf(values));
                }
                j++;
            }
            List<List<String>> properties = new ArrayList<>(columnTemplate.size());
            for (int i = 0; i < decList.size(); i++) {

                LinkedList<Double> deccriptor = decList.get(i);
                Double[] ts = deccriptor.toArray(new Double[deccriptor.size()]);
                properties.add(toStringRepr(new NaturalRanking(NaNStrategy.valueOf(nanStrategy), TiesStrategy.valueOf(tieStrategy)).rank(toDoubleArray(ts))));

            }

            dataValues.clear();
            columns.clear();


            Map<Integer, List<String>> values = new HashMap<>();
            int maxSize = 0;
            for (int c = 0; c < properties.size(); c++) {
                if (properties.get(c).size() > maxSize)
                    maxSize = properties.get(c).size();
            }
            for (int c = 1; c <= properties.size(); c++) {
                if (properties.get(c - 1).size() < maxSize) {
                    for (int p = properties.get(c - 1).size(); p < maxSize; p++) {
                        properties.get(c - 1).add(null);
                    }
                }

            }
            properties = transpose(properties);
            for (int c = 1; c <= properties.size(); c++) {

                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    static <T> List<List<T>> transpose(List<List<T>> table) {
        List<List<T>> ret = new ArrayList<List<T>>();
        final int N = table.get(0).size();
        for (int i = 0; i < N; i++) {
            List<T> col = new ArrayList<T>();
            for (List<T> row : table) {
                col.add(row.get(i));
            }
            ret.add(col);
        }
        return ret;
    }

    public void anova() {
        try {
            ArrayList<Integer> independant = new ArrayList<>();
            for (int i = 0; i < columnTemplate.size(); i++) {
                for (int ind = 0; ind < testsData.length; ind++) {
                    if (columnTemplate.get(i).equals(testsData[ind])) {
                        independant.add(i);
                    }
                }

            }

            int j = 1;

            List<List<Object>> y = new LinkedList<>();
            for (DataValue value : statisticsValues) {
                y.add(new LinkedList<>());
                for (int i = 0; i < independant.size(); i++) {

                    String values = value.getValues(j, independant.get(i));
                    if (values != null)
                        y.get(j - 1).add(Double.valueOf(values));
                }
                j++;
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(4);
            String[] propertyNames = new String[]{
                    " ANOVA F-values", " ANOVA  p-values",
                    "One-Way ANOVA test with significance level "
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }

            List<List<Object>> transposed = transpose(y);
            List<double[]> classes = new ArrayList<>();
            for (List<Object> var : transposed) {
                double[] array = new double[var.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = ((Double) var.get(i)).doubleValue();
                }
                classes.add(array);
            }
            double fStatistic = TestUtils.oneWayAnovaFValue(classes);
            properties.get(0).add(String.valueOf(fStatistic));

            double pValue = TestUtils.oneWayAnovaPValue(classes);
            properties.get(1).add(String.valueOf(pValue));


            double signifValue = TestUtils.oneWayAnovaPValue(classes);
            properties.get(2).add(String.valueOf(signifValue));
            TestUtils.oneWayAnovaTest(classes, Double.valueOf(alpha));


            Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public void covariance() {
        try {
            List<Covariance> decList = new LinkedList<Covariance>();

            ArrayList<Integer> independant = new ArrayList<>();
            for (int i = 0; i < columnTemplate.size(); i++) {
                for (int ind = 0; ind < matrixDataCov.length; ind++) {
                    if (columnTemplate.get(i).equals(matrixDataCov[ind])) {
                        independant.add(i);
                    }
                }

            }

            int j = 1;
            decList.add(new Covariance());

            double[][] y = new double[statisticsValues.size()][independant.size()];
            for (DataValue value : statisticsValues) {

                for (int i = 0; i < independant.size(); i++) {

                    String values = value.getValues(j, independant.get(i));
                    if (values != null)
                        y[j - 1][i] = Double.valueOf(values);
                }
                j++;
            }

            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "covarianceMatrix", "pearsonsCorrelation",
                    "pearsonsCorrelationMatrix", "pearsonsCorrelationPValues",
                    "pearsonsCorrelationStandardErrors"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }
            for (int i = 0; i < decList.size(); i++) {


                String covarianceMatrix = convertArrayToString(new Covariance(y).getCovarianceMatrix().getData());
                properties.get(0).add(String.valueOf(covarianceMatrix));
                PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(y);
                String pearsonsCorrelationMatrix = convertArrayToString(pearsonsCorrelation.getCorrelationMatrix().getData());
                properties.get(1).add(String.valueOf(pearsonsCorrelationMatrix));
                String pearsonsCorrelationPValues = convertArrayToString(pearsonsCorrelation.getCorrelationPValues().getData());
                properties.get(2).add(String.valueOf(pearsonsCorrelationPValues));
                String pearsonsCorrelationStandardErrors = convertArrayToString(pearsonsCorrelation.getCorrelationStandardErrors().getData());
                properties.get(3).add(String.valueOf(pearsonsCorrelationStandardErrors));

            }

            Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }

    public String convertArrayToString(double[][] array) {
        StringBuilder sb = new StringBuilder();
        for (double[] s1 : array) {
            sb.append(Arrays.toString(s1)).append('\n');
        }
        return sb.toString();
    }

    public void pearsonsCorrelation() {
        try {
            List<Covariance> decList = new LinkedList<>();

            int dependant = 0;
            int independant = 0;

            for (int i = 0; i < columnTemplate.size(); i++) {

                if (columnTemplate.get(i).equals(selectedX)) {
                    dependant = i;
                }
                if (columnTemplate.get(i).equals(selectedY)) {
                    independant = i;
                }
            }


            decList.add(new Covariance());

            double[] x = new double[statisticsValues.size()];
            double[] y = new double[statisticsValues.size()];
            int rowInd = 1;
            for (DataValue value : statisticsValues) {
                String xvalues = value.getValues(rowInd, dependant);
                if (xvalues != null) {
                    x[rowInd - 1] = Double.valueOf(xvalues);
                    String valuesy = value.getValues(rowInd, independant);
                    if (valuesy != null)
                        y[rowInd - 1] = Double.valueOf(valuesy);

                    rowInd++;
                }
            }
            double covariance = decList.get(0).covariance(x, y);
            double covarianceWithoutBias = decList.get(0).covariance(x, y, false);
            double covarianceWithBias = decList.get(0).covariance(x, y, true);
            dataValues.clear();
            columns.clear();
            columnTemplate.clear();
            columnTemplate = new ArrayList<>(Arrays.asList("Result"));
            columnTemplate.add(0, "Property");
            List<List<String>> properties = new ArrayList<>(decList.size());
            String[] propertyNames = new String[]{
                    "Covariance Matrix", "covariance", "covarianceWithoutBias", "covarianceWithBias",
                    "PearsonsCorrelation", "SpearmansCorrelation", "KendallsCorrelation"
            };
            for (int i = 0; i < propertyNames.length; i++) {
                properties.add(new ArrayList<>());
                properties.get(i).add(propertyNames[i]);
            }
            for (int i = 0; i < decList.size(); i++) {

                Covariance simpleRegression = decList.get(i);


            }
            properties.get(1).add(String.valueOf(covariance));
            properties.get(2).add(String.valueOf(covarianceWithoutBias));
            properties.get(3).add(String.valueOf(covarianceWithBias));
            properties.get(4).add(String.valueOf(new PearsonsCorrelation().correlation(x, y)));
            properties.get(5).add(String.valueOf(new SpearmansCorrelation().correlation(x, y)));
            properties.get(6).add(String.valueOf(new KendallsCorrelation().correlation(x, y)));
            Map<Integer, List<String>> values = new HashMap<>();

            for (int c = 1; c <= properties.size(); c++) {
                values.put(c, properties.get(c - 1));
                dataValues.add(new DataValue(c, values));
            }
            createDynamicColumns();
            nullifyAll();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
        }
    }


    public ArrayList<String> getColumnTemplate() {
        return columnTemplate;
    }

    public void setColumnTemplate(ArrayList<String> columnTemplate) {
        this.columnTemplate = columnTemplate;
    }

    public String[] getSelectedIndepVars() {
        return selectedIndepVars;
    }

    public void setSelectedIndepVars(String[] selectedIndepVars) {
        this.selectedIndepVars = selectedIndepVars;
    }

    public String getSelectedDepVars() {
        return selectedDepVars;
    }

    public void setSelectedDepVars(String selectedDepVars) {
        this.selectedDepVars = selectedDepVars;
    }

    public String getSelectedInDep() {
        return selectedInDep;
    }

    public void setSelectedInDep(String selectedInDep) {
        this.selectedInDep = selectedInDep;
    }

    public String getSelectedDep() {
        return selectedDep;
    }

    public void setSelectedDep(String selectedDep) {
        this.selectedDep = selectedDep;
    }

    public String[] getSelectedObsVars() {
        return selectedObsVars;
    }

    public void setSelectedObsVars(String[] selectedObsVars) {
        this.selectedObsVars = selectedObsVars;
    }

    public void setSelectedX(String selectedX) {
        this.selectedX = selectedX;
    }

    public String getSelectedX() {
        return selectedX;
    }

    public void setSelectedY(String selectedY) {
        this.selectedY = selectedY;
    }

    public String getSelectedY() {
        return selectedY;
    }

    public String getNanStrategy() {
        return null;
    }

    public String getTieStrategy() {
        return null;
    }

    public void setNanStrategy(String nanStrategy) {
        this.nanStrategy = nanStrategy;
    }

    public void setTieStrategy(String tieStrategy) {
        this.tieStrategy = tieStrategy;
    }

    public String[] getMatrixDataCov() {
        return matrixDataCov;
    }

    public void setMatrixDataCov(String[] matrixDataCov) {
        this.matrixDataCov = matrixDataCov;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    public void setMuValue(String muValue) {
        this.muValue = muValue;
    }

    public String getMuValue() {
        return muValue;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }

    public String getAlpha() {
        return alpha;
    }
}