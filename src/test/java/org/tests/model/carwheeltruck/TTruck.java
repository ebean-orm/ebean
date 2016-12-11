package org.tests.model.carwheeltruck;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("truck")
public class TTruck extends TCar {

  @Column(name = "truckLoad")
  Long load;

  public Long getLoad() {
    return load;
  }

  public void setLoad(Long load) {
    this.load = load;
  }

}
