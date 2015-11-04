package com.nz.simplecrud.controller;

import com.nz.simplecrud.entity.UserTable;
import com.nz.simplecrud.service.DataAccessService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.nz.simplecrud.service.UserService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SelectableDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

/**
 * Custom Lazy User DataModel which extends PrimeFaces LazyDataModel. For more information please
 * visit http://www.primefaces.org/showcase-labs/ui/datatableLazy.jsf
 */
public class LazyUserDataModel extends LazyDataModel<UserTable> implements Serializable,
    SelectableDataModel<UserTable> {

  // Data Source for binding data to the DataTable
  private List<UserTable> datasource;

  // Selected Page size in the DataTable
  private int pageSize;

  // Current row index number
  private int rowIndex;

  // Total row number
  private int rowCount;

  // Data Access Service for create read update delete operations
  private DataAccessService crudService;

  /**
   * @param crudService
   */
  public LazyUserDataModel(DataAccessService crudService) {
    this.crudService = crudService;
  }

  /**
   * Checks if the row is available
   * @return boolean
   */
  @Override
  public boolean isRowAvailable() {
    if (datasource == null) {
      return false;
    }
    int index = rowIndex % pageSize;
    return index >= 0 && index < datasource.size();
  }

  @Override
  public List<UserTable> load(int first, int pageSize, String sortField, SortOrder sortOrder,
      Map<String,
          Object> filters) {
    return datasource;
  }

  @Override
  public List<UserTable> load(int first, int pageSize, List<SortMeta> multiSortMeta,
      Map<String, Object> filters) {
    return datasource;
  }

  /**
   * Gets the user object's primary key
   * @param userTable
   * @return Object
   */
  @Override
  public Object getRowKey(UserTable userTable) {
    return userTable.getId().toString();
  }

  /**
   * Returns the user object at the specified position in datasource.
   * @return
   */
  @Override
  public UserTable getRowData() {
    if (datasource == null) {
      return null;
    }
    int index = rowIndex % pageSize;
    if (index > datasource.size()) {
      return null;
    }
    return datasource.get(index);
  }

  /**
   * Returns the user object that has the row key.
   * @param rowKey
   * @return
   */
  @Override
  public UserTable getRowData(String rowKey) {
    if (datasource == null) {
      return null;
    }
    for (UserTable userTable : datasource) {
      if (userTable.getId().toString().equals(rowKey)) {
        return userTable;
      }
    }
    return null;
  }

    
    /*
     * ===== Getters and Setters of LazyUserDataModel fields
     */

  /**
   * @param pageSize
   */
  @Override
  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  /**
   * Returns page size
   * @return int
   */
  @Override
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Returns current row index
   * @return int
   */
  @Override
  public int getRowIndex() {
    return this.rowIndex;
  }

  /**
   * Sets row index
   * @param rowIndex
   */
  @Override
  public void setRowIndex(int rowIndex) {
    this.rowIndex = rowIndex;
  }

  /**
   * Sets row count
   * @param rowCount
   */
  @Override
  public void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  /**
   * Returns row count
   * @return int
   */
  @Override
  public int getRowCount() {
    return this.rowCount;
  }

  /**
   * Sets wrapped data
   * @param list
   */
  @Override
  public void setWrappedData(Object list) {
    this.datasource = (List<UserTable>) list;
  }

  /**
   * Returns wrapped data
   * @return
   */
  @Override
  public Object getWrappedData() {
    return datasource;
  }
}
                    
