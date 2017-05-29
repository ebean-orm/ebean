package org.tests.model.basic;

import io.ebean.annotation.DbEnumValue;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;

@Entity
@Inheritance
@DiscriminatorValue("T")
public class Truck extends Vehicle {
  private static final long serialVersionUID = 9195535931523211134L;

  public enum Size {
    SMALL("S"),
    MEDIUM("M"),
    LARGE("L"),
    HUGE("H");

    String value;

    Size(String value) {
      this.value = value;
    }

    @DbEnumValue
    public String value() {
      return value;
    }
  }

  @Column(name = "siz")
  private Size size;

  @ManyToOne
  TruckRef truckRef;

  private Double capacity;

  public Double getCapacity() {
    return capacity;
  }

  public void setCapacity(Double capacity) {
    this.capacity = capacity;
  }

  public TruckRef getTruckRef() {
    return truckRef;
  }

  public void setTruckRef(TruckRef truckRef) {
    this.truckRef = truckRef;
  }

  public Size getSize() {
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
  }
}
