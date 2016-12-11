package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class VehicleDriver extends BasicDomain {

  private static final long serialVersionUID = 1L;

  private String name;

  @ManyToOne(optional = true)
  private Vehicle vehicle;

  @ManyToOne(optional = true)
  private Address address;

  private Date licenseIssuedOn;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    this.address = address;
  }

  public Date getLicenseIssuedOn() {
    return licenseIssuedOn;
  }

  public void setLicenseIssuedOn(Date licenseIssuedOn) {
    this.licenseIssuedOn = licenseIssuedOn;
  }

}
