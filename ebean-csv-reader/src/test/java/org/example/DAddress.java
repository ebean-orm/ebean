package org.example;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import static javax.persistence.CascadeType.PERSIST;

@Entity
public class DAddress extends BaseEntity {

  String line1;

  String line2;
  String city;

  @ManyToOne(optional = false)
  Country country;

  public String line1() {
    return line1;
  }

  public DAddress line1(String line1) {
    this.line1 = line1;
    return this;
  }

  public String line2() {
    return line2;
  }

  public DAddress line2(String line2) {
    this.line2 = line2;
    return this;
  }

  public String city() {
    return city;
  }

  public DAddress city(String city) {
    this.city = city;
    return this;
  }

  public Country country() {
    return country;
  }

  public DAddress country(Country country) {
    this.country = country;
    return this;
  }
}
