package org.tests.inheritance.model;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractBaseClass {
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
