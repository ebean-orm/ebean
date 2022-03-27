package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CarFuse {

  @Id
  Long id;

  String locationCode;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getLocationCode() {
    return locationCode;
  }

  public void setLocationCode(String locationCode) {
    this.locationCode = locationCode;
  }
}
