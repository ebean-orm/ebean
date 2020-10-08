package org.tests.inheritance.cache;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

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
