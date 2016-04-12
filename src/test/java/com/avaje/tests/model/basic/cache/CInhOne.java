package com.avaje.tests.model.basic.cache;

import com.avaje.ebean.annotation.CacheStrategy;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@CacheStrategy
@Entity
@Inheritance
@DiscriminatorValue("O")
public class CInhOne extends CInhRoot {

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
