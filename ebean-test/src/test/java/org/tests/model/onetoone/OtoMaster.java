package org.tests.model.onetoone;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class OtoMaster {

  @Id
  Long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "master")
  OtoChild child;

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

  public OtoChild getChild() {
    return child;
  }

  public void setChild(OtoChild child) {
    this.child = child;
  }

}
