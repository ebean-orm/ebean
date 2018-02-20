package org.tests.model.onetoone;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class OtoCustAddress {

  @Id
  long aid;

  String line1;
  String line2;
  String line3;

  @OneToOne
  OtoCust customer;

  @Version
  long version;

  public OtoCustAddress(String line1, String line2) {
    this.line1 = line1;
    this.line2 = line2;
  }

  public long getAid() {
    return aid;
  }

  public void setAid(long aid) {
    this.aid = aid;
  }

  public OtoCust getCustomer() {
    return customer;
  }

  public void setCustomer(OtoCust customer) {
    this.customer = customer;
  }

  public String getLine1() {
    return line1;
  }

  public void setLine1(String line1) {
    this.line1 = line1;
  }

  public String getLine2() {
    return line2;
  }

  public void setLine2(String line2) {
    this.line2 = line2;
  }

  public String getLine3() {
    return line3;
  }

  public void setLine3(String line3) {
    this.line3 = line3;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
