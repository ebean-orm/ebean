package org.tests.insert;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class EStrIdBean {

  @Id
  String id;

  @Version
  long version; // = 1;

  private String name;

  public String id() {
    return id;
  }

  public EStrIdBean setId(String id) {
    this.id = id;
    return this;
  }

  public long version() {
    return version;
  }

  public EStrIdBean setVersion(long version) {
    this.version = version;
    return this;
  }

  public String name() {
    return name;
  }

  public EStrIdBean setName(String name) {
    this.name = name;
    return this;
  }
}
