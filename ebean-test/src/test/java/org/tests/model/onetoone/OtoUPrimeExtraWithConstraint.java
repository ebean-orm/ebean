package org.tests.model.onetoone;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.util.UUID;

@Entity
public class OtoUPrimeExtraWithConstraint {

  @Id
  UUID eid;

  String extra;

  @Version
  Long version;

  public OtoUPrimeExtraWithConstraint(String extra) {
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

}
