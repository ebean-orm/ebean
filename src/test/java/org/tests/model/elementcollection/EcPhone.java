package org.tests.model.elementcollection;

import javax.persistence.Embeddable;
import javax.validation.constraints.Size;

@Embeddable
public class EcPhone {

  @Size(max = 2)
  String countryCode;

  @Size(max = 6)
  String area;

  @Size(max = 20)
  String number;

  public EcPhone(String countryCode, String area, String number) {
    this.countryCode = countryCode;
    this.area = area;
    this.number = number;
  }

  @Override
  public String toString() {
    return countryCode + "-" + area + "-" + number;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getArea() {
    return area;
  }

  public void setArea(String area) {
    this.area = area;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }
}
