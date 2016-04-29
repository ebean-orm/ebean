package com.avaje.tests.model.basic.cache;

import com.avaje.ebean.annotation.Cache;
import com.avaje.tests.model.basic.BasicDomain;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Cache
@Entity
@Inheritance
@DiscriminatorColumn(length = 3)
public abstract class CInhRoot extends BasicDomain {

  private String licenseNumber;

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }
}
