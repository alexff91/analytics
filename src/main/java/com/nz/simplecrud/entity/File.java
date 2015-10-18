package com.nz.simplecrud.entity;

import java.io.Serializable;
import java.util.List;
import javax.persistence.*;

/**
 * @author Emre Simtay <emre@simtay.com>
 */
@Entity
@NamedQueries(value = @NamedQuery(name = File.ALL, query = "SELECT f FROM File f"))
@Table(schema = "SIMPLECRUD_DB", name = "FILE")
public class File extends BaseEntity implements Serializable {

  public final static String ALL = "File.populateFiles";

  public File() {
  }

  @ManyToMany(mappedBy = "files")
  private List<UserTable> userTables;

  public List<UserTable> getUserTables() {
    return userTables;
  }

  public void setUserTables(List<UserTable> userTables) {
    this.userTables = userTables;
  }

  private String filename;

  @Column(name = "FILE_NAME")
  public String getFilename() {
    return filename;
  }

  public void setFilename(final String filename) {
    this.filename = filename;
  }
}