package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Cache
@Entity
@Inheritance
@DiscriminatorValue("O")
public class CInhOne extends CInhRoot {
  private static final long serialVersionUID = -3933815364935720317L;

  private String driver;

  private String notes;

  public String getDriver() {
    return driver;
  }

  public void setDriver(String driver) {
    this.driver = driver;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
