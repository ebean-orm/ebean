package org.tests.model.carwheeltruck;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.validation.constraints.Size;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
@DiscriminatorValue("car")
public class TCar {

  @Id
  @Size(max=32)
  String plateNo;

  @OneToMany(cascade = CascadeType.ALL)
  List<TWheel> wheels;

  public String getPlateNo() {
    return plateNo;
  }

  public void setPlateNo(String plateNo) {
    this.plateNo = plateNo;
  }

  public List<TWheel> getWheels() {
    return wheels;
  }

  public void setWheels(List<TWheel> wheels) {
    this.wheels = wheels;
  }

}
