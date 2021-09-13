package org.tests.model.carwheeltruck;

import javax.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("truck")
public class TTruck extends TCar {

  @Column(name = "truckload")
  Long load;

  public Long getLoad() {
    return load;
  }

  public void setLoad(Long load) {
    this.load = load;
  }

}
