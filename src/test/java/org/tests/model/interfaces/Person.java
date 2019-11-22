package org.tests.model.interfaces;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import io.ebean.annotation.EntityImplements;
import io.ebean.annotation.PrivateOwned;

@Entity
@EntityImplements(IPerson.class)
public class Person implements IPerson {
  @Id
  private long oid;

  @Version
  private int version;

  @ManyToOne(cascade = CascadeType.PERSIST)
  private IAddress defaultAddress;

  @OneToMany(cascade = CascadeType.PERSIST)
  @PrivateOwned
  private List<IAddress> extraAddresses = new ArrayList<>();

  @ManyToMany(cascade = CascadeType.PERSIST)
  private List<IAddress> addressLinks = new ArrayList<>();


  @Override
  public IAddress getDefaultAddress() {
    return defaultAddress;
  }

  @Override
  public void setDefaultAddress(IAddress address) {
    this.defaultAddress = address;
  }

  @Override
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

  @Override
  public List<IAddress> getExtraAddresses() {
    return extraAddresses;
  }

  @Override
  public List<IAddress> getAddressLinks() {
    return addressLinks;
  }
}
