package org.tests.model.interfaces;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Person implements IPerson {
  @Id
  private long oid;

  @Version
  private int version;

  @ManyToOne(targetEntity = Address.class)
  private IAddress defaultAddress;

  @Override
  public IAddress getDefaultAddress() {
    return defaultAddress;
  }

  @Override
  public void setDefaultAddress(IAddress address) {
    this.defaultAddress = address;
  }

  public long getOid() {
    return oid;
  }

  public void setOid(long oid) {
    this.oid = oid;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}
