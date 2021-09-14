package org.tests.model.embedded;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class EPerAddr {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @Embedded(prefix = "ma_")
  EAddr address;

  public EPerAddr(String name, EAddr address) {
    this.name = name;
    this.address = address;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EAddr getAddress() {
    return address;
  }

  public void setAddress(EAddr address) {
    this.address = address;
  }
}
