package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class OtoPrimeExtra {

  @Id
  Long eid;

  String extra;

  @Version
  Long version;

  public OtoPrimeExtra(String extra) {
    this.extra = extra;
  }

  @Override
  public String toString() {
    return "exId:"+ eid +" "+extra;
  }

  public Long getEid() {
    return eid;
  }

  public void setEid(Long eid) {
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
