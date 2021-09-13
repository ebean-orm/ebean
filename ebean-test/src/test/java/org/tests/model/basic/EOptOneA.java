package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class EOptOneA extends BasicDomain {

  private static final long serialVersionUID = 1L;

  String nameForA;

  @ManyToOne(optional = true)
  EOptOneB b;

  public String getNameForA() {
    return nameForA;
  }

  public void setNameForA(String nameForA) {
    this.nameForA = nameForA;
  }

  public EOptOneB getB() {
    return b;
  }

  public void setB(EOptOneB b) {
    this.b = b;
  }


}
