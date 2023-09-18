package org.tests.model;

import jakarta.persistence.Entity;

@Entity
public class EWhoProps extends EWhoPropsSuper {

  String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
