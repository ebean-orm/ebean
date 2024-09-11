package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.util.Date;

@Entity
public final class Vehicle extends BasicDomain {

  private static final long serialVersionUID = -3060920549470002030L;

  private String licenseNumber;

  private Date registrationDate;

  // @Transient
  private transient String testTransient;

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
}
