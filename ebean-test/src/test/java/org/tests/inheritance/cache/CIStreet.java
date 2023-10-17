package org.tests.inheritance.cache;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "street")
@DiscriminatorValue(value = "1")
public class CIStreet extends CIStreetParent {

  @Column(name="num")
  protected String number;

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }
}
