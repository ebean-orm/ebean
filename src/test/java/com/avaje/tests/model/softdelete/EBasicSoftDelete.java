package com.avaje.tests.model.softdelete;

import javax.persistence.Entity;

@Entity
public class EBasicSoftDelete extends BaseSoftDelete {

  String name;

  String description;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
