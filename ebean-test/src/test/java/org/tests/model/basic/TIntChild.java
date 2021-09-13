package org.tests.model.basic;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Entity
@Inheritance
@DiscriminatorValue("2")
public class TIntChild extends TIntRoot {

  private static final long serialVersionUID = 1L;

  String childProperty;

  public String getChildProperty() {
    return childProperty;
  }

  public void setChildProperty(String childProperty) {
    this.childProperty = childProperty;
  }

}
