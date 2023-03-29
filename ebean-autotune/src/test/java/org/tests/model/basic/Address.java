package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "o_address")
public class Address extends BaseModel {

  String line1;
  String line2;
  String city;

  public Address(String line1, String city) {
    this.line1 = line1;
    this.city = city;
  }

  public String getLine1() {
    return line1;
  }

  public void setLine1(String line1) {
    this.line1 = line1;
  }

  public String getLine2() {
    return line2;
  }

  public void setLine2(String line2) {
    this.line2 = line2;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }
}
