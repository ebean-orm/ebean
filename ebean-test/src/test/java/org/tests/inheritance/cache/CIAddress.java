package org.tests.inheritance.cache;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class CIAddress extends CIBaseModel {

  @ManyToOne
  public CIStreetParent street;

  public CIStreetParent getStreet() {
    return street;
  }

  public void setStreet(CIStreetParent street) {
    this.street = street;
  }
}
