package org.tests.model.m2o;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class Empl {

  @Id
  Long id;

  String name;

  Integer age;

  @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
  List<Addr> addresses;

  @ManyToOne(cascade = CascadeType.ALL)
  Addr defaultAddress;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public List<Addr> getAddresses() {
    return addresses;
  }

  public void setAddresses(List<Addr> addresses) {
    this.addresses = addresses;
  }

  public Addr getDefaultAddress() {
    return defaultAddress;
  }

  public void setDefaultAddress(Addr defaultAddress) {
    this.defaultAddress = defaultAddress;
  }
}
