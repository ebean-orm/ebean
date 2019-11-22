package org.tests.model.interfaces;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import io.ebean.annotation.EntityImplements;

@Entity
@EntityImplements(IAddress.class)
public class Address implements IAddress {

  @Id
  private long oid;

  @Version
  private int version;

  private String street;

  @ManyToOne
  private Person extraAddress;

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
  public String getStreet() {
    return street;
  }

  @Override
  public void setStreet(String s) {
    this.street = s;
  }

}
