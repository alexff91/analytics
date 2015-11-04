package com.nz.simplecrud.entity;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;

/**
 * Super Entity class
 * @author Emre Simtay <emre@simtay.com>
 */
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

  private static final long serialVersionUID = 1L;

  @SequenceGenerator(name = "Emp_Gen", sequenceName = "Emp_Seq")
  @Id
  @GeneratedValue(generator = "Emp_Gen")
  private Integer id;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (id != null ? id.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {

    if (obj == null) {
      return false;
    } else if (!(obj instanceof BaseEntity)) {
      return false;
    } else if (((BaseEntity) obj).id.equals(this.id)) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public String toString() {
    return "entity." + this.getClass() + "[ id=" + id + " ] ";
  }
}
