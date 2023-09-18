package org.tests.model.onetoone;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class OtoUPrimeOptionalExtra {

  @Id
  UUID eid;

  String extra;

  @OneToOne(optional = false)
  @PrimaryKeyJoinColumn
  private OtoUPrime prime;

  @Version
  Long version;

  public OtoUPrimeOptionalExtra(String extra) {
    this.extra = extra;
  }

  @Override
  public String toString() {
    return "exId:" + eid + " " + extra;
  }

  public UUID getEid() {
    return eid;
  }

  public void setEid(UUID eid) {
    this.eid = eid;
  }

  public String getExtra() {
    return extra;
  }

  public void setExtra(String extra) {
    this.extra = extra;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public OtoUPrime getPrime() {
    return prime;
  }

  public void setPrime(OtoUPrime prime) {
    this.prime = prime;
  }
}
