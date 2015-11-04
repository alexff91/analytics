package com.nz.simplecrud.controller;

import com.nz.simplecrud.entity.File;
import com.nz.simplecrud.entity.Role;
import com.nz.simplecrud.entity.UserTable;
import com.nz.simplecrud.service.FileService;
import com.nz.simplecrud.service.RoleService;
import com.nz.simplecrud.service.UserService;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;

/**
 * User Controller class allows users to do CRUD operations
 * @author Emre Simtay <emre@simtay.com>
 */

@Named
@SessionScoped
public class UserController implements Serializable {

  private
  @Inject
  transient Logger logger;

  private
  @Inject
  RoleService dasRole;

  @Inject
  LoginController loginController;

  private
  @Inject
  FileService fileService;

  private
  @Inject
  UserService das;

  // Selected users that will be removed
  private UserTable[] selectedUserTables;

  // Lazy loading user list
  private LazyUserDataModel lazyModel;

  // Creating new user
  private UserTable newUserTable = new UserTable();

  // Selected user that will be updated
  private UserTable selectedUserTable = new UserTable();

  // Available role list
  private List<Role> roleList;

  /**
   * Default constructor
   */
  public UserController() {

  }

  /**
   * Initializing Data Access Service for LazyUserDataModel class role list for UserContoller class
   */
  @PostConstruct
  public void init() {
    logger.log(Level.INFO, "UserController is initializing");
    lazyModel = new LazyUserDataModel(das);
    List<UserTable> dsd = das.findByNativeQuery("select * from SIMPLECRUD_DB" +
            ".USERTABLE"
    );
    lazyModel.setWrappedData(dsd);
    roleList = dasRole.findByNativeQuery("select * from SIMPLECRUD_DB" +
        ".ROLE");

    fileList = fileService.findByNativeQuery("select * from SIMPLECRUD_DB" +
        ".FILE WHERE SIMPLECRUD_DB.FILE.ID in ( select SIMPLECRUD_DB.USER_FILES.FILE_FILEID " +
        "from SIMPLECRUD_DB.USER_FILES " +
        "where SIMPLECRUD_DB.USER_FILES.USER_USERID =  " + loginController.getLoggedUser().getId() +
        " )");
    //        roleList = das.findWithNamedQuery(Role.ALL);
  }

  public void updateUserList() {
    logger.log(Level.INFO, "UserController is initializing");
    lazyModel = new LazyUserDataModel(das);
    List<UserTable> dsd = das.findByNativeQuery("select * from SIMPLECRUD_DB" +
            ".USERTABLE"
    );
    lazyModel.setWrappedData(dsd);
    roleList = dasRole.findByNativeQuery("select * from SIMPLECRUD_DB" +
        ".ROLE");
  }

  /**
   * Create, Update and Delete operations
   */
  public void doCreateUser() {
    das.create(newUserTable);
    updateUserList();
  }

  /**
   * @param actionEvent
   */
  public void doUpdateUser(ActionEvent actionEvent) {
    das.update(selectedUserTable);
    updateUserList();
  }

  /**
   * @param actionEvent
   */
  public void doDeleteUsers(ActionEvent actionEvent) {
    das.deleteItems(selectedUserTables);
    updateUserList();
  }

  /**
   * Getters, Setters
   * @return
   */

  public UserTable getSelectedUserTable() {
    return selectedUserTable;
  }

  /**
   * @param selectedUserTable
   */
  public void setSelectedUserTable(UserTable selectedUserTable) {
    this.selectedUserTable = selectedUserTable;
  }

  /**
   * @return
   */
  public UserTable[] getSelectedUserTables() {
    return selectedUserTables;
  }

  /**
   * @param selectedUserTables
   */
  public void setSelectedUserTables(UserTable[] selectedUserTables) {
    this.selectedUserTables = selectedUserTables;
  }

  /**
   * @return
   */
  public UserTable getNewUserTable() {
    return newUserTable;
  }

  /**
   * @param newUserTable
   */
  public void setNewUserTable(UserTable newUserTable) {
    this.newUserTable = newUserTable;
  }

  /**
   * @return LazyDataModel
   */
  public LazyDataModel<UserTable> getLazyModel() {

    return lazyModel;
  }

  /**
   * @return List<Role>
   */
  public List<Role> getRoleList() {
    return roleList;
  }

  /**
   * @param roleList
   */
  public void setRoleList(List<Role> roleList) {
    this.roleList = roleList;
  }

  private List<File> fileList;

  public void setFileList(List<File> fileList) {
    this.fileList = fileList;
  }

  public List<File> getFileList() {
    fileList = fileService.findByNativeQuery("select * from SIMPLECRUD_DB" +
        ".FILE WHERE SIMPLECRUD_DB.FILE.ID in ( select SIMPLECRUD_DB.USER_FILES.FILE_FILEID " +
        "from SIMPLECRUD_DB.USER_FILES " +
        "where SIMPLECRUD_DB.USER_FILES.USER_USERID =  " + loginController.getLoggedUser().getId() +
        " )");
    return fileList;
  }
}
                    