package org.tests.inheritance.cache;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

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
