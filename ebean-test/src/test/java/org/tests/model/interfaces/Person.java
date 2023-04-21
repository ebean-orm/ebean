package org.tests.model.interfaces;

import javax.persistence.*;
import java.util.List;

@Entity
public class Person implements IPerson {
  @Id
  private long oid;

  @Version
  private int version;

  @OneToMany(targetEntity=Address.class)
  private List<IAddress> addresses;
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

  public List<IAddress> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<IAddress> addresses) {
    this.addresses = addresses;
  }
}
