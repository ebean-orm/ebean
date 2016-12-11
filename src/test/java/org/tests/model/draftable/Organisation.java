package org.tests.model.draftable;

import javax.persistence.Entity;

@Entity
public class Organisation extends BaseDomain {

  String name;

  public Organisation(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
