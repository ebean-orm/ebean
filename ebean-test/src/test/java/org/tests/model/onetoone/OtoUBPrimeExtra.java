package org.tests.model.onetoone;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class OtoUBPrimeExtra {

  @Id
  UUID eid;

  String extra;

  /**
   * Child side of bi-directional PrimaryJoinColumn.
   */
  @OneToOne(optional = false)
  @PrimaryKeyJoinColumn
  OtoUBPrime prime;

  @Version
  Long version;

  public OtoUBPrimeExtra(String extra) {
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

  public OtoUBPrime getPrime() {
    return prime;
  }

  public void setPrime(OtoUBPrime prime) {
    this.prime = prime;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
