package com.nz.simplecrud.controller;

import com.nz.simplecrud.entity.Role;
import com.nz.simplecrud.entity.UserTable;
import com.nz.simplecrud.service.RoleService;
import com.nz.simplecrud.service.UserService;
import com.nz.simplecrud.util.DateUtility;
import com.nz.simplecrud.util.SHAConverter;
import org.apache.commons.httpclient.HttpURL;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.io.ByteArrayInputStream;
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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.servlet.AsyncContext;
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

  @Inject
  private UserController userController;

  private String username;

  private String password;

  private String code;

  private String state;

  private String error;

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

  public String phase2() {
    if (error != null) {
      logger.info("error=" + error);
      try {
        FacesContext.getCurrentInstance().getExternalContext().redirect("error.jsf");
      } catch (Exception e) {
        logger.log(Level.SEVERE, "phase2() Error: redirect failed!", e);
      }
      return "";
    }
    if (code != null) {
      HttpSession sess = (HttpSession) FacesContext.getCurrentInstance()
          .getExternalContext()
          .getSession(true);
      ServiceBuilder builder = new ServiceBuilder();
      OAuthService service = builder.provider(Google2Api.class)
          .apiKey(GOOGLE_CLIENT_ID)
          .callback("http://statscholars.com:28080/analytics_war/GoogleLogin.xhtml")
          .apiSecret("RdqxeyEtXd6WqJxnAP-I5T7g").scope("openid profile email")
          .build(); //Now build the call
      //Get the all important authorization code
      //Construct the access token
      Token token = service.getAccessToken(null, new Verifier(code));
      code = null;
      //Save the token for the duration of the session
      sess.setAttribute("token", token);

      //Now do something with it - get the user's G+ profile
      OAuthRequest oReq = new OAuthRequest(Verb.GET,
          "https://www.googleapis.com/oauth2/v2/userinfo");
      service.signRequest(token, oReq);
      Response oResp = oReq.send();

      //Read the result
      JsonReader reader = Json.createReader(new ByteArrayInputStream(
          oResp.getBody().getBytes()));
      JsonObject profile = reader.readObject();
      //Save the user details somewhere or associate it with
      String email = profile.getString("email");
      String userName = profile.getString("email");
      String firstName = profile.getString("given_name");
      String lastname = profile.getString("family_name");
      UserTable googleUser = new UserTable();
      googleUser.setEmail(email);
      googleUser.setUsername(userName);
      googleUser.setFirstname(firstName);
      googleUser.setLastname(lastname);
      SHAConverter shaConverter = new SHAConverter();
      Object pass = shaConverter.getAsObject(FacesContext.getCurrentInstance(), null,
          GOOGLE_CLIENT_ID + userName + email);
      googleUser.setPassword(pass.toString());
      List<UserTable> dsd = das.findByNativeQuery("select * from SIMPLECRUD_DB" +
          ".USERTABLE");
      final Boolean[] isGoogleUserFound = {false};
      for (UserTable userTable : dsd) {
        if (userTable != null && userTable.getUsername() != null && userTable.getUsername().equals
            (userName)) {
          isGoogleUserFound[0] = true;
        }
      }
      this.password = GOOGLE_CLIENT_ID + userName + email;
      this.username = userName;
      if (isGoogleUserFound[0]) {
        login();
      } else {
        userController.setNewUserTable(googleUser);
        userController.doCreateUser();
        userController.updateUserList();
        login();
      }
    } return null;
  }

  public void redirect() {
    String plainUrl = "https://accounts.google.com/o/oauth2/v2/auth?"
        + "scope=email%20profile&"
        + "redirect_uri=http://statscholars.com:28080/analytics_war/GoogleLogin.xhtml&"
        + "response_type=code&"
        + "client_id=" + GOOGLE_CLIENT_ID;
    try {
      FacesContext.getCurrentInstance().getExternalContext().redirect(plainUrl);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Listen for button clicks on the #{loginController.login} action, validates the username and
   * password entered by the user and navigates to the appropriate page.
   */
  public void login() {

    FacesContext context = FacesContext.getCurrentInstance();
    HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
    try {
      final String[] navigateString = {""};
      // Checks if username and password are valid if not throws a ServletException
      SHAConverter shaConverter = new SHAConverter();
      Object pass = shaConverter.getAsObject(FacesContext.getCurrentInstance(), null, password);

      List<UserTable> dsd = das.findByNativeQuery("select * from SIMPLECRUD_DB" +
          ".USERTABLE" +
          " where SIMPLECRUD_DB.USERTABLE.PASSWORD = '" + pass.toString() + "' AND SIMPLECRUD_DB" +
          ".USERTABLE.USERNAME = '" + getUsername() + "'");
      loggedUser = dsd.get(0);
      //      List<Role> roles = dasRole.findByNativeQuery("select * from SIMPLECRUD_DB" +
      //          ".ROLE where SIMPLECRUD_DB.ROLE.ID in (select SIMPLECRUD_DB.USER_ROLES.ROLE_ROLEID from" +
      //          " SIMPLECRUD_DB.USER_ROLES " +
      //          " where SIMPLECRUD_DB.USER_ROLES.USER_USERID = " + dsd.get(0).getId() + ")");
      //      request.login(username, password);
      // gets the user principle and navigates to the appropriate page
      //      Principal principal = request.getUserPrincipal();
      navigateString[0] = "/user/UserHome.xhtml";
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
        } else {
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

  public String getCode() {
    return code;
  }

  public void setCode(final String code) {
    this.code = code;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getError() {
    return error;
  }

  public void setError(final String error) {
    this.error = error;
  }

  private static final String GOOGLE_CLIENT_ID
      = "67413742665-uj4981bhb10ksne63eotc8vn85dscoq2.apps.googleusercontent.com";
}
