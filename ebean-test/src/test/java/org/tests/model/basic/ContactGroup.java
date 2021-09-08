package org.tests.model.basic;

import javax.persistence.Entity;

@Entity
public class ContactGroup extends BasicDomain {

  private static final long serialVersionUID = -5447111032760796085L;

  String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
