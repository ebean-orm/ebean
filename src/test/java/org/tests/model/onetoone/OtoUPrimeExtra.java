package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.UUID;

@Entity
public class OtoUPrimeExtra {

  @Id
  UUID eid;

  String extra;

  @Version
  Long version;

  public OtoUPrimeExtra(String extra) {
    this.extra = extra;
  }

  @Override
  public String toString() {
    return "exId:"+ eid +" "+extra;
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
