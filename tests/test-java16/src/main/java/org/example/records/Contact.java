package org.example.records;

import io.ebean.Model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class Contact extends Model {

  @Id
  private long id;

  @Version
  private long version;

  private final String name;

  @Embedded(prefix = "home_")
  private Address homeAddress;

  @Embedded(prefix = "work_")
  private Address workAddress;

  public Contact(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Address getHomeAddress() {
    return homeAddress;
  }

  public void setHomeAddress(Address homeAddress) {
    this.homeAddress = homeAddress;
  }

  public Address getWorkAddress() {
    return workAddress;
  }

  public void setWorkAddress(Address workAddress) {
    this.workAddress = workAddress;
  }

  public long getId() {
    return id;
  }

  public long getVersion() {
    return version;
  }
}
