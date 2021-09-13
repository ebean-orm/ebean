package org.tests.model.version;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class VersionToy {

  @Id
  Integer id;

  String name;

  @Version
  Integer version;

  @ManyToOne
  VersionChild child;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(final Integer version) {
    this.version = version;
  }

  public VersionChild getChild() {
    return child;
  }

  public void setChild(final VersionChild child) {
    this.child = child;
  }
}
