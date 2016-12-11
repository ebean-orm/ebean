package org.tests.model.embedded;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class EAddress {

  String street;

  String suburb;

  String city;

  @Enumerated(EnumType.STRING)
  EAddressStatus status;

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getSuburb() {
    return suburb;
  }

  public void setSuburb(String suburb) {
    this.suburb = suburb;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public EAddressStatus getStatus() {
    return status;
  }

  public void setStatus(EAddressStatus status) {
    this.status = status;
  }
}
