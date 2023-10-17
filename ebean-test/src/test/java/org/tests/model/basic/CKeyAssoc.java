package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CKeyAssoc {

  @Id
  Integer id;

  String assocOne;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getAssocOne() {
    return assocOne;
  }

  public void setAssocOne(String assocOne) {
    this.assocOne = assocOne;
  }

}
