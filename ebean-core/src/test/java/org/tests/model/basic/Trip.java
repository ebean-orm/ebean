package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class Trip extends BasicDomain {

  private static final long serialVersionUID = 1L;

  @ManyToOne
  private VehicleDriver vehicleDriver;

  private String destination;

  @ManyToOne
  private Address address;

  private Date starDate;

  public VehicleDriver getVehicleDriver() {
    return vehicleDriver;
  }

  public void setVehicleDriver(VehicleDriver vehicleDriver) {
    this.vehicleDriver = vehicleDriver;
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String destination) {
    this.destination = destination;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Date getStarDate() {
    return starDate;
  }

  public void setStarDate(Date starDate) {
    this.starDate = starDate;
  }


}
