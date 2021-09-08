package org.tests.inheritance.cache;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue(value = "0")
public class CICustomerParent extends CIBaseModel {

  @ManyToOne
  protected CIAddress address;

  public CIAddress getAddress() {
    return address;
  }

  public void setAddress(CIAddress adress) {
    this.address = adress;
  }
}
