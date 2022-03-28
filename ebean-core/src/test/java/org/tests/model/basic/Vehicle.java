package org.tests.model.basic;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
@Inheritance
@DiscriminatorColumn(length = 3)
public abstract class Vehicle extends BasicDomain {

  private static final long serialVersionUID = -3060920549470002030L;

  private String licenseNumber;

  private Date registrationDate;

  // @Transient
  private transient String testTransient;

  @ManyToOne
  private VehicleLease lease;

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }

  public Date getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(Date registrationDate) {
    this.registrationDate = registrationDate;
  }

  public String getTestTransient() {
    return testTransient;
  }

  public void setTestTransient(String testTransient) {
    this.testTransient = testTransient;
  }

  public VehicleLease getLease() {
    return lease;
  }

  public void setLease(VehicleLease lease) {
    this.lease = lease;
  }
}
