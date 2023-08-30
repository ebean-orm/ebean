package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class CarAccessory extends BasicDomain {

  private static final long serialVersionUID = 1L;

  private String name;

  @ManyToOne(optional = false)
  private CarFuse fuse;

  @ManyToOne
  private Vehicle car;

  public CarAccessory(Vehicle car, CarFuse fuse) {
    this.car = car;
    this.fuse = fuse;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Vehicle getCar() {
    return car;
  }

  public void setCar(Vehicle car) {
    this.car = car;
  }

  public CarFuse getFuse() {
    return fuse;
  }

  public void setFuse(CarFuse fuse) {
    this.fuse = fuse;
  }
}
