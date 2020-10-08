package org.tests.model.basic;

import javax.persistence.Entity;

@Entity
public class EOptOneC extends BasicDomain {

  private static final long serialVersionUID = 1L;

  String nameForC;

  public String getNameForC() {
    return nameForC;
  }

  public void setNameForC(String nameForA) {
    this.nameForC = nameForA;
  }

}
