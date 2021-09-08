package org.tests.model.carwheeltruck;

import io.ebean.annotation.SoftDelete;

import javax.persistence.*;
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

  @SoftDelete
  boolean deleted;

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
