package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;
import org.tests.model.basic.BasicDomain;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Inheritance;

@Cache(enableQueryCache = true)
@Entity
@Inheritance
@DiscriminatorColumn(length = 3)
public abstract class CInhRoot extends BasicDomain {
  private static final long serialVersionUID = -4673953370819311120L;

  private String licenseNumber;

  public String getLicenseNumber() {
    return licenseNumber;
  }

  public void setLicenseNumber(String licenseNumber) {
    this.licenseNumber = licenseNumber;
  }
}
