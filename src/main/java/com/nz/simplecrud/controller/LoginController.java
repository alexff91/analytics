package com.nz.simplecrud.controller;

import com.nz.simplecrud.entity.Role;
import com.nz.simplecrud.entity.UserTable;
import com.nz.simplecrud.service.RoleService;
import com.nz.simplecrud.service.UserService;
import com.nz.simplecrud.util.DateUtility;
import com.nz.simplecrud.util.SHAConverter;

import java.io.IOException;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Login Controller class allows only authenticated users to log in to the web application.
 * @author Emre Simtay <emre@simtay.com>
 */
@Named
@SessionScoped
public class LoginController implements Serializable {

  @Inject
  private transient Logger logger;

  private String username;

  private String password;

  public UserTable getLoggedUser() {
    return loggedUser;
  }

  public boolean isUserAdmin() {
    return isUserAdmin;
  }

  public void setUserAdmin(final boolean isUserAdmin) {
    this.isUserAdmin = isUserAdmin;
  }

  boolean isUserAdmin = false;

  public void setLoggedUser(final UserTable loggedUser) {
    this.loggedUser = loggedUser;
  }

  private UserTable loggedUser;

  /**
   * Creates a new instance of LoginController
   */
  public LoginController() {
  }

  //  Getters and Setters

  /**
   * @return username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return password
   */
  public String getPassword() {
    return password;
  }

  private
  @Inject
  UserService das;

  private
  @Inject
  RoleService dasRole;

  /**
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Listen for button clicks on the #{loginController.login} action, validates the username and
   * password entered by the user and navigates to the appropriate page.
   * @param actionEvent
   */
  public void login(ActionEvent actionEvent) {

    FacesContext context = FacesContext.getCurrentInstance();
    HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
    try {
      final String[] navigateString = {""};
      // Checks if username and password are valid if not throws a ServletException
      SHAConverter shaConverter = new SHAConverter();
      Object pass = shaConverter.getAsObject(FacesContext.getCurrentInstance(), null, password);

      List<UserTable> dsd = das.findByNativeQuery("select * from SIMPLECRUD_DB" +
          ".USERTABLE" +
          " where SIMPLECRUD_DB.USERTABLE.PASSWORD = '" + pass.toString() + "'");
      loggedUser = dsd.get(0);
      //      List<Role> roles = dasRole.findByNativeQuery("select * from SIMPLECRUD_DB" +
      //          ".ROLE where SIMPLECRUD_DB.ROLE.ID in (select SIMPLECRUD_DB.USER_ROLES.ROLE_ROLEID from" +
      //          " SIMPLECRUD_DB.USER_ROLES " +
      //          " where SIMPLECRUD_DB.USER_ROLES.USER_USERID = " + dsd.get(0).getId() + ")");
      //      request.login(username, password);
      // gets the user principle and navigates to the appropriate page
      //      Principal principal = request.getUserPrincipal();
      for (Role role : dsd.get(0).getRoles()) {
        if (role.getRoledesc().equals("Administrator")) {
          isUserAdmin = true;
          navigateString[0] = "/admin/AdminHome.xhtml";
          //        } else if (role.getRoledesc().equals("Manager")) {
          //          isUserAdmin=false;
          //          navigateString[0] = "/manager/ManagerHome.xhtml";
        } else if (role.getRoledesc().equals("User")) {
          isUserAdmin = false;
          navigateString[0] = "/user/UserHome.xhtml";
        }
      } ;

      try {
        logger.log(Level.INFO, "User ({0}) loging in #" + DateUtility.getCurrentDateTime(),
            dsd.get(0).getFirstname());
        context.getExternalContext().redirect(request.getContextPath() + navigateString[0]);
      } catch (IOException ex) {
        logger.log(Level.SEVERE,
            "IOException, Login Controller" + "Username : " + dsd.get(0).getFirstname(), ex);
        context.addMessage(null, new FacesMessage("Error!", "Exception occured"));
      }
    } catch (Exception e) {
      logger.log(Level.SEVERE, e.toString(), e);
      context.addMessage(null, new FacesMessage("Error!",
          "The username or password you provided does not match our records."));
    }
  }

  /**
   * Listen for logout button clicks on the #{loginController.logout} action and navigates to login
   * screen.
   */
  public void logout() {
    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext()
        .getSession(false);
    HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance()
        .getExternalContext().getRequest();
    logger.log(Level.INFO, "User ({0}) loging out #" + DateUtility.getCurrentDateTime(),
        loggedUser.getLastname());
    if (session != null) {
      session.invalidate();
    }
    loggedUser = null;
    isUserAdmin = false;
    FacesContext.getCurrentInstance().getApplication().getNavigationHandler()
        .handleNavigation(FacesContext.getCurrentInstance(), null,
            "/Login.xhtml?faces-redirect=true");
  }
}
