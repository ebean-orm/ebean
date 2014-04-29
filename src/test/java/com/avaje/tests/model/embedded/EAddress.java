package com.avaje.tests.model.embedded;

import javax.persistence.Embeddable;

@Embeddable
public class EAddress {

  String street;
  
  String suburb;
  
  String city;

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

}
