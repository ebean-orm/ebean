package org.tests.model.onetoone;

import io.ebean.Finder;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class OtoSdMaster {

  public static Finder<Long, OtoSdMaster> find = new Finder<>(OtoSdMaster.class);

  @Id
  long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "master")//, fetch = FetchType.LAZY)
  OtoSdChild child;

  @Version
  long version;

  public OtoSdMaster(String name) {
    this.name = name;
  }

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

  public OtoSdChild getChild() {
    return child;
  }

  public void setChild(OtoSdChild child) {
    this.child = child;
  }

}
