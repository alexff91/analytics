package com.analytics.web;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.collect.Lists;
import com.nz.simplecrud.controller.LoginController;
import com.nz.simplecrud.service.UserService;
import javastat.inference.ChisqTest;
import org.apache.commons.io.FileUtils;
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

  private String descrStatData[];

  private String freqDistribData[];

  public void descriptiveStatistics() {
    DescriptiveStatistics descriptiveStatistic = new DescriptiveStatistics(this);
    descriptiveStatistic.descriptiveStatistics(descrStatData);
  }

  public void frequencyDistributions() {
    FrequencyDistribution frequencyDistribution = new FrequencyDistribution(this);
    frequencyDistribution.frequencyDistributions(freqDistribData);
  }

  public void anova() {
    AnovaTest descriptiveStatistic = new AnovaTest(this);
    descriptiveStatistic.anova();
  }

  public void ttest() {
    TTTest descriptiveStatistic = new TTTest(this);
    descriptiveStatistic.ttest();
  }

  public void kolmogorov() {
    Kolmogorov descriptiveStatistic = new Kolmogorov(this);
    descriptiveStatistic.kolmogorov();
  }

  public void twotest() {
    TwoTest descriptiveStatistic = new TwoTest(this);
    descriptiveStatistic.twotest();
  }

  public void chiSquare() {
    ChiSquare descriptiveStatistic = new ChiSquare(this);
    descriptiveStatistic.chiSquare();
  }

  public void gtest() {
    GTest descriptiveStatistic = new GTest(this);
    descriptiveStatistic.gtest();
  }

  public void covariance() {
    Covariance descriptiveStatistic = new Covariance(this);
    descriptiveStatistic.covariance();
  }

  public void multipleRegressionOls() {
    MultipleRegressionOLS descriptiveStatistic = new MultipleRegressionOLS(this);
    descriptiveStatistic.multipleRegressionOls();
  }

  public void pearsonsCorrelation() {
    PearsonCorrelation descriptiveStatistic = new PearsonCorrelation(this);
    descriptiveStatistic.pearsonsCorrelation();
  }

  public void rankTransform() {
    RankTransform descriptiveStatistic = new RankTransform(this);
    descriptiveStatistic.rankTransform();
  }

  public void multipleRegressionGls() {
    MultipleRegressionGLS descriptiveStatistic = new MultipleRegressionGLS(this);
    descriptiveStatistic.multipleRegressionGls();
  }

  public void multipleRegressionStat() {
    MultipleRegressionStat descriptiveStatistic = new MultipleRegressionStat(this);
    descriptiveStatistic.multipleRegressionStat();
  }

  public void simpleRegression() {
    SimpleRegression descriptiveStatistic = new SimpleRegression(this);
    descriptiveStatistic.simpleRegression();
  }

  public com.nz.simplecrud.entity.File getFiles() {
    return files;
  }

  public void setFiles(final com.nz.simplecrud.entity.File files) {
    this.files = files;
  }

  com.nz.simplecrud.entity.File files;

  public void uploadSelectedFile() {
    final File[] fileToUpload = new File[1];
    try {
      java.nio.file.Files.walk(Paths.get("filestorage")).forEach(path -> {
            if (path.toFile().getName().equals(files.getFilename
                ())) {
              fileToUpload[0] = path.toFile();
            }
          }

      );
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      FileInputStream fileInputStream = new FileInputStream(fileToUpload[0]);
      String fileName = files.getFilename();
      FacesContext.getCurrentInstance().addMessage(fileName + "is uploaded" +
          " ", new FacesMessage("File uploading", fileName + "is uploaded"));
      String format = fileName.substring(fileName.lastIndexOf("."), fileName.length());
      uploadFileFromeDiskAndStore(FacesContext.getCurrentInstance(),
          fileToUpload[0].getAbsolutePath(), format,
          fileInputStream, false);
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

  public static boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
  }

  public void automaticallyMapVariables() {
    try {

      int dependant = 0;

      for (int i = 0; i < columnTemplate.size(); i++) {
        dependant = i;

        String[] x = new String[statisticsValues.size()];
        int rowInd = 1;
        HashMap<String, String> oldNewMapping = new HashMap<>();
        for (DataValue value : statisticsValues) {
          String xvalues = value.getValues(rowInd, dependant);
          if (xvalues != null) {
            if (!isNumeric(xvalues)) {
              String changedKey = x[rowInd - 1];
              if (oldNewMapping.containsKey(changedKey)) {
                x[rowInd - 1] = oldNewMapping.get(changedKey);
              } else {
                oldNewMapping.put(changedKey, String.valueOf(oldNewMapping.size() + 1));
                x[rowInd - 1] = String.valueOf(rowInd);
              }

              mapOfValues
                  .add(
                      "Column:" + columnTemplate.get(dependant) + ", key:" + changedKey + ", value:"
                          + x[rowInd - 1]);
              if (mapOfColumns.get(dependant) == null) {
                HashMap<String, String> valueMap = new HashMap<>();
                valueMap.put(x[rowInd - 1], changedKey);
                mapOfColumns.put(dependant, valueMap);
              } else {
                mapOfColumns.get(dependant).put(x[rowInd - 1], changedKey);
              }
            } else {
              x[rowInd - 1] = xvalues;
            }

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
      }
    } catch (Exception e) {
      e.printStackTrace();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  public List<DataValue> getStatisticsValues() {
    return statisticsValues;
  }

  public void setStatisticsValues(final List<DataValue> statisticsValues) {
    this.statisticsValues = statisticsValues;
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

  void createDynamicColumns() {
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

  public void setDescrStatData(String[] descrStatData) {
    this.descrStatData = descrStatData;
  }

  public String[] getDescrStatData() {
    return descrStatData;
  }

  public void setFreqDistribData(String[] freqDistribData) {
    this.freqDistribData = freqDistribData;
  }

  public String[] getFreqDistribData() {
    return freqDistribData;
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

  /**
   * Stores uploaded data into the users folder on server and parseing it for statistics
   * @param event
   */
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
      uploadFileFromeDiskAndStore(facesContext, fileName, format, inputStream, true);
    } catch (Exception e) {
      e.printStackTrace();
      nullifyAll();
      FacesContext.getCurrentInstance().addMessage(null,
          new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error!", e.getMessage()));
    }
  }

  /**
   * Depending on the current operation system stores the file
   * @param facesContext
   * @param fileName
   * @param format
   * @param inputStream
   * @param needToStore
   */
  private void uploadFileFromeDiskAndStore(final FacesContext facesContext, final String fileName,
      final String format, final InputStream inputStream, boolean needToStore) {
    File file;
    if (needToStore) {
      if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
        file = new File("filestorage\\" + fileName);
      } else {
        System.out.println("/opt/wildfly/bin/filestorage/" + fileName);
        file = new File("/opt/wildfly/bin/filestorage/" + fileName);
      }
    } else {
      System.out.println("not need to store");
      file = new File(fileName);
    }
    if (format.equals(".xlsx")) {

      StreamingReader reader = null;

      try {

        saveFile(inputStream, needToStore, file);
        InputStream is;
        if (needToStore) {
          is = new FileInputStream(file);
        } else {
          is = inputStream;
        }

        //create destination File
        //                  saveFileToUserFolder(is,fileName);
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
        e.printStackTrace();
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
            for (int j = 0; j < columnTemplate.size() && j < cells.size(); j++) {
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
        saveFile(inputStream, needToStore, file);
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
    // don't need to show all data after uploading
    String[] ts = columnTemplate.toArray(new String[columnTemplate.size()]);
    statisticsColumnTemplate = Lists.newArrayList(ts);
    createDynamicColumns();
    nullifyAll();
    tableHeader = "Data values";
    mapOfColumns.clear();
    mapOfValues.clear();
  }

  private void saveFile(final InputStream inputStream, final boolean needToStore, final File file)
  throws IOException {
    if (needToStore) {
      FileUtils.forceMkdir(new File("filestorage"));
      OutputStream out = new FileOutputStream(file);
      IOUtils.copy(inputStream, out);
      inputStream.close();
      out.close();
    }
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
    File destFile = new File("filestorage/" + fileName);
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

  /**
   * Adding a new column to the statistics dataset with choosen operation
   */
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

  public static double roundTo2Decimals(double val) {
    return (double) Math.round(val * 1000) / 1000;
  }

  List<ChartSeries> getChartSerieses() {
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

  double[] getRoundedArray(double[] s1) {
    double[] roundedArray = new double[s1.length];
    for (int i = 0; i < s1.length; i++) {
      roundedArray[i] = roundTo2Decimals(s1[i]);
    }
    return roundedArray;
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
    return nanStrategy;
  }

  public String getTieStrategy() {
    return tieStrategy;
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