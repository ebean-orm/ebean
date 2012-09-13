package com.avaje.tests.model.basic;

import java.sql.Timestamp;

import javax.persistence.Entity;

@Entity
public class NoIdEntityType {

  String name;
  
  Timestamp someDateTime;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Timestamp getSomeDateTime() {
    return someDateTime;
  }

  public void setSomeDateTime(Timestamp someDateTime) {
    this.someDateTime = someDateTime;
  }
  
  
}
