package org.tests.expression.bitwise;

import io.ebean.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class BwBean extends Model {

  @Id
  long id;

  String name;

  long flags;

  @Version
  long version;

  public BwBean(String name) {
    this.name = name;
  }

  public BwBean(String name, long flags) {
    this.name = name;
    this.flags = flags;
  }

  @Override
  public String toString() {
    return "nm:" + name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getFlags() {
    return flags;
  }

  public void setFlags(long flags) {
    this.flags = flags;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
