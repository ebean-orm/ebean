package org.tests.model.cache;

import io.ebean.Model;
import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Cache(enableQueryCache = true)
@Entity
@Table(name = "e_col_ab")
public class EColAB extends Model {

  @Id
  private Integer id;

  private String columnA;

  private String columnB;

  public EColAB(String columnA, String columnB) {
    this.columnA = columnA;
    this.columnB = columnB;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getColumnA() {
    return columnA;
  }

  public void setColumnA(String columnA) {
    this.columnA = columnA;
  }

  public String getColumnB() {
    return columnB;
  }

  public void setColumnB(String columnB) {
    this.columnB = columnB;
  }
}
