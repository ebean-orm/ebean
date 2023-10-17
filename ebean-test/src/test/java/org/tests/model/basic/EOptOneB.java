package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class EOptOneB extends BasicDomain {

  private static final long serialVersionUID = 1L;

  String nameForB;

  @ManyToOne(optional = false)
  EOptOneC c;

  public String getNameForB() {
    return nameForB;
  }

  public void setNameForB(String nameForA) {
    this.nameForB = nameForA;
  }

  public EOptOneC getC() {
    return c;
  }

  public void setC(EOptOneC c) {
    this.c = c;
  }

}
