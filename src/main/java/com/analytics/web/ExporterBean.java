package com.analytics.web;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.nz.simplecrud.controller.LoginController;
import com.nz.simplecrud.entity.*;
import com.nz.simplecrud.service.UserService;
import multipleLinearRegression.Dependent;
import multipleLinearRegression.Feature;
import org.apache.commons.io.FileUtils;
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
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.*;

import com.monitorjbl.xlsx.StreamingReader;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

@ManagedBean(name = "bBean")
@SessionScoped
public class ExporterBean implements Serializable {
  @Inject
  LoginController loginController;

  public com.nz.simplecrud.entity.File getFiles() {
    return files;
  }

  public void setFiles(final com.nz.simplecrud.entity.File files) {
    this.files = files;
  }

  com.nz.simplecrud.entity.File files;

  public void uploadSelectedFile() {
    final File[] fileToUpload = new File[1];
    Paths.get("filestorage").forEach(path -> {
          if (path.toFile().getName().equals(files.getFilename
              ())) {
            fileToUpload[0] = path.toFile();
          }
        }

    );
    try {
      FileInputStream fileInputStream = new FileInputStream(fileToUpload[0]);
      String fileName = files.getFilename();
      String format = fileName.substring(fileName.lastIndexOf("."), fileName.length());
      uploadFileFromeDiskAndStore(FacesContext.getCurrentInstance(), fileName, format,
          fileInputStream);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private ArrayList<String> columnTemplate = new ArrayList<String>(Arrays.asList("id"));

  private ArrayList<String> statisticsColumnTemplate = new ArrayList<String>();

  private List<ColumnModel> columns = new ArrayList<ColumnModel>();

  private List<DataValue> dataValues = new ArrayList<>();

  private List<DataValue> statisticsValues = new ArrayList<DataValue>();

  private List<DataValue> originalValues = new ArrayList<DataValue>();

  private List<DataValue> filteredDataValues;

  private String selectedDepVars;

  private String[] selectedIndepVars;

  private String[] selectedObsVars;

  private
  @Inject
  UserService das;

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

  private String muValue = "0.4";

  private String alpha = "0.4";

  private String tableHeader = "Data values";

  private String selectedXTTEST;

  private String selectedDepVarsOLS;

  private String[] selectedIndepVarsOLS;

  private String selectedXCovar;

  private String selectedYCovar;

  private String selectedXGTest;

  private String selectedYGTest;

  private String selectedXChi;

  private String selectedYChi;

  private String selectedXTwoT;

  private String selectedYTwoT;

  private String selectedXKolmogorov;

  private String selectedYKolmogorov;

  private String methodReg;

  private String[] methodsArray = {"Back Propogation AdjRSquared",
      "Forward Propogation AdjRSquared",
      "Back Propogation RSS", "Forward Propogation RSS",
      "Back Propogation NFeatures", "Forward Propogation NFeatures",
  };

  private String selectedDepVarMultipleReg;

  private String[] selectedInDepVarMultipleReg;

  private String selectedMapping;

  private String keyMapped;

  private String valueMapped;

  private String output;

  private List<String> mapOfValues = new LinkedList<>();

  private String description;

  private BarChartModel barModel;

  public Map<Integer, Map<String, String>> getMapOfColumns() {
    return mapOfColumns;
  }

  public void setMapOfColumns(Map<Integer, Map<String, String>> mapOfColumns) {
    this.mapOfColumns = mapOfColumns;
  }

  private Map<Integer, Map<String, String>> mapOfColumns = new HashMap<>();

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  private String content;

  public List<String> getMapOfValues() {
    return mapOfValues;
  }

  public void setMapOfValues(List<String> mapOfValues) {
    this.mapOfValues = mapOfValues;
  }

  public void mapVariables() {
    try {

      int dependant = 0;

      for (int i = 0; i < columnTemplate.size(); i++) {

        if (columnTemplate.get(i).equals(selectedMapping)) {
          dependant = i;
        }
      }
      mapOfValues.add("Column:" + columnTemplate.get(dependant) + ", key:" + keyMapped + ", value:"
          + valueMapped);
      if (mapOfColumns.get(dependant) == null) {
        HashMap<String, String> value = new HashMap<>();
        value.put(valueMapped, keyMapped);
        mapOfColumns.put(dependant, value);
      } else {
        mapOfColumns.get(dependant).put(valueMapped, keyMapped);
      }
      String[] x = new String[statisticsValues.size()];
      int rowInd = 1;
      for (DataValue value : statisticsValues) {
        String xvalues = value.getValues(rowInd, dependant);
        if (xvalues != null) {
          x[rowInd - 1] = xvalues.equals(keyMapped) ? valueMapped : xvalues;

          rowInd++;
        }
      }
      tableHeader = "Data values";
      dataValues.clear();
      columns.clear();
      columnTemplate.clear();

      for (int c = 0; c < statisticsValues.size(); c++) {
        statisticsValues.get(c).setValues(c + 1, dependant, x[c]);
      }

      dataValues = new ArrayList<>(statisticsValues);
      columnTemplate = new ArrayList<>(statisticsColumnTemplate);
      createDynamicColumns();
      nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public String getValueMapped() {
    return valueMapped;
  }

  public void setValueMapped(String valueMapped) {
    this.valueMapped = valueMapped;
  }

  public String getKeyMapped() {
    return keyMapped;
  }

  public void setKeyMapped(String keyMapped) {
    this.keyMapped = keyMapped;
  }

  public String getSelectedMapping() {
    return selectedMapping;
  }

  public void setSelectedMapping(String selectedMapping) {
    this.selectedMapping = selectedMapping;
  }

  @PostConstruct
  public void init() {
    //    try {
    //      files = new LinkedList<>();
    //      java.nio.file.Files.walk(Paths.get("filestorage")).forEach(filePath -> {
    //        if (java.nio.file.Files.isRegularFile(filePath)) {
    //          if (filePath != null) {
    //            files.add(filePath.toFile());
    //          }
    //        }
    //      });
    //    } catch (IOException e) {
    //      e.printStackTrace();
    //    }
  }

  public void nullifyAll() {
    //        this.filteredDataValues = null;
    //        this.selectedDepVars = null;
    //        this.selectedIndepVars = null;
    //        this.selectedObsVars = null;
    //        this.selectedDep = null;
    //        this.selectedInDep = null;
    //        this.selectedX = null;
    //        this.selectedY = null;
    //        this.tieStrategy = null;
    //        this.nanStrategy = null;
    //        this.matrixDataCov = null;
    //        this.testsData = null;
    //        this.operation = null;
    //        this.varName = null;
    //        this.muValue = null;
    //        this.alpha = null;
    this.output = "";
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
      if (columnKey != null) {
        String key = columnKey.trim();

        columns.add(new ColumnModel(columnKey, columnKey, i));
        i++;
      }
    }
  }

  public void updateColumns() {
    //reset table state
    UIComponent table = FacesContext.getCurrentInstance().getViewRoot()
        .findComponent(":form:dataValues");
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

  public void setTableHeader(String tableHeader) {
    this.tableHeader = tableHeader;
  }

  public String getTableHeader() {
    return tableHeader;
  }

  public void setSelectedXTTEST(String selectedXTTEST) {
    this.selectedXTTEST = selectedXTTEST;
  }

  public String getSelectedXTTEST() {
    return selectedXTTEST;
  }

  public void setSelectedDepVarsOLS(String selectedDepVarsOLS) {
    this.selectedDepVarsOLS = selectedDepVarsOLS;
  }

  public String getSelectedDepVarsOLS() {
    return selectedDepVarsOLS;
  }

  public void setSelectedIndepVarsOLS(String[] selectedIndepVarsOLS) {
    this.selectedIndepVarsOLS = selectedIndepVarsOLS;
  }

  public String[] getSelectedIndepVarsOLS() {
    return selectedIndepVarsOLS;
  }

  public void setSelectedXCovar(String selectedXCovar) {
    this.selectedXCovar = selectedXCovar;
  }

  public String getSelectedXCovar() {
    return selectedXCovar;
  }

  public void setSelectedYCovar(String selectedYCovar) {
    this.selectedYCovar = selectedYCovar;
  }

  public String getSelectedYCovar() {
    return selectedYCovar;
  }

  public void setSelectedXGTest(String selectedXGTest) {
    this.selectedXGTest = selectedXGTest;
  }

  public String getSelectedXGTest() {
    return selectedXGTest;
  }

  public void setSelectedYGTest(String selectedYGTest) {
    this.selectedYGTest = selectedYGTest;
  }

  public String getSelectedYGTest() {
    return selectedYGTest;
  }

  public void setSelectedXChi(String selectedXChi) {
    this.selectedXChi = selectedXChi;
  }

  public String getSelectedXChi() {
    return selectedXChi;
  }

  public void setSelectedYChi(String selectedYChi) {
    this.selectedYChi = selectedYChi;
  }

  public String getSelectedYChi() {
    return selectedYChi;
  }

  public void setSelectedXTwoT(String selectedXTwoT) {
    this.selectedXTwoT = selectedXTwoT;
  }

  public String getSelectedXTwoT() {
    return selectedXTwoT;
  }

  public void setSelectedYTwoT(String selectedYTwoT) {
    this.selectedYTwoT = selectedYTwoT;
  }

  public String getSelectedYTwoT() {
    return selectedYTwoT;
  }

  public void setSelectedXKolmogorov(String selectedXKolmogorov) {
    this.selectedXKolmogorov = selectedXKolmogorov;
  }

  public String getSelectedXKolmogorov() {
    return selectedXKolmogorov;
  }

  public void setSelectedYKolmogorov(String selectedYKolmogorov) {
    this.selectedYKolmogorov = selectedYKolmogorov;
  }

  public String getSelectedYKolmogorov() {
    return selectedYKolmogorov;
  }

  public void setMethodReg(String methodReg) {
    this.methodReg = methodReg;
  }

  public String getMethodReg() {
    return methodReg;
  }

  public void setMethodsArray(String[] methodsArray) {
    this.methodsArray = methodsArray;
  }

  public String[] getMethodsArray() {
    return methodsArray;
  }

  public void setSelectedDepVarMultipleReg(String selectedDepVarMultipleReg) {
    this.selectedDepVarMultipleReg = selectedDepVarMultipleReg;
  }

  public String getSelectedDepVarMultipleReg() {
    return selectedDepVarMultipleReg;
  }

  public void setSelectedInDepVarMultipleReg(String[] selectedInDepVarMultipleReg) {
    this.selectedInDepVarMultipleReg = selectedInDepVarMultipleReg;
  }

  public String[] getSelectedInDepVarMultipleReg() {
    return selectedInDepVarMultipleReg;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public String getOutput() {
    return output;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setBarModel(BarChartModel barModel) {
    this.barModel = barModel;
  }

  public BarChartModel getBarModel() {
    return barModel;
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
        facesContext
            .addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "File is null", null));
      }
      String fileName = file1.getFileName();
      String format = fileName.substring(fileName.lastIndexOf("."), fileName.length());
      saveUploadedFileToDb(fileName);
      InputStream inputStream;
      inputStream = file1.getInputstream();
      uploadFileFromeDiskAndStore(facesContext, fileName, format, inputStream);
    } catch (Exception e) {
      e.printStackTrace();
      nullifyAll();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  private void uploadFileFromeDiskAndStore(final FacesContext facesContext, final String fileName,
      final String format, final InputStream inputStream) {
    if (format.equals(".xlsx")) {

      StreamingReader reader = null;
      try {

        FileUtils.forceMkdir(new File("filestorage"));

        File file = new File("filestorage\\" + fileName);
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(inputStream, out);
        inputStream.close();
        out.close();
        InputStream is = new FileInputStream(file);
        //create destination File
        //                  saveFileToUserFolder(is,fileName);
        is = new FileInputStream(file);
        reader = StreamingReader.builder()
            .rowCacheSize(500)    // number of rows to keep in memory (defaults to 10)
            .bufferSize(
                8096)     // buffer size to use when reading InputStream to file (defaults to 1024)
            .sheetIndex(0)        // index of sheet to use (defaults to 0)
            .read(is);            // InputStream or File for XLSX file (required)
      } catch (IOException e) {
        facesContext
            .addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error reading file" + e, null));
      }

      Iterator<Row> rowIterator = reader.iterator();
      int i = 0;
      dataValues.clear();
      columnTemplate.clear();
      barModel = null;
      columns.clear();
      while (rowIterator.hasNext()) {
        Row row = rowIterator.next();

        Iterator<Cell> cellIterator = row.cellIterator();
        ArrayList<String> cells = new ArrayList<>();

        while (cellIterator.hasNext()) {
          Cell cell = cellIterator.next();

          if (i != 0) {
            if (cells.size() < cell.getColumnIndex()) {
              for (int j = cells.size(); j < cell.getColumnIndex(); j++) {
                cells.add(null);
              }
            }
            cells.add(cell.getColumnIndex(), "" + cell.getStringCellValue());
          } else {
            cells.add("id");
          }

          if (i == 0) {

            columnTemplate.add(cell.getStringCellValue());
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
        File file = new File(fileName);
        OutputStream out = new FileOutputStream(file);

        IOUtils.copy(inputStream, out);
        inputStream.close();
        out.close();
        listReader = new CsvListReader(new FileReader(file), CsvPreference.TAB_PREFERENCE);

        String[] listReaderHeader = listReader
            .getHeader(false); // skip the header (can't be used with CsvListReader)
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
    tableHeader = "Data values";
    mapOfColumns.clear();
    mapOfValues.clear();
  }

  private void saveUploadedFileToDb(final String fileName) {
    com.nz.simplecrud.entity.File dbFile = new com.nz.simplecrud.entity.File();
    dbFile.setFilename(fileName);
    dbFile.setUserTables(Arrays.asList(loginController.getLoggedUser()));
    boolean isFileStored = false;
    if (loginController.getLoggedUser().getFiles() != null) {
      for (com.nz.simplecrud.entity.File file : loginController.getLoggedUser().getFiles()) {
        if (file.getFilename().equals(fileName)) {
          isFileStored = true;
        }
      }
    }
    if (!isFileStored) {
      loginController.getLoggedUser().getFiles().add(dbFile);
      das.update(loginController.getLoggedUser());
      loginController.setLoggedUser(das.find(loginController.getLoggedUser().getId()));
    }
  }

  private List<String> getListOfFiles() {

    return null;
  }

  private void saveFileToUserFolder(final InputStream inputStream, final String fileName) {
    //create destination File
    File destFile = new File("\\filestorage\\" + fileName);
    try {
      FileUtils.forceMkdir(new File("filestorage"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    //use org.apache.commons.io.FileUtils to copy the File
    try {
      //      FileUtils.copyInputStreamToFile(inputStream, destFile);
      OutputStream out = new FileOutputStream(destFile);
      byte[] buf = new byte[1024];
      int len;
      while ((len = inputStream.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
      out.close();
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void original() {
    try {
      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      dataValues = new ArrayList<>(statisticsValues);
      columnTemplate = new ArrayList<>(statisticsColumnTemplate);
      tableHeader = "Data values";
      createDynamicColumns();
      nullifyAll();
      description = "";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
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
          if (valuesy != null) {
            y[rowInd - 1] = Double.valueOf(valuesy);
          }

          rowInd++;
        }
      }
      tableHeader = "Data values";
      dataValues.clear();
      columns.clear();
      columnTemplate.clear();

      statisticsColumnTemplate.add(varName);
      for (int c = 0; c < statisticsValues.size(); c++) {
        if (operation.equals("add")) {
          statisticsValues
              .set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] + y[c], c + 1)));
        } else if (operation.equals("multiply")) {
          statisticsValues
              .set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] * y[c], c + 1)));
        } else if (operation.equals("divide")) {
          statisticsValues
              .set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] / y[c], c + 1)));
        } else if (operation.equals("extract")) {
          statisticsValues
              .set(c, new DataValue(c, statisticsValues.get(c).addValue(x[c] - y[c], c + 1)));
        }
      }

      dataValues = new ArrayList<>(statisticsValues);
      columnTemplate = new ArrayList<>(statisticsColumnTemplate);
      createDynamicColumns();
      nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void descriptiveStatistics() {
    try {
      barModel = new BarChartModel();
      barModel.setLegendPosition("s");
      barModel.setLegendCols(10);
      tableHeader = "Descriptive statistics";
      List<DescriptiveStatistics> decList = new LinkedList<>();
      int j = 1;
      for (DataValue value : statisticsValues) {
        for (int i = 0;
            i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j)
                .size(); i++) {
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
      List<List<String>> properties = new ArrayList<>(decList.size());
      String[] propertyNames = new String[]{
          "Variables", "Mean", "Std Deviation", "Geom. Mean", "Kurtosis", "Maximum", "Minimum", "N",
          "Skewness", "Sum of Squares", "Population Variance", "Variance"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      List<ChartSeries> seriesList = new LinkedList<>();
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        properties.add(new ArrayList<>());
        properties.get(i).add(statisticsColumnTemplate.get(i));
        ChartSeries chartSeries = new ChartSeries();
        chartSeries.setLabel(statisticsColumnTemplate.get(i));
        seriesList.add(chartSeries);
      }
      for (int i = 0; i < decList.size(); i++) {
        DescriptiveStatistics deccriptor = decList.get(i);
        double mean = roundTo2Decimals(deccriptor.getMean());
        properties.get(i).add(String.valueOf(mean));
        double standardDeviation = roundTo2Decimals(deccriptor.getStandardDeviation());
        properties.get(i).add(String.valueOf(standardDeviation));
        double geomMean = roundTo2Decimals(deccriptor.getGeometricMean());
        properties.get(i).add(String.valueOf(geomMean));
        double kurtosis = roundTo2Decimals(deccriptor.getKurtosis());
        properties.get(i).add(String.valueOf(kurtosis));
        double max = roundTo2Decimals(deccriptor.getMax());
        properties.get(i).add(String.valueOf(max));
        double min = roundTo2Decimals(deccriptor.getMin());
        properties.get(i).add(String.valueOf(min));
        double n = deccriptor.getN();
        seriesList.get(i).set("N", n);
        properties.get(i).add(String.valueOf(n));
        double skewness = roundTo2Decimals(deccriptor.getSkewness());
        properties.get(i).add(String.valueOf(skewness));
        double sumsq = roundTo2Decimals(deccriptor.getSumsq());
        properties.get(i).add(String.valueOf(sumsq));
        double populationVariance = roundTo2Decimals(deccriptor.getPopulationVariance());
        properties.get(i).add(String.valueOf(populationVariance));
        double variance = roundTo2Decimals(deccriptor.getVariance());
        properties.get(i).add(String.valueOf(variance));
      }

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
      description = "" +
          "<br><li> Mean - the <a href=\"http://www.xycoon.com/arithmetic_mean.htm\">\n" +
          "   arithmetic mean </a> of the available values\n" +
          "<br><li>  Std Deviation -  Standard deviation is the square root of the variance.  It measures the spread of a set of observations.  The larger the standard deviation is, the more spread out the observations are."
          +
          "<br><li>  Geom. Mean - the <a href=\"http://www.xycoon.com/geometric_mean.htm\">\n" +
          " geometric mean </a> of the available values" +
          "\n" +
          "<br><li>  Kurtosis - the Kurtosis of the available values. Kurtosis is a\n" +
          " measure of the \"peakedness\" of a distribution\n. Kurtosis - Kurtosis is a measure of the heaviness of the tails of a distribution. In SAS, a normal distribution has kurtosis 0. Extremely nonnormal distributions may have high positive or negative kurtosis values, while nearly normal distributions will have kurtosis values close to 0. Kurtosis is positive if the tails are \"heavier\" than for a normal distribution and negative if the tails are \"lighter\" than for a normal distribution."
          +
          "\n" +
          "<br> Maximum - This is the maximum, or largest, value of the variable.\n" +
          "<br> Minimum - the minimum of the available values.\n" +
          "\n" +
          "<br><li>  N - the number of available values.\n" +
          "<br><li>  Skewness - Skewness measures the degree and direction of asymmetry.  A symmetric distribution such as a normal distribution has a skewness of 0, and a distribution that is skewed to the left, e.g. when the mean is less than the median, has a negative skewness."
          +
          "<br><li>  Sum of Squares - the sum of the squares of the available values.\n" +
          "<br><li>  Population Variance - the <a href=\"http://en.wikibooks.org/wiki/Statistics/Summary/Variance\">\n"
          +
          " population variance</a> of the available values.\n" +
          "<br><li>  Variance - The variance is a measure of variability. It is the sum of the squared distances of data value from the mean divided by the variance divisor. The Corrected SS is the sum of squared distances of data value from the mean. Therefore, the variance is the corrected SS divided by N-1. We don't generally use variance as an index of spread because it is in squared units. Instead, we use standard deviation.\n"
          +
          "";

      for (ChartSeries series : seriesList) {
        barModel.addSeries(series);
      }
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public static double roundTo2Decimals(double val) {
    return (double) Math.round(val * 1000) / 1000;
  }

  public void frequencyDistributions() {
    try {
      tableHeader = "Frequency Distribution";
      List<Frequency> decList = new LinkedList<>();
      List<Integer> missedValues = new ArrayList<>();
      List<Integer> validValdues = new ArrayList<>();
      int j = 1;
      for (DataValue value : statisticsValues) {
        for (int i = 0;
            i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j)
                .size(); i++) {
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
      List<ChartSeries> seriesList = getChartSerieses();
      List<List<String>> properties = new ArrayList<>(decList.size());
      String[] propertyNames = new String[]{
          "Sum. freq.", "Unique count", "Valid pct.", "Missed pct.", "Value", "Freq. count",
          "Cum. freq.", "Cum. pct.", "Freq. pct."
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        properties.add(new ArrayList<>());
      }
      int s = 0;
      for (int i = 0; i < decList.size(); i++) {
        properties.get(s).add(statisticsColumnTemplate.get(i));
        Frequency frequency = decList.get(i);
        double sumFreq = frequency.getSumFreq();
        properties.get(s).add(String.valueOf(sumFreq));
        double uniqueCount = frequency.getUniqueCount();
        properties.get(s).add(String.valueOf(uniqueCount));
        int fullSumOfCount = validValdues.get(i) + missedValues.get(i);
        properties.get(s)
            .add("" + roundTo2Decimals((validValdues.get(i) * 100.0) / fullSumOfCount) + "%");
        seriesList.get(i)
            .set("Valid pct.", roundTo2Decimals((validValdues.get(i) * 100.0) / fullSumOfCount));
        properties.get(s)
            .add("" + roundTo2Decimals((missedValues.get(i) * 100.0) / fullSumOfCount) + "%");
        Iterator<Comparable<?>> iterator = frequency.valuesIterator();
        Stack<Double> stack = new Stack<>();
        while (iterator.hasNext()) {
          stack.add((Double) iterator.next());
          properties.add(new ArrayList<>());
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
          properties.get(s).add("" + roundTo2Decimals(cumFreq));
          double cumPct = frequency.getCumPct(stackValue);
          properties.get(s).add("" + roundTo2Decimals(cumPct));
          double frequencyPct = roundTo2Decimals(frequency.getPct(stackValue));
          properties.get(s).add("" + frequencyPct);
          properties.get(s).add("" + frequencyPct);

          s++;
        }
      }
      description = "" +
          "<br><li> Sum. freq. - the  sum of all frequencies." +
          "<br><li> Unique count -  the number of unique values that have been added to the frequency table."
          +
          "<br><li> Valid pct. - the percentage of valide values to the total count." +
          "<br><li> Missed pct. - the percentage of missed values to the total count." +
          "<br><li> Freq. count - the number of values equal to value." +
          "<br><li> Cum. freq. - the cumulative frequency of values less than or equal to v." +
          "<br><li> Cum. pct. - the cumulative percentage of values less than or equal to value." +
          " (as a proportion between 0 and 1)." +
          "<br><li> Freq. pct. - the percentage of values that are equal to v." +
          "   (as a proportion between 0 and 1)." +
          "";

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  private List<ChartSeries> getChartSerieses() {
    barModel = new BarChartModel();
    barModel.setLegendPosition("s");
    barModel.setLegendCols(10);
    List<ChartSeries> seriesList = new LinkedList<>();
    for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
      ChartSeries chartSeries = new ChartSeries();
      chartSeries.setLabel(statisticsColumnTemplate.get(i));
      seriesList.add(chartSeries);
    }
    for (ChartSeries series : seriesList) {
      barModel.addSeries(series);
    }
    dataValues.clear();
    columns.clear();
    columnTemplate.clear();
    columnTemplate.add(0, "Variable");
    return seriesList;
  }

  public void ttest() {
    try {
      tableHeader = "T-test of " + selectedXTTEST + " with alpha = " + alpha + " and Mu = "
          + muValue;
      int xIndexes = 0;

      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {

        if (statisticsColumnTemplate.get(i).equals(selectedXTTEST)) {
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
      columnTemplate = new ArrayList<>(Arrays.asList("Value"));
      List<List<String>> properties = new ArrayList<>(4);
      String[] propertyNames = new String[]{
          " t-statistic associated with a one-sample t-test comparing the mean",
          " p-value associated with the null hypothesis", "test using a fixed significance level"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add(selectedXTTEST + " with alpha = " + alpha + " and Mu = " + muValue);

      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(TestUtils.t(Double.valueOf(muValue), x))));
      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(TestUtils.tTest(Double.valueOf(muValue), x))));
      properties.get(0)
          .add(String.valueOf(TestUtils.tTest(Double.valueOf(muValue), x, Double.valueOf(alpha))));

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void twotest() {
    try {
      tableHeader = "Two samle T-test of " + selectedXTwoT + " and " + selectedYTwoT;
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {

        if (statisticsColumnTemplate.get(i).equals(selectedXTwoT)) {
          xIndexes = i;
        }
        if (statisticsColumnTemplate.get(i).equals(selectedYTwoT)) {
          yIndexes = i;
        }
      }

      double[] x = new double[statisticsValues.size()];
      double[] y = new double[statisticsValues.size()];

      int rowInd = 1;
      int rowIndY = 1;
      for (DataValue value : statisticsValues) {
        String xvalues = value.getValues(rowInd, xIndexes);
        String yvalues = value.getValues(rowInd, yIndexes);
        if (xvalues != null) {
          x[rowInd - 1] = Double.valueOf(xvalues);
          rowInd++;
        }
        if (yvalues != null) {
          y[rowIndY - 1] = Double.valueOf(yvalues).longValue();
          rowIndY++;
        }
      }

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Value"));
      List<List<String>> properties = new ArrayList<>(4);
      String[] propertyNames = new String[]{
          "T-statistic", "P-value  t-statistic", "T-test using a fixed sign. level"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add(selectedXTwoT + " and " + selectedYTwoT);

      properties.get(0).add(String.valueOf(roundTo2Decimals(TestUtils.pairedT(x, y))));
      properties.get(0).add(String.valueOf(roundTo2Decimals(TestUtils.pairedTTest(x, y))));
      properties.get(0).add(String.valueOf(TestUtils.pairedTTest(x, y, Double.valueOf(alpha))));

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void chiSquare() {
    try {
      barModel = new BarChartModel();
      barModel.setLegendPosition("s");
      barModel.setLegendCols(10);
      tableHeader = "Chi-square test of " + selectedXChi + " and " + selectedYChi;
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {

        if (statisticsColumnTemplate.get(i).equals(selectedXChi)) {
          xIndexes = i;
        }
        if (statisticsColumnTemplate.get(i).equals(selectedYChi)) {
          yIndexes = i;
        }
      }

      ArrayList<Double> xx = new ArrayList<Double>();
      ArrayList<Long> yy = new ArrayList<Long>();

      int rowInd = 1;
      int rowIndY = 1;
      for (DataValue value : statisticsValues) {
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

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Value"));
      List<List<String>> properties = new ArrayList<>(4);
      String[] propertyNames = new String[]{
          "Chi-square", "P-value  chi-square ", "Chi-square  using a fixed sign. level"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add(selectedXChi + " and " + selectedYChi);
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
         * writes the  table.
		 */
      HashSet<String> xSet = new HashSet<>();
      xSet.addAll(Arrays.asList(xStrings));
      HashSet<String> ySet = new HashSet<>();
      ySet.addAll(Arrays.asList(yStrings));

      ChisqTestExtended crosstab = new ChisqTestExtended();
      double testStatistic = crosstab.testStatistic(xStrings, yStrings);
      double pValue = crosstab.pValue(xStrings, yStrings);
      content = "<div id=\"correlations\">"
          + "<h1>Crosstable</h1>";
      content
          += "<table style = \"  display: table; border-collapse: collapse; border-spacing: none; border-color: gray;\">";
      content += "<tr><td style=\"border:1px solid black;\"></td>";

      content += "<th style=\"border:1px solid black;\" colspan = 4 >" + " The contingency table"
          + "</th>";
      content += "</tr>";
      content += "<tr>";
      content += "<th style=\"border:1px solid black;\"></th>";
      content += "<th style=\"border:1px solid black;\"></th>";
      content += "<td style=\"border:1px solid black;\" colspan = " + (xSet.size()) +
          ">" + selectedXChi + "</td>";
      content += "<th style=\"border:1px solid black;\"> Row Count </th>";
      content += "</tr>";
      content += "<tr>";
      content += "<th style=\"border:1px solid black;\"></th>";
      content += "<th style=\"border:1px solid black;\">Row Values</th>";
      Iterator<String> iteratorX = xSet.iterator();
      while (iteratorX.hasNext()) {
        LinkedList linkedList = new LinkedList(xSet);
        String next = iteratorX.next();
        content += "<td style=\"border:1px solid black;\">" + (
            mapOfColumns.get(xIndexes) == null ? next : mapOfColumns.get(xIndexes).get(next))
            + "</td>";
      }
      content += "<th style=\"border:1px solid black;\"></th>";
      content += "</tr>";
      List<Integer> columnCount = new LinkedList<>();
      for (int i = 0; i < crosstab.table.length; i++) {
        for (int j = 0; j < crosstab.table[i].length; j++) {
          if (columnCount.size() < j + 1) {
            columnCount.add(0);
          }
          columnCount.set(j, (int) (columnCount.get(j) + crosstab.table[i][j]));
        }
      }
      List<ChartSeries> seriesList = new LinkedList<>();
      ChartSeries chartXSeries = new ChartSeries();
      chartXSeries.setLabel(selectedXChi);
      ChartSeries chartYSeries = new ChartSeries();
      chartYSeries.setLabel(selectedYChi);
      seriesList.add(chartXSeries);
      seriesList.add(chartYSeries);
      barModel.getSeries().addAll(seriesList);
      Iterator<String> iterator = ySet.iterator();
      for (int i = 0; i < crosstab.table.length; i++) {
        content += "<tr>";
        if (i == 0) {
          content += "<td style=\"border:1px solid black;\" rowspan=" + (crosstab.table.length * 4)
              + ">" + selectedYChi + "</td>";
        }
        LinkedList linkedList = new LinkedList(ySet);
        String next = iterator.next();
        while (next == null && iterator.hasNext()) {
          next = iterator.next();
        }
        content += "<td style=\"border:1px solid black;\"rowspan=" + 4 + ">" + (
            mapOfColumns.get(yIndexes) == null ? next : mapOfColumns.get(yIndexes).get(next))
            + "</td>";
        int rowcount = 0;
        for (int j = 0; j < crosstab.table[i].length; j++) {
          content += "<td style=\"border:1px solid black;\"> Count " + crosstab.table[i][j]
              + "</td>";
          rowcount += crosstab.table[i][j];
          if (i == 0) {
            chartYSeries
                .set("ColumnCount " + selectedXChi + " " + xSet.toArray()[j], columnCount.get(j));
          }
        }
        chartXSeries.set("Rowcount " + selectedYChi + " " + ySet.toArray()[i], rowcount);
        content += "<td style=\"border:1px solid black;\" rowspan = 4 >" + "Total row count "
            + rowcount + "</td>";
        content += "</tr>";
        content += "<tr>";
        for (int j = 0; j < crosstab.table[i].length; j++) {
          content += "<td style=\"border:1px solid black;\" >" + "Expected  count "
              + rowcount / crosstab.table[i].length + "</td>";
        }
        content += "</tr>";
        content += "<tr>";
        for (int j = 0; j < crosstab.table[i].length; j++) {
          content += "<td style=\"border:1px solid black;\">" + "Percent % within " + selectedXChi
              + " " + roundTo2Decimals(
              ((crosstab.table[i][j]) / new Double(columnCount.get(j))) * 100d) + "</td>";
        }
        content += "</tr>";
        content += "<tr>";
        for (int j = 0; j < crosstab.table[i].length; j++) {
          content += "<td style=\"border:1px solid black;\">" + "Std.Residual " + roundTo2Decimals(
              (crosstab.table[i][j] - rowcount / crosstab.table[i].length) / (Math
                  .sqrt(rowcount / crosstab.table.length))) + "</td>";
        }
        content += "</tr>";
      }
      if (x.length == y.length) {
        properties.get(0).add(String.valueOf(roundTo2Decimals(TestUtils.chiSquare(x, y))));
        properties.get(0).add(String.valueOf(roundTo2Decimals(TestUtils.chiSquareTest(x, y))));
        properties.get(0).add(String.valueOf(TestUtils.chiSquareTest(x, y, Double.valueOf(alpha))));
      }

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
      content += "<tr>";
      content += "<th style=\"border:1px solid black;\"></th>";
      content += "<th style=\"border:1px solid black;\"></th>";

      content += "<th style=\"border:1px solid black;\"></th>";
      content += "</tr>";
      content += "<tr>";
      content += "<td style=\"border:1px solid black;\" colspan = 5>" + "Total column count "
          + columnCount + "</td>";
      content += "</tr>";
      content += "<tr><td style=\"border:1px solid black;\"></td>";
      content += "<th style=\"border:1px solid black;\">" + "</th>";
      content += "<th style=\"border:1px solid black;\" colspan = 3>" + crosstab.output + "</th>";
      content += "</tr>";
      content += "</table></div>";

      description =
          "<br>Chi-square - It is also known as Pearson chi-square test.  It compares the observed"
              +
              "<br> frequencies with the expected frequencies collectively (considering the degree of freedom"
              +
              "<br> for each of the variables).  The degrees of freedom for chi-square test is (R-1)*(C-1)"
              +
              "<br> where R is the number of rows and C the number of columns of the table." +
              "<br>  (In other words, the number of levels of each of the variables.)" +
              "<br>  A large chi-square statistic will correspond to small p-value.  If the p-value is small"
              +
              "<br> enough (say < 0.05), then we will reject the null hypothesis that the two variables are independent"
              +
              "<br> and conclude that there is an association between the row and the column variables. "
              +
              "<br><li> Table of - This is the title of the table.  The first variable listed will be the row variable and the second variable will be the column variable.\n"
              +
              "\n" +
              "<br><li> Frequency - This is the observed cell frequency.  It is also called count.  For example, there are 15 males (female=0) in the low socioeconomic status group.  The observed cell frequencies and the expected cell frequencies are used to test if the row and the column variables are independent.\n"
              +
              "\n" +
              "<br><li> Expected - This is the cell frequency expected under the null hypothesis that the row and column variables are independent.  This number is produced by using the option expected in the tables statement.  Comparing the expected cell frequency with the observed frequency we should have some idea about whether the row variable is independent of the column variable.\n"
              +
              "\n" +
              "<br><li> Row Pct - This gives the percent of observations in the row.  If there are 15 males (female=0) and 32 females (female=1) in low socioeconomic status group.  So the row percent for the first cell is 15/47*100=31.91.  n"
              +
              "\n" +
              "<br><li> Col Pct - This gives the percent of observations in the column.  If there are 91 males and there are 15 males in the low socioeconomic status group. So the column percent for the first cell is 15/91*100=16.48. \n"
              +
              "\n" +
              "<br><li> Total - This is the number of valid observations for the variable.  The total number of observations is the sum of N and the number of missing values.  If the sample size is not large enough, the test of independence of contingency tables such as Chi-square may not be accurate.\n"
              +
              "\n" +
              "";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!",
              e.getMessage() + " Preconditions:\n" +
                  "\n" +
                  "Expected counts must all be positive.\n" +
                  "Observed counts must all be ≥ 0.\n" +
                  "The observed and expected arrays must have the same length and their common length must be at least 2."));
    }
  }

  public void gtest() {
    try {
      tableHeader = "G test of" + selectedXGTest + " and " + selectedYGTest;
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {

        if (statisticsColumnTemplate.get(i).equals(selectedXGTest)) {
          xIndexes = i;
        }
        if (statisticsColumnTemplate.get(i).equals(selectedYGTest)) {
          yIndexes = i;
        }
      }

      ArrayList<Double> xList = new ArrayList<Double>();
      ArrayList<Long> yList = new ArrayList<Long>();

      int rowInd = 1;
      for (DataValue value : statisticsValues) {
        String xvalues = value.getValues(rowInd, xIndexes);
        String yvalues = value.getValues(rowInd, yIndexes);
        if (xvalues != null) {
          xList.add(Double.valueOf(xvalues));
        }
        rowInd++;
        if (yvalues != null) {
          yList.add(Double.valueOf(yvalues).longValue());
        }
      }
      double[] x = new double[xList.size()];
      long[] y = new long[yList.size()];
      for (int i = 0; i < xList.size(); i++) {
        x[i] = (double) xList.get(i);
        y[i] = (long) yList.get(i);
      }
      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Value"));
      List<List<String>> properties = new ArrayList<>(4);
      String[] propertyNames = new String[]{
          "G-test", " p-value  G-test", "G-test using a fixed significance level"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add(selectedXGTest + " and " + selectedYGTest);

      properties.get(0).add(String.valueOf(roundTo2Decimals(TestUtils.g(x, y))));
      properties.get(0).add(String.valueOf(roundTo2Decimals(TestUtils.gTest(x, y))));
      properties.get(0).add(String.valueOf(TestUtils.gTest(x, y, Double.valueOf(alpha))));

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
      description =
          "<br>G tests are an alternative to chi-square tests that are recommended when observed" +
              "<br> counts are small and / or incidence probabilities for some cells are small." +
              "<br> See Ted Dunning's paper, Accurate Methods for the Statistics of Surprise and Coincidence(<href>http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.14.5962</href>)"
              +
              "<br> for background and an empirical analysis showing now chi-square statistics can be misleading"
              +
              "<br> in the presence of low incidence probabilities. " +
              " <br>\n" +
              "<b>F Value</b> and <b>Pr &gt; F</b> - These are the F Value and p-value, \n" +
              "respectively, testing the null hypothesis that an individual predictor \n" +
              "in the model does not explain a significant proportion of the variance, given \n" +
              "the other variables are in the model. \n" +
              "F Value is computed as MS<sub>Source Var\n" +
              "</sub>/ MS<sub>Error</sub>. Under the null \n" +
              "hypothesis, F Value follows a central F-distribution with numerator DF = DF<sub>Source \n"
              +
              "Var</sub>, where Source Var is the predictor variable of interest, and denominator DF =DF<sub>Error</sub>. \n"
              +
              "Following the point made in Source, superscript o, we focus only on the \n" +
              "interaction term.<br>" +
              "To test the null hypothesis that observed conforms to expected with alpha siginficance level"
              +
              "<br> (equiv. 100 * (1-alpha)% confidence) where 0 < alpha < 1 use: alpha value";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void simpleRegression() {
    try {
      tableHeader = "Simple Regression of dependant variable " + selectedDep
          + " and independant variable " + selectedInDep;
      List<SimpleRegression> decList = new LinkedList<>();

      int dependant = 0;
      int independant = 0;
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        if (statisticsColumnTemplate.get(i).equals(selectedInDep)) {
          independant = i;
        }
        if (statisticsColumnTemplate.get(i).equals(selectedDep)) {
          dependant = i;
        }
      }

      int j = 1;
      decList.add(new SimpleRegression());
      for (DataValue value : statisticsValues) {

        String ind = value.getValues(j, independant);
        String dep = value.getValues(j, dependant);
        if (ind != null && dep != null) {
          decList.get(0).addData(Double.valueOf(ind), Double.valueOf(dep));
        }

        j++;
      }

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Variable"));
      List<List<String>> properties = new ArrayList<>(decList.size());
      String[] propertyNames = new String[]{
          "Square Error", "Intercept", "Intercept Std Err", "Sum Of Cross Products",
          "Sum Squared Errors", "N", "Slope Std Err", "Slope", "R-Square",
          "Slope Confidence Interval", "Total Sum Squares"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0)
          .add("dependant variable " + selectedDep + " and independant variable " + selectedInDep);
      for (int i = 0; i < decList.size(); i++) {

        SimpleRegression simpleRegression = decList.get(i);
        double squareError = roundTo2Decimals(simpleRegression.getMeanSquareError());
        properties.get(i).add(String.valueOf(squareError));
        double intercept = roundTo2Decimals(simpleRegression.getIntercept());
        properties.get(i).add(String.valueOf(intercept));
        double interceptStdErr = roundTo2Decimals(simpleRegression.getInterceptStdErr());
        properties.get(i).add(String.valueOf(interceptStdErr));
        double sumOfCrossProducts = roundTo2Decimals(simpleRegression.getSumOfCrossProducts());
        properties.get(i).add(String.valueOf(sumOfCrossProducts));
        double sumSquaredErrors = roundTo2Decimals(simpleRegression.getSumSquaredErrors());
        properties.get(i).add(String.valueOf(sumSquaredErrors));
        double n = simpleRegression.getN();
        properties.get(i).add(String.valueOf(n));
        double slopeStdErr = roundTo2Decimals(simpleRegression.getSlopeStdErr());
        properties.get(i).add(String.valueOf(slopeStdErr));
        double slope = roundTo2Decimals(simpleRegression.getSlope());
        properties.get(i).add(String.valueOf(slope));
        double rSquare = roundTo2Decimals(simpleRegression.getRSquare());
        properties.get(i).add(String.valueOf(rSquare));
        double slopeConfidenceInterval = roundTo2Decimals(
            simpleRegression.getSlopeConfidenceInterval());
        properties.get(i).add(String.valueOf(slopeConfidenceInterval));
        double totalSumSquares = roundTo2Decimals(simpleRegression.getTotalSumSquares());
        properties.get(i).add(String.valueOf(totalSumSquares));
      }

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }

      description = "" +
          "<br><li> Square Error - the sum of squared errors divided by the degrees of freedom,\n" +
          "  usually abbreviated MSE." +
          "<br><li> Intercept -   the intercept of the estimated regression line, if" +
          "     intercept is true; otherwise 0." +
          "     <p>\n" +
          "     The least squares estimate of the intercept is computed using the\n" +
          "     <a href=\"http://www.xycoon.com/estimation4.htm\">normal equations</a>.\n" +
          "     The intercept is sometimes denoted b0.</p>\n" +
          "     <p>\n" +
          "     <strong>Preconditions</strong>: <ul>\n" +
          "     <li>At least two observations (with at least two different x values)\n" +
          "     must have been added before invoking this method." +
          "<br><li> Intercept Std Err -  the <a href=\"http://www.xycoon.com/standarderrorb0.htm\">\n"
          +
          "     standard error of the intercept estimate</a>,\n" +
          "     usually denoted s(b0).\n" +
          "     <p>" +
          "<br><li> Sum Of Cross Products - the sum of crossproducts, x<sub>i</sub>*y<sub>i</sub>" +
          "<br><li> Slope Std Err -   the <a href=\"http://www.xycoon.com/SumOfSquares.htm\">\n" +
          "     sum of squared errors</a> (SSE) associated with the regression\n" +
          "      model.\n" +
          "     <p>\n" +
          "     The sum is computed using the computational formula</p>\n" +
          "     <p>\n" +
          "     <code>SSE = SYY - (SXY * SXY / SXX)</code></p>\n" +
          "     <p>\n" +
          "     where <code>SYY</code> is the sum of the squared deviations of the y\n" +
          "     values about their mean, <code>SXX</code> is similarly defined and\n" +
          "     <code>SXY</code> is the sum of the products of x and y mean deviations.\n" +
          "     </p><p>\n" +
          "     <p>\n" +
          "     The return value is constrained to be non-negative - i.e., if due to\n" +
          "     rounding errors the computational formula returns a negative result,\n" +
          "     0 is returned.</p>\n" +
          "     <p>\n" +
          "     <strong>Preconditions</strong>: <ul>\n" +
          "     <li>At least two observations (with at least two different x values)\n" +
          "     must have been added before invoking this method." +
          "     returned.\n" +
          "     </li></ul></p>\n" +
          "     " +
          "<br><li> Slope - the slope of the estimated regression line.\n" +
          "    <p>\n" +
          "    The least squares estimate of the slope is computed using the\n" +
          "    <a href=\"http://www.xycoon.com/estimation4.htm\">normal equations</a>.\n" +
          "    The slope is sometimes denoted b1.</p>\n" +
          "    <p>\n" +
          "    <strong>Preconditions</strong>: <ul>\n" +
          "    <li>At least two observations (with at least two different x values)\n" +
          "    must have been added before invoking this method." +
          "    </li></ul></p>\n" +
          "    " +
          "<br><li> R-Square - the <a href=\"http://www.xycoon.com/coefficient1.htm\">\n" +
          "     coefficient of determination</a>,\n" +
          "     usually denoted r-square.\n" +
          "     <p>\n" +
          "     <strong>Preconditions</strong>: <ul>\n" +
          "     <li>At least two observations (with at least two different x values)\n" +
          "     must have been added before invoking this method. If this method is\n" +
          "     invoked before a model can be estimated, <code>Double,NaN</code> is\n" +
          "     returned.\n" +
          "     </li></ul></p>" +
          "<br><li> Slope Confidence Interval - the half-width of a 95% confidence interval for the slope\n"
          +
          "     estimate.\n" +
          "     <p>\n" +
          "     The 95% confidence interval is</p>\n" +
          "     <p>\n" +
          "     Slope - SlopeConfidenceInterval,\n" +
          "     Slope + SlopeConfidenceInterval</p>\n" +
          "     <p>\n" +
          "     If there are fewer that <strong>three</strong> observations in the\n" +
          "     model, or if there is no variation in x, this returns\n" +
          "     <code>Double.NaN</code>.</p>\n" +
          "     <p>\n" +
          "     <strong>Usage Note</strong>:<br>\n" +
          "     The validity of this statistic depends on the assumption that the\n" +
          "     observations included in the model are drawn from a\n" +
          "     <a href=\"http://mathworld.wolfram.com/BivariateNormalDistribution.html\">\n" +
          "     Bivariate Normal Distribution</a>.</p>" +
          "<br><li> Total Sum Squares - the sum of squared deviations of the y values about their mean.\n"
          +
          "     <p>\n" +
          "     This is defined as SSTO\n" +
          "     <a href=\"http://www.xycoon.com/SumOfSquares.htm\">here</a>.</p>\n" +
          "";
      createDynamicColumns();
      nullifyAll();
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void multipleRegressionOls() {
    try {

      List<OLSMultipleLinearRegression> decList = new LinkedList<OLSMultipleLinearRegression>();

      int dependant = 0;
      ArrayList<Integer> independant = new ArrayList<>();
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        for (int ind = 0; ind < selectedIndepVarsOLS.length; ind++) {
          if (statisticsColumnTemplate.get(i).equals(selectedIndepVarsOLS[ind])) {
            independant.add(i);
          }
        }

        if (statisticsColumnTemplate.get(i).equals(selectedDepVarsOLS)) {
          dependant = i;
        }
      }

      decList.add(new OLSMultipleLinearRegression());

      double[] x = new double[statisticsValues.size()];
      double[][] y = new double[statisticsValues.size()][independant.size()];
      int j = 1;
      int z = 1;
      for (DataValue value : statisticsValues) {
        String values = value.getValues(j, dependant);
        if (values != null) {
          x[j - 1] = Double.valueOf(values);

          j++;
        }
        for (int i = 0; i < independant.size(); i++) {

          String yValue = value.getValues(z, independant.get(i));
          if (yValue != null) {
            y[z - 1][i] = Double.valueOf(yValue);
          }
        }
        z++;
      }
      decList.get(0).newSampleData(x, y);

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Variables"));

      List<List<String>> properties = new ArrayList<>(decList.size());
      String[] propertyNames = new String[]{
          "Est. Regr. Std Error", "Est. Error Variance",
          "Est. Regr. Variance", "Calc. Residual Sum Of Squares",
          "Calc. Total Sum Of Squares", "Calc. Adjusted RSquared", "Calc. RSquared",
          "Est. Regr. Parameters"
          , "Est. Regr. Param. Variance"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));

      properties.add(new ArrayList<>());
      String selectedIndep = Arrays.toString(selectedIndepVars);
      properties.get(0).add("Independant variables : " + Arrays.toString(selectedIndepVarsOLS)
          + " and dependant variable " + selectedDepVarsOLS);

      for (int i = 0; i < decList.size(); i++) {

        OLSMultipleLinearRegression simpleRegression = decList.get(i);
        double estimateRegressionStandardError = roundTo2Decimals(
            simpleRegression.estimateRegressionStandardError());
        properties.get(i).add(String.valueOf(estimateRegressionStandardError));
        double estimateErrorVariance = roundTo2Decimals(simpleRegression.estimateErrorVariance());
        properties.get(i).add(String.valueOf(estimateErrorVariance));
        double estimateRegressandVariance = roundTo2Decimals(
            simpleRegression.estimateRegressandVariance());
        properties.get(i).add(String.valueOf(estimateRegressandVariance));
        double calculateResidualSumOfSquares = roundTo2Decimals(
            simpleRegression.calculateResidualSumOfSquares());
        properties.get(i).add(String.valueOf(calculateResidualSumOfSquares));
        double calculateTotalSumOfSquares = roundTo2Decimals(
            simpleRegression.calculateTotalSumOfSquares());
        properties.get(i).add(String.valueOf(calculateTotalSumOfSquares));
        double calculateAdjustedRSquared = roundTo2Decimals(
            simpleRegression.calculateAdjustedRSquared());
        properties.get(i).add(String.valueOf(calculateAdjustedRSquared));
        double calculateRSquared = roundTo2Decimals(simpleRegression.calculateRSquared());
        properties.get(i).add(String.valueOf(calculateRSquared));
        String estimateRegressionParameters = Arrays
            .toString(getRoundedArray(simpleRegression.estimateRegressionParameters()));
        properties.get(i).add(String.valueOf(estimateRegressionParameters));
        String estimateRegressionParametersVariance = convertArrayToString(
            simpleRegression.estimateRegressionParametersVariance());
        properties.get(i).add(String.valueOf(estimateRegressionParametersVariance));
      }

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      tableHeader = "Multiple Regression OLS of independant variables " +
          Arrays.toString(selectedIndepVarsOLS) + " and dependant variable "
          + selectedDepVarsOLS;
      createDynamicColumns();
      nullifyAll();
      description = "<p>Implements ordinary least squares (OLS) to estimate the parameters of a\n" +
          "  multiple linear regression model.</p>\n" +
          " \n" +
          "  <p>The regression coefficients, <code>b</code>, satisfy the normal equations:\n" +
          "  <pre>X<sup>T</sup> X b = X<sup>T</sup> y </pre></p>\n" +
          " \n" +
          "  <p>To solve the normal equations, this implementation uses QR decomposition\n" +
          " <br> of the <b>X</b> matrix. (See  QRDecomposition for details on the\n" +
          " <br>  decomposition algorithm.) The <b>X</b> matrix, also known as the <i>design matrix,</i>\n"
          +
          " <br>  has rows corresponding to sample observations and columns corresponding to independent\n"
          +
          " <br>  variables.  When the model is estimated using an intercept term (i.e. when\n" +
          " <br>  Intercept is false as it is by default), the <b>X</b>\n" +
          " <br>  matrix includes an initial column identically equal to 1.  We solve the normal equations\n"
          +
          " <br>  as follows:\n" +
          " <br>  <pre><b> X<sup>T</sup>X b = X<sup>T</sup> y\n" +
          " <br>  (QR)<sup>T</sup> (QR) b = (QR)<sup>T</sup>y\n" +
          " <br>  R<sup>T</sup> (Q<sup>T</sup>Q) R b = R<sup>T</sup> Q<sup>T</sup> y\n" +
          " <br>  R<sup>T</sup> R b = R<sup>T</sup> Q<sup>T</sup> y\n" +
          " <br>  (R<sup>T</sup>)<sup>-1</sup> R<sup>T</sup> R b = (R<sup>T</sup>)<sup>-1</sup> R<sup>T</sup> Q<sup>T</sup> y\n"
          +
          " <br>  R b = Q<sup>T</sup> y </b></pre></p>" +
          "<br>Est. Regr. Std Error - Std. Error of the Estimate - The standard error of the estimate, "
          +
          "also called the root mean square error, is the standard deviation of the error term,<br>"
          +
          " and is the square root of the Mean Square Residual (or Error)." +
          "<br><li> Est. Error Variance - Estimates the variance of the error." +
          "<br><li> Est. Regr. Variance -  the variance of the regressand, ie Var(y)." +
          "<br><li> Calc. Residual Sum Of Squares - the sum of squared residuals" +
          "<br><li> Calc. Adjusted RSquared - the adjusted R-squared statistic, defined by the formula <pre>\n"
          +
          "     * R<sup>2</sup><sub>adj</sub> = 1 - [SSR (n - 1)] / [SSTO (n - p)]\n" +
          "     * </pre>\n" +
          "     * where SSR is the sum of squared residuals},\n" +
          "     * SSTO is the total sum of squares}, n is the number\n" +
          "     * of observations and p is the number of parameters estimated (including the intercept).</p>\n"
          +
          "     *\n" +
          "     * <p>If the regression is estimated without an intercept term, what is returned is <pre>\n"
          +
          "     * <b> 1 - (1 - calculateRSquared}) * (n / (n - p)) </b>\n" +
          "     * </pre></p>" +
          "<br><li> Calc. Total Sum Of Squares -  the sum of squared deviations of Y from its mean.\n"
          +
          "     <br>  <p>If the model has no intercept term, <b>0</b> is used for the\n" +
          "    <br>  mean of Y - i.e., what is returned is the sum of the squared Y values.</p>\n" +
          "     <br>  <p>The value returned by this method is the SSTO value used in\n" +
          "    <br>  the calculateRSquared R-squared} computation.</p>" +
          "<br><li> Calc. RSquared -  Returns the R-Squared statistic, defined by the formula <pre>\n"
          +
          "      R<sup>2</sup> = 1 - SSR / SSTO\n" +
          "      </pre>\n" +
          "      where SSR is the ResidualSumOfSquares sum of squared residuals\n" +
          "      and SSTO is the TotalSumOfSquares total sum of squares" +
          "<br><li> Est. Regr. Parameters - Estimates the regression parameters b." +
          "<br><li> Est. Regr. Param. Variance - Estimates the variance of the regression parameters, ie Var(b)."
          +
          "";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void multipleRegressionStat() {
    try {
      int dependant = 0;
      ArrayList<Integer> independant = new ArrayList<>();
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        for (int ind = 0; ind < selectedInDepVarMultipleReg.length; ind++) {
          if (statisticsColumnTemplate.get(i).equals(selectedInDepVarMultipleReg[ind])) {
            independant.add(i);
          }
        }

        if (statisticsColumnTemplate.get(i).equals(selectedDepVarMultipleReg)) {
          dependant = i;
        }
      }

      double[][] y = new double[statisticsValues.size()][independant.size()];
      double[] x = new double[statisticsValues.size()];
      int j = 1;
      int z = 1;
      for (DataValue value : statisticsValues) {
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
      LinkedList<Feature> features = new LinkedList<>();
      List<List<Double>> listY = new LinkedList<>();
      for (double[] array : y) {
        LinkedList<Double> doubles = new LinkedList<>();
        for (double d : array) {
          doubles.add(d);
        }
        listY.add(doubles);
      }
      List<List<Double>> transposed = transpose(listY);
      for (int i = 0; i < transposed.size(); i++) {
        double[] objects = toDoubleArray(
            transposed.get(i).toArray(new Double[transposed.get(i).size()]));
        features.add(new Feature(i, statisticsColumnTemplate.get(i), objects));
      }

      Dependent dep = new Dependent(x);
      MultipleLinearRegressionExt reg = new MultipleLinearRegressionExt(
          features.toArray(new Feature[features.size()]), dep);
      this.output = reg.doMultipleLinearRegressionExt();

      String thrVar = "AdjustedRSquared";
      if (methodReg.contains("RSS")) {
        thrVar = "RSS";
      }
      if (methodReg.contains("NFeatures")) {
        thrVar = "NFeatures";
      }
      StepwiseSelectorExt sel = new StepwiseSelectorExt(reg, thrVar);
      if (methodReg.contains("Back Propogation")) {
        this.output += sel.backwardSelection();
      }
      if (methodReg.contains("Forward Propogation")) {
        this.output += sel.forwardSelection();
      }

      //            reg.createAllPossibleInteractions();

      description = "<h3>Stepwise selection</h3>\n" +
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
          "<br>  in heterozygosity when compared to Hardy–Weinberg expectation.";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getLocalizedMessage()));
    }
  }

  public void multipleRegressionGls() {
    try {

      List<GLSMultipleLinearRegression> decList = new LinkedList<>();

      int dependant = 0;
      ArrayList<Integer> independant = new ArrayList<>();
      ArrayList<Integer> observations = new ArrayList<>();
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        for (int ind = 0; ind < selectedIndepVars.length; ind++) {
          if (statisticsColumnTemplate.get(i).equals(selectedIndepVars[ind])) {
            independant.add(i);
          }
        }
        for (int ind = 0; ind < getSelectedObsVars().length; ind++) {
          if (statisticsColumnTemplate.get(i).equals(getSelectedObsVars()[ind])) {
            observations.add(i);
          }
        }
        if (statisticsColumnTemplate.get(i).equals(selectedDepVars)) {
          dependant = i;
        }
      }

      decList.add(new GLSMultipleLinearRegression());

      double[] x = new double[statisticsValues.size()];
      double[][] y = new double[statisticsValues.size()][independant.size()];
      double[][] o = new double[statisticsValues.size()][observations.size()];
      int rowInd = 1;
      for (DataValue value : statisticsValues) {
        String stringValue = value.getValues(rowInd, dependant);
        if (stringValue != null) {
          x[rowInd - 1] = Double.valueOf(stringValue);
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

            String obsValue = value.getValues(rowInd, observations.get(i));
            if (obsValue != null) {
              o[rowInd - 1][i] = Double.valueOf(obsValue);
            }
          }
          rowInd++;
        }
      }
      decList.get(0).newSampleData(x, y, o);

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Variable"));

      List<List<String>> properties = new ArrayList<>(decList.size());
      String[] propertyNames = new String[]{
          "Est. Regr. Std Error", "Est. Error Variance",
          "Est. Regr. Variance", "Est. Regr. Param. Variance",
          "Est. Regr. Param. Std Errors", "Est. Residuals", "Est. Regr. Param."
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add("Independant variables " +
          Arrays.toString(selectedIndepVars) + " and dependant variable "
          + selectedDepVars + " and observed variables " + Arrays.toString(selectedObsVars));

      for (int i = 0; i < decList.size(); i++) {

        GLSMultipleLinearRegression simpleRegression = decList.get(i);
        double estimateRegressionStandardError = roundTo2Decimals(
            simpleRegression.estimateRegressionStandardError());
        properties.get(i).add(String.valueOf(estimateRegressionStandardError));
        double estimateErrorVariance = roundTo2Decimals(simpleRegression.estimateErrorVariance());
        properties.get(i).add(String.valueOf(estimateErrorVariance));
        double estimateRegressandVariance = roundTo2Decimals(
            simpleRegression.estimateRegressandVariance());
        properties.get(i).add(String.valueOf(estimateRegressandVariance));
        String estimateRegressionParametersVariance = convertArrayToString(
            simpleRegression.estimateRegressionParametersVariance());
        properties.get(i).add(String.valueOf(estimateRegressionParametersVariance));
        String estimateRegressionParametersStandardErrors = Arrays
            .toString(simpleRegression.estimateRegressionParametersStandardErrors());
        properties.get(i).add(String.valueOf(estimateRegressionParametersStandardErrors));
        String estimateResiduals = Arrays.toString(simpleRegression.estimateResiduals());
        properties.get(i).add(String.valueOf(estimateResiduals));
        String estimateRegressionParameters = Arrays
            .toString(simpleRegression.estimateRegressionParameters());
        properties.get(i).add(String.valueOf(estimateRegressionParameters));
      }

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      tableHeader = "Multiple Regression GLS of independant variables " +
          Arrays.toString(selectedIndepVars) + " and dependant variable "
          + selectedDepVars + " and observed variables " + Arrays.toString(selectedObsVars);
      createDynamicColumns();
      nullifyAll();
      description = " * The GLS implementation of multiple linear regression.\n" +
          " \n" +
          "  GLS assumes a general covariance matrix Omega of the error\n" +
          "  <pre>\n" +
          "  u ~ N(0, Omega)\n" +
          "  </pre>\n" +
          " \n" +
          "  Estimated by GLS,\n" +
          "  <pre>\n" +
          "  b=(X' Omega^-1 X)^-1X'Omega^-1 y\n" +
          "  </pre>\n" +
          "  whose variance is\n" +
          "  <pre>\n" +
          "  Var(b)=(X' Omega^-1 X)^-1\n" +
          "  </pre>" +
          "<br>Est. Regr. Std Error - Std. Error of the Estimate - The standard error of the estimate, "
          +
          "also called the root mean square error, is the standard deviation of the error term,<br>"
          +
          " and is the square root of the Mean Square Residual (or Error)." +
          "<br><li> Est. Error Variance - Estimates the variance of the error." +
          "<br><li> Est. Regr. Variance -  the variance of the regressand, ie Var(y)." +
          "<br><li> Calc. Residual Sum Of Squares - the sum of squared residuals" +
          "<br><li> Est. Regr. Parameters - Estimates the regression parameters b." +
          "<br><li> Est. Regr. Param. Variance - Estimates the variance of the regression parameters, ie Var(b)."
          +
          "<br><li> Est. Regr. Param. Std Errors - Returns the standard errors of the regression parameters."
          +
          "<br><li> Est. Residuals - Estimates the residuals, ie u = y - X*b." +
          "<br><li> Est. Regr. Param. - Estimates the regression parameters b.";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public double[] toDoubleArray(Double[] arr) {
    double[] tempArray = new double[arr.length];
    int i = 0;
    for (Double d : arr) {
      tempArray[i] = roundTo2Decimals(d.doubleValue());
      i++;
    }
    return tempArray;
  }

  public LinkedList<String> toStringRepr(double[] arr) {
    LinkedList<String> tempArray = new LinkedList<String>();
    int i = 0;
    for (Double d : arr) {
      tempArray.add(roundTo2Decimals(d) + "");
      i++;
    }
    return tempArray;
  }

  public void rankTransform() {
    try {
      tableHeader = "Rank Transform";
      LinkedList<LinkedList<Double>> decList = new LinkedList<>();
      int j = 1;
      for (DataValue value : statisticsValues) {
        for (int i = 0;
            i < value.getValues().get(j).size() && decList.size() != value.getValues().get(j)
                .size(); i++) {
          decList.add(new LinkedList<Double>());
        }
        for (int i = 0; i < value.getValues().get(j).size(); i++) {
          String values = value.getValues(j, i);
          if (values != null) {
            decList.get(i).add(Double.valueOf(values));
          }
        }
        j++;
      }
      List<List<String>> properties = new ArrayList<>(columnTemplate.size());
      for (int i = 0; i < decList.size(); i++) {

        LinkedList<Double> deccriptor = decList.get(i);
        Double[] ts = deccriptor.toArray(new Double[deccriptor.size()]);
        properties.add(toStringRepr(
            new NaturalRanking(NaNStrategy.valueOf(nanStrategy), TiesStrategy.valueOf(tieStrategy))
                .rank(toDoubleArray(ts))));
      }

      dataValues.clear();
      columns.clear();

      Map<Integer, List<String>> values = new HashMap<>();
      int maxSize = 0;
      for (int c = 0; c < properties.size(); c++) {
        if (properties.get(c).size() > maxSize) {
          maxSize = properties.get(c).size();
        }
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
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(statisticsColumnTemplate);
      createDynamicColumns();
      nullifyAll();
      description = "<p > Ranking based on the natural ordering on doubles.</p>\n " +
          " <p>NaNs are treated according to the configured NaNStrategy and ties\n" +
          "  are handled using the selected TiesStrategy.\n" +
          "  Configuration settings are supplied in optional constructor arguments.\n" +
          "  Defaults are NaNStrategy#FAILED and TiesStrategy#AVERAGE,\n" +
          "  respectively.</p>\n" +
          "  <p>Examples:\n" +
          "  <table border=\"1\" cellpadding=\"3\">\n" +
          "  <tr><th colspan=\"3\">\n" +
          "  Input data: (20, 17, 30, 42.3, 17, 50, Double.NaN, Double.NEGATIVE_INFINITY, 17)\n" +
          "  </th></tr>\n" +
          "  <tr><th>NaNStrategy</th><th>TiesStrategy</th>\n" +
          "  <th><code>rank(data)</code></th>\n" +
          "  <tr>\n" +
          "  <td>default (NaNs maximal)</td>\n" +
          "  <td>default (ties averaged)</td>\n" +
          "  <td>(5, 3, 6, 7, 3, 8, 9, 1, 3)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>default (NaNs maximal)</td>\n" +
          "  <td>MINIMUM</td>\n" +
          "  <td>(5, 2, 6, 7, 2, 8, 9, 1, 2)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>MINIMAL</td>\n" +
          "  <td>default (ties averaged)</td>\n" +
          "  <td>(6, 4, 7, 8, 4, 9, 1.5, 1.5, 4)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>REMOVED</td>\n" +
          "  <td>SEQUENTIAL</td>\n" +
          "  <td>(5, 2, 6, 7, 3, 8, 1, 4)</td></tr>\n" +
          "  <tr>\n" +
          "  <td>MINIMAL</td>\n" +
          "  <td>MAXIMUM</td>\n" +
          "  <td>(6, 5, 7, 8, 5, 9, 2, 2, 5)</td></tr></table></p>\n" +
          "   Strategies for handling NaN values in rank transformations.\n" +
          " <ul>\n" +
          " <li>MINIMAL - NaNs are treated as minimal in the ordering, equivalent to\n" +
          "  (that is, tied with) Double.NEGATIVE_INFINITY.</li>\n" +
          "  <li>MAXIMAL - NaNs are treated as maximal in the ordering, equivalent to\n" +
          "  Double.POSITIVE_INFINITY</li>\n" +
          "  <li>REMOVED - NaNs are removed before the rank transform is applied</li>\n" +
          "  <li>FIXED - NaNs are left \"in place,\" that is the rank transformation is\n" +
          "  applied to the other elements in the input array, but the NaN elements\n" +
          "  are returned unchanged.</li>\n" +
          "  <li>FAILED - If any NaN is encountered in the input array, an appropriate\n" +
          "  exception is thrown</li>\n" +
          " </ul>\n" +
          "  Strategies for handling tied values in rank transformations.\n" +
          "  <ul>\n" +
          "  <li>SEQUENTIAL - Ties are assigned ranks in order of occurrence in the original array,\n"
          +
          "  for example (1,3,4,3) is ranked as (1,2,4,3)</li>\n" +
          "  <li>MINIMUM - Tied values are assigned the minimum applicable rank, or the rank\n" +
          "  of the first occurrence. For example, (1,3,4,3) is ranked as (1,2,4,2)</li>\n" +
          "  <li>MAXIMUM - Tied values are assigned the maximum applicable rank, or the rank\n" +
          "  of the last occurrence. For example, (1,3,4,3) is ranked as (1,3,4,3)</li>\n" +
          "  <li>AVERAGE - Tied values are assigned the average of the applicable ranks.\n" +
          "  For example, (1,3,4,3) is ranked as (1,2.5,4,2.5)</li>\n" +
          " <li>RANDOM - Tied values are assigned a random integer rank from among the\n" +
          " applicable values. The assigned rank will always be an integer, (inclusively)\n" +
          " between the values returned by the MINIMUM and MAXIMUM strategies.</li>\n" +
          " </ul> ";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
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
      tableHeader = "Anova test";
      ArrayList<Integer> independant = new ArrayList<>();
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        for (int ind = 0; ind < testsData.length; ind++) {
          if (statisticsColumnTemplate.get(i).equals(testsData[ind])) {
            independant.add(i);
          }
        }
      }

      List<List<Object>> y = new LinkedList<>();
      for (int i = 0; i < independant.size(); i++) {
        int j = 1;
        int rowInd = 1;
        for (DataValue value : statisticsValues) {

          String values = value.getValues(j, independant.get(i));
          if (values != null) {
            if (i == 0) {
              y.add(new LinkedList<>());
            }
            y.get(rowInd - 1).add(Double.valueOf(values));

            rowInd++;
          }
          j++;
        }
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
          if (var.get(i) != null) {
            array[i] = ((Double) var.get(i)).doubleValue();
          } else {
            array[i] = Double.NaN;
          }
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

      description = " <br>\n" +
          "<b>F Value</b> and <b>Pr &gt; F</b> - These are the F Value and p-value, \n" +
          "respectively, testing the null hypothesis that an individual predictor \n" +
          "in the model does not explain a significant proportion of the variance, given \n" +
          "the other variables are in the model. \n" +
          "F Value is computed as MS<sub>Source Var\n" +
          "</sub>/ MS<sub>Error</sub>. Under the null \n" +
          "hypothesis, F Value follows a central F-distribution with numerator DF = DF<sub>Source \n"
          +
          "Var</sub>, where Source Var is the predictor variable of interest, and denominator DF =DF<sub>Error</sub>. \n"
          +
          "Following the point made in Source, superscript o, we focus only on the \n" +
          "interaction term.<br>" +
          "One-Way ANOVA test with significance level set at 0.01  test will, assuming assumptions\n"
          +
          " are met, reject the null hypothesis incorrectly only about one in 100 times.";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void covariance() {
    try {
      tableHeader = "Covariance of " + Arrays.toString(matrixDataCov);
      List<Covariance> decList = new LinkedList<Covariance>();

      ArrayList<Integer> independant = new ArrayList<>();
      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {
        for (int ind = 0; ind < matrixDataCov.length; ind++) {
          if (statisticsColumnTemplate.get(i).equals(matrixDataCov[ind])) {
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
          if (values != null) {
            y[j - 1][i] = Double.valueOf(values);
          }
        }
        j++;
      }

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Value"));
      List<List<String>> properties = new ArrayList<>(decList.size());
      String[] propertyNames = new String[]{
          "Covariance Matrix", "Pearsons Correlation",
          "Pearsons Correlation Matrix", "Pearsons Correlation PValues",
          "Pearsons Correlation Standard Errors"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add(Arrays.toString(matrixDataCov));
      for (int i = 0; i < decList.size(); i++) {

        String covarianceMatrix = convertArrayToString(
            new Covariance(y).getCovarianceMatrix().getData());
        properties.get(i).add(String.valueOf(covarianceMatrix));
        PearsonsCorrelation pearsonsCorrelation = new PearsonsCorrelation(y);
        String pearsonsCorrelationMatrix = convertArrayToString(
            pearsonsCorrelation.getCorrelationMatrix().getData());
        properties.get(i).add(String.valueOf(pearsonsCorrelationMatrix));
        String pearsonsCorrelationPValues = convertArrayToString(
            pearsonsCorrelation.getCorrelationPValues().getData());
        properties.get(i).add(String.valueOf(pearsonsCorrelationPValues));
        String pearsonsCorrelationStandardErrors = convertArrayToString(
            pearsonsCorrelation.getCorrelationStandardErrors().getData());
        properties.get(i).add(String.valueOf(pearsonsCorrelationStandardErrors));
      }

      Map<Integer, List<String>> values = new HashMap<Integer, List<String>>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
      description = "Computes covariances for pairs of arrays or columns of a matrix.\n" +
          " The columns of the input matrices are assumed to represent variable values.</p>\n" +
          " \n" +
          " <p>The  argument biasCorrected determines whether or\n" +
          " not computed covariances are bias-corrected.</p>\n" +
          " \n" +
          "  <p>Unbiased covariances are given by the formula</p>\n" +
          "  cov(X, Y) = &Sigma;[(x<sub>i</sub> - E(X))(y<sub>i</sub> - E(Y))] / (n - 1)\n" +
          "  where E(X) is the mean of X and E(Y)\n" +
          "  is the mean of the Y values.\n" +
          " \n" +
          "  <p>Non-bias-corrected estimates use n in place of n - 1" +
          "<br>Pearson Correlation - These numbers measure the strength and direction of the linear relationship"
          +
          "<br> between the two variables.  The correlation coefficient can range from -1 to +1, with -1 indicating"
          +
          " <br>a perfect negative correlation, +1 indicating a perfect positive correlation, and 0 indicating no"
          +
          "<br> correlation at all.  (A variable correlated with itself will always have a correlation coefficient of 1.)"
          +
          " <br> You can think of the correlation coefficient as telling you the extent to which you can guess the value"
          +
          "<br> of one variable given a value of the other variable.  From the scatterplot of the variables read and write"
          +
          "<br> below, we can see that the points tend along a line going from the bottom left to the upper right, which is"
          +
          "<br> the same as saying that the correlation is positive. The .597 is the numerical description of how tightly"
          +
          "<br> around the imaginary line the points lie. If the correlation was higher, the points would tend to be closer"
          +
          "<br> to the line; if it was smaller, they would tend to be further away from the line.  Also note that, by definition,"
          +
          "<br> any variable correlated with itself has a correlation of 1." +
          "<li> Pearsons Correlation PValues -  a matrix of p-values associated with the (two-sided) null\n"
          +
          "hypothesis that the corresponding correlation coefficient is zero.\n" +
          "<p>The values in the matrix are sometimes referred to as the\n" +
          " <i>significance</i> of the corresponding correlation coefficients.</p>\n" +
          " <li> Pearsons Correlation Standard Errors -  a matrix of standard errors associated with the estimates\n"
          +
          "     in the correlation matrix.<br/>\n" +
          "      <p>The formula used to compute the standard error is <br/>\n" +
          "      SE<sub>r</sub> = ((1 - r<sup>2</sup>) / (n - 2))<sup>1/2</sup>\n" +
          "      where r is the estimated correlation coefficient and\n" +
          "     * n is the number of observations in the source dataset.</p>   ";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public void kolmogorov() {
    try {
      tableHeader = "Kolmogorov test of " + selectedXKolmogorov + " and " + selectedYKolmogorov;
      int xIndexes = 0;
      int yIndexes = 0;

      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {

        if (statisticsColumnTemplate.get(i).equals(selectedXKolmogorov)) {
          xIndexes = i;
        }
        if (statisticsColumnTemplate.get(i).equals(selectedYKolmogorov)) {
          yIndexes = i;
        }
      }

      double[] x = new double[statisticsValues.size()];
      double[] y = new double[statisticsValues.size()];

      int rowInd = 1;
      int rowIndY = 1;
      for (DataValue value : statisticsValues) {
        String xvalues = value.getValues(rowInd, xIndexes);
        String yvalues = value.getValues(rowInd, yIndexes);
        if (xvalues != null) {
          x[rowInd - 1] = Double.valueOf(xvalues);
          rowInd++;
        }
        if (yvalues != null) {
          y[rowIndY - 1] = Double.valueOf(yvalues).longValue();
          rowIndY++;
        }
      }

      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Value"));
      List<List<String>> properties = new ArrayList<>(4);
      String[] propertyNames = new String[]{
          "Kolmogorov-Smirnov Test", " Kolmogorov-Smirnov Statistic",
          "Kolmogorov-Smirnov Test strict"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add(selectedXKolmogorov + " and " + selectedYKolmogorov);

      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(TestUtils.kolmogorovSmirnovTest(x, y))));
      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(TestUtils.kolmogorovSmirnovStatistic(x, y))));
      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(TestUtils.kolmogorovSmirnovTest(x, y, true))));

      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      createDynamicColumns();
      nullifyAll();
      description = "<br>A goodness-of-fit test for any statistical distribution. The test relies" +
          "<br> on the fact that the value of the sample cumulative density function is asymptotically normally distributed.\n"
          +
          "\n" +
          "To apply the Kolmogorov-Smirnov test, calculate the cumulative frequency (normalized by the sample size)"
          +
          "<br> of the observations as a function of class. Then calculate the cumulative frequency for a true "
          +
          "<br>distribution (most commonly, the normal distribution). Find the greatest discrepancy between the"
          +
          "<br> observed and expected cumulative frequencies, which is called the \"D-statistic.\" Compare this"
          +
          "<br> against the critical D-statistic for that sample size. If the calculated D-statistic is greater"
          +
          "<br> than the critical one, then reject the null hypothesis that the distribution is of the expected form. "
          +
          "<br>The test is an R-estimate." +
          " <br>\n" +
          "<b>F Value</b> and <b>Pr &gt; F</b> - These are the F Value and p-value, \n" +
          "respectively, testing the null hypothesis that an individual predictor \n" +
          "in the model does not explain a significant proportion of the variance, given \n" +
          "the other variables are in the model. \n" +
          "F Value is computed as MS<sub>Source Var\n" +
          "</sub>/ MS<sub>Error</sub>. Under the null \n" +
          "hypothesis, F Value follows a central F-distribution with numerator DF = DF<sub>Source \n"
          +
          "Var</sub>, where Source Var is the predictor variable of interest, and denominator DF =DF<sub>Error</sub>. \n"
          +
          "Following the point made in Source, superscript o, we focus only on the \n" +
          "interaction term.<br>" +
          "Kolmogorov-Smirnov Test - Given a double[] array data of values, to evaluate the null\n"
          +
          "hypothesis that the values are drawn from a unit normal distribution returns the p-value"
          +
          "<br> Kolmogorov-Smirnov Statistic returns the D-statistic" +
          "<br> Kolmogorov-Smirnov Test strict - exact computation of the p-value (overriding the selection of estimation method).";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public String convertArrayToString(double[][] array) {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (double[] s1 : array) {
      double[] roundedArray = getRoundedArray(s1);
      sb.append(Arrays.toString(roundedArray)).append(",\r\n");
    }
    sb.append("]");
    return sb.toString();
  }

  private double[] getRoundedArray(double[] s1) {
    double[] roundedArray = new double[s1.length];
    for (int i = 0; i < s1.length; i++) {
      roundedArray[i] = roundTo2Decimals(s1[i]);
    }
    return roundedArray;
  }

  public void pearsonsCorrelation() {
    try {

      List<Covariance> decList = new LinkedList<>();

      int dependant = 0;
      int independant = 0;

      for (int i = 0; i < statisticsColumnTemplate.size(); i++) {

        if (statisticsColumnTemplate.get(i).equals(selectedXCovar)) {
          dependant = i;
        }
        if (statisticsColumnTemplate.get(i).equals(selectedYCovar)) {
          independant = i;
        }
      }

      decList.add(new Covariance());

      double[] x = new double[statisticsValues.size()];
      double[] y = new double[statisticsValues.size()];
      int rowInd = 1;
      boolean containsNan = false;
      for (DataValue value : statisticsValues) {
        String xvalues = value.getValues(rowInd, dependant);
        String valuesy = value.getValues(rowInd, independant);
        if (xvalues != null && valuesy != null) {
          x[rowInd - 1] = Double.valueOf(xvalues);
          y[rowInd - 1] = Double.valueOf(valuesy);
        }
        rowInd++;
      }
      double covariance = decList.get(0).covariance(x, y);
      double covarianceWithoutBias = decList.get(0).covariance(x, y, false);
      double covarianceWithBias = decList.get(0).covariance(x, y, true);
      dataValues.clear();
      columns.clear();
      columnTemplate.clear();
      columnTemplate = new ArrayList<>(Arrays.asList("Variable"));
      List<List<String>> properties = new ArrayList<List<String>>(decList.size());
      String[] propertyNames = new String[]{
          "Covariance", "Covariance Without Bias", "Covariance With Bias",
          "Pearsons Correlation", "Spearmans Correlation", "Kendalls Correlation"
      };
      columnTemplate.addAll(Arrays.asList(propertyNames));
      properties.add(new ArrayList<>());
      properties.get(0).add("Correlation of " + selectedYCovar + " and " + selectedXCovar);
      for (int i = 0; i < decList.size(); i++) {

        Covariance simpleRegression = decList.get(i);
      }
      properties.get(0).add(String.valueOf(roundTo2Decimals(covariance)));
      properties.get(0).add(String.valueOf(roundTo2Decimals(covarianceWithoutBias)));
      properties.get(0).add(String.valueOf(roundTo2Decimals(covarianceWithBias)));
      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(new PearsonsCorrelation().correlation(x, y))));
      if (!containsNan) {
        properties.get(0)
            .add(String.valueOf(roundTo2Decimals(new SpearmansCorrelation().correlation(x, y))));
      }
      properties.get(0)
          .add(String.valueOf(roundTo2Decimals(new KendallsCorrelation().correlation(x, y))));
      Map<Integer, List<String>> values = new HashMap<>();

      for (int c = 1; c <= properties.size(); c++) {
        values.put(c, properties.get(c - 1));
        dataValues.add(new DataValue(c, values));
      }
      tableHeader = "Covariance and Correlation of " + selectedYCovar + " and " + selectedXCovar;
      createDynamicColumns();
      nullifyAll();
      description = "Computes covariances for pairs of arrays or columns of a matrix.\n" +
          " The columns of the input matrices are assumed to represent variable values.</p>\n" +
          " \n" +
          " <p>The  argument biasCorrected determines whether or\n" +
          " not computed covariances are bias-corrected.</p>\n" +
          " \n" +
          "  <p>Unbiased covariances are given by the formula</p>\n" +
          "  cov(X, Y) = &Sigma;[(x<sub>i</sub> - E(X))(y<sub>i</sub> - E(Y))] / (n - 1)\n" +
          "  where E(X) is the mean of X and E(Y)\n" +
          "  is the mean of the Y values.\n" +
          " \n" +
          "  <p>Non-bias-corrected estimates use n in place of n - 1" +
          "<br>Pearson Correlation - These numbers measure the strength and direction of the linear relationship"
          +
          "<br> between the two variables.  The correlation coefficient can range from -1 to +1, with -1 indicating"
          +
          " <br>a perfect negative correlation, +1 indicating a perfect positive correlation, and 0 indicating no"
          +
          "<br> correlation at all.  (A variable correlated with itself will always have a correlation coefficient of 1.)"
          +
          " <br> You can think of the correlation coefficient as telling you the extent to which you can guess the value"
          +
          "<br> of one variable given a value of the other variable.  From the scatterplot of the variables read and write"
          +
          "<br> below, we can see that the points tend along a line going from the bottom left to the upper right, which is"
          +
          "<br> the same as saying that the correlation is positive. The .597 is the numerical description of how tightly"
          +
          "<br> around the imaginary line the points lie. If the correlation was higher, the points would tend to be closer"
          +
          "<br> to the line; if it was smaller, they would tend to be further away from the line.  Also note that, by definition,"
          +
          "<br> any variable correlated with itself has a correlation of 1." +
          "<li> Pearsons Correlation PValues -  a matrix of p-values associated with the (two-sided) null\n"
          +
          "hypothesis that the corresponding correlation coefficient is zero.\n" +
          "<p>The values in the matrix are sometimes referred to as the\n" +
          " <i>significance</i> of the corresponding correlation coefficients.</p>\n" +
          " <li> Pearsons Correlation Standard Errors -  a matrix of standard errors associated with the estimates\n"
          +
          "     in the correlation matrix.<br/>\n" +
          "      <p>The formula used to compute the standard error is <br/>\n" +
          "      SE<sub>r</sub> = ((1 - r<sup>2</sup>) / (n - 2))<sup>1/2</sup>\n" +
          "      where r is the estimated correlation coefficient and\n" +
          "     n is the number of observations in the source dataset.</p>  " +
          "<br>  Implementation of Kendall's Tau-b rank correlation</a>.\n" +
          " <p>\n" +
          "  A pair of observations (x<sub>1</sub>, y<sub>1</sub>) and\n" +
          "  (x<sub>2</sub>, y<sub>2</sub>) are considered <i>concordant</i> if\n" +
          "  x<sub>1</sub> &lt; x<sub>2</sub> and y<sub>1</sub> &lt; y<sub>2</sub>\n" +
          "  or x<sub>2</sub> &lt; x<sub>1</sub> and y<sub>2</sub> &lt; y<sub>1</sub>.\n" +
          "  The pair is <i>discordant</i> if x<sub>1</sub> &lt; x<sub>2</sub> and\n" +
          " y<sub>2</sub> &lt; y<sub>1</sub> or x<sub>2</sub> &lt; x<sub>1</sub> and\n" +
          "  y<sub>1</sub> &lt; y<sub>2</sub>.  If either x<sub>1</sub> = x<sub>2</sub>\n" +
          "  or y<sub>1</sub> = y<sub>2</sub>, the pair is neither concordant nor\n" +
          " discordant.\n" +
          "  <p>\n" +
          "  Kendall's Tau-b is defined as:\n" +
          "  <pre>\n" +
          "  tau<sub>b</sub> = (n<sub>c</sub> - n<sub>d</sub>) / sqrt((n<sub>0</sub> - n<sub>1</sub>) * (n<sub>0</sub> - n<sub>2</sub>))\n"
          +
          "  </pre>\n" +
          "  <p>\n" +
          "  where:\n" +
          "  <ul>\n" +
          "     <li>n<sub>0</sub> = n * (n - 1) / 2</li>\n" +
          "     <li>n<sub>c</sub> = Number of concordant pairs</li>\n" +
          "     <li>n<sub>d</sub> = Number of discordant pairs</li>\n" +
          "     <li>n<sub>1</sub> = sum of t<sub>i</sub> * (t<sub>i</sub> - 1) / 2 for all i</li>\n"
          +
          "     <li>n<sub>2</sub> = sum of u<sub>j</sub> * (u<sub>j</sub> - 1) / 2 for all j</li>\n"
          +
          "     <li>t<sub>i</sub> = Number of tied values in the i<sup>th</sup> group of ties in x</li>\n"
          +
          "     <li>u<sub>j</sub> = Number of tied values in the j<sup>th</sup> group of ties in y</li>\n"
          +
          " </ul>\n" +
          " <p>\n" +
          " This implementation uses the O(n log n) algorithm described in\n" +
          " William R. Knight's 1966 paper \"A Computer Method for Calculating\n" +
          " Kendall's Tau with Ungrouped Data\" in the Journal of the American\n" +
          " Statistical Association.\n" +
          " \n" +
          "   <a href=\"http://en.wikipedia.org/wiki/Kendall_tau_rank_correlation_coefficient\">\n"
          +
          "  Kendall tau rank correlation coefficient (Wikipedia)</a>\n" +
          "  <a href=\"http://www.jstor.org/stable/2282833\">A Computer\n" +
          " Method for Calculating Kendall's Tau with Ungrouped Data</a>\n" +
          " " +
          " <br>  Spearman's rank correlation. This implementation performs a rank\n" +
          " transformation on the input data and then computes  PearsonsCorrelation\n" +
          " on the ranked data.\n" +
          " <p>\n" +
          "  By default, ranks are computed using NaturalRanking with default\n" +
          "  strategies for handling NaNs and ties in the data (NaNs maximal, ties averaged).\n" +
          "  The ranking algorithm can be set using a constructor argument.";
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
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

  public ArrayList<String> getStatisticsColumnTemplate() {
    return statisticsColumnTemplate;
  }

  public void setStatisticsColumnTemplate(ArrayList<String> statisticsColumnTemplate) {
    this.statisticsColumnTemplate = statisticsColumnTemplate;
  }

  public void onCellEdit(CellEditEvent event) {
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();

    if (newValue != null && !newValue.equals(oldValue)) {
      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cell Changed",
          "Old: " + oldValue + ", New:" + newValue);
      FacesContext.getCurrentInstance().addMessage(null, msg);
    }
  }

  private List<File> selectedFiles;

  public void setSelectedFiles(List<File> selectedFiles) {
    this.selectedFiles = selectedFiles;
  }

  public List<File> getSelectedFiles() {
    return selectedFiles;
  }
}