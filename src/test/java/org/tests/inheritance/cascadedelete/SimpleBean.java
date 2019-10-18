package org.tests.inheritance.cascadedelete;

import javax.persistence.Entity;

@Entity
public class SimpleBean extends RootBean {

  private String value;

  public SimpleBean(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
