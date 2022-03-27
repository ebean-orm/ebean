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
  private Car car;

  public CarAccessory(Car car, CarFuse fuse) {
    this.car = car;
    this.fuse = fuse;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Car getCar() {
    return car;
  }

  public void setCar(Car car) {
    this.car = car;
  }

  public CarFuse getFuse() {
    return fuse;
  }

  public void setFuse(CarFuse fuse) {
    this.fuse = fuse;
  }
}
