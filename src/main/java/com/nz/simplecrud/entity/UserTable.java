package com.nz.simplecrud.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.*;

/**
 * @author Emre Simtay <emre@simtay.com>
 */
@Entity
@NamedQueries({
    @NamedQuery(name = UserTable.ALL, query = "SELECT u FROM UserTable u "),
    @NamedQuery(name = UserTable.TOTAL, query = "SELECT COUNT(u) FROM UserTable u")})
@Table(schema = "SIMPLECRUD_DB", name = "USERTABLE")
public class UserTable extends BaseEntity implements Serializable {

  public final static String ALL = "UserTable.populateUsers";

  public final static String TOTAL = "UserTable.countUsersTotal";

  @Column(nullable = false, length = 50)
  private String username;

  @Column(length = 50)
  private String firstname;

  @Column(length = 50)
  private String lastname;

  @Column(length = 50)
  private String email;

  @Column(length = 64)
  private String password;

  @ManyToMany
  @JoinTable(name = "USER_ROLES", schema = "SIMPLECRUD_DB", joinColumns = {
      @JoinColumn(name = "USER_USERID")}, inverseJoinColumns = {
      @JoinColumn(name = "ROLE_ROLEID")})
  private List<Role> roles = new ArrayList<Role>();

  public UserTable() {
    roles = new ArrayList<Role>();
    files = new ArrayList<>();
  }

  @ManyToMany
  @JoinTable(name = "USER_FILE", schema = "SIMPLECRUD_DB", joinColumns = {
      @JoinColumn(name = "USER_USERID")}, inverseJoinColumns = {
      @JoinColumn(name = "FILE_FILEID")})
  private List<File> files = new ArrayList<>();

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstname() {
    return this.firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return this.lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<Role> getRoles() {
    return roles;
  }

  public void setRoles(List<Role> roles) {
    this.roles = roles;
  }

  public List<File> getFiles() {
    return files;
  }

  public void setFiles(final List<File> files) {
    this.files = files;
  }
}