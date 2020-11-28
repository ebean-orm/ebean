package org.tests.inheritance.order;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;

@Entity
@Inheritance
public abstract class OrderedParent {

  @Id
  Integer id;

  String commonName;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(final String commonName) {
    this.commonName = commonName;
  }
}
