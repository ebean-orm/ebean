package org.tests.inheritance.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;

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
