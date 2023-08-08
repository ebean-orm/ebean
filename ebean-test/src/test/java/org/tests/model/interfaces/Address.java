package org.tests.model.interfaces;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Address implements IAddress {

  @Id
  private long oid;

  @Version
  private int version;

  @ManyToOne(targetEntity=Person.class, optional=false)
  private IPerson person;

  private String street;

  public Address(String street, IPerson person) {
    this.street = street;
    this.person = person;
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

  @Override
  public String getStreet() {
    return street;
  }

  @Override
  public void setStreet(String s) {
    this.street = s;
  }

}
