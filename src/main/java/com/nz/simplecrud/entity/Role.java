package com.nz.simplecrud.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * @author Emre Simtay <emre@simtay.com>
 */
@Entity
@NamedQueries(value = @NamedQuery(name = Role.ALL, query = "SELECT r FROM Role r"))
@Table(schema = "SIMPLECRUD_DB", name = "ROLE")
public class Role extends BaseEntity implements Serializable {

  public final static String ALL = "Role.populateRoles";

  private String roledesc;

  private String rolename;

  public Role() {
  }

  public Role(Integer roleid, String rolename) {
    this.rolename = rolename;
  }

  @ManyToMany(mappedBy = "roles")
  private List<UserTable> userTables;

  @Column(name = "ROLEDESC")
  public String getRoledesc() {
    return this.roledesc;
  }

  public void setRoledesc(String roledesc) {
    this.roledesc = roledesc;
  }

  @Column(name = "ROLENAME")
  public String getRolename() {
    return this.rolename;
  }

  public void setRolename(String rolename) {
    this.rolename = rolename;
  }

  public List<UserTable> getUserTables() {
    return userTables;
  }

  public void setUserTables(List<UserTable> userTables) {
    this.userTables = userTables;
  }
}