package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;

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
