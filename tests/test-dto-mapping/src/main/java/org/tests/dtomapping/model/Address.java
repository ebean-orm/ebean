package org.tests.dtomapping.model;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Address extends Model {

  @Id
  private Long id;

  private String line1;
  private String city;

  public Address(String line1, String city) {
    this.line1 = line1;
    this.city = city;
  }

  public Long getId() {
    return id;
  }

  public String getLine1() {
    return line1;
  }

  public void setLine1(String line1) {
    this.line1 = line1;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
