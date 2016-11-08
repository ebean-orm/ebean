package com.avaje.tests.model.carwheel;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "sa_car")
public class Car {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  protected Long id;

  @Version
  private int version;

  @OneToMany(mappedBy = "car", cascade = CascadeType.ALL)
  private List<Wheel> wheels;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public List<Wheel> getWheels() {
    return wheels;
  }

  public void setWheels(List<Wheel> wheels) {
    this.wheels = wheels;
  }

}
