package com.avaje.tests.json.transientproperties;


import com.avaje.ebean.annotation.Sql;

import javax.persistence.Entity;

@Sql
@Entity
public class ModelB {

  Integer oneField;

  Integer twoField;

  public Integer getOneField() {
    return oneField;
  }

  public void setOneField(Integer oneField) {
    this.oneField = oneField;
  }

  public Integer getTwoField() {
    return twoField;
  }

  public void setTwoField(Integer twoField) {
    this.twoField = twoField;
  }
}