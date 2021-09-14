package org.tests.model.embedded;

import org.tests.model.basic.Country;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

@Embeddable
public class EAddr {

  String street;

  String suburb;

  String city;

  @ManyToOne
  Country country;

  public EAddr(String street, String city, Country country) {
    this.street = street;
    this.city = city;
    this.country = country;
  }

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

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }
}
