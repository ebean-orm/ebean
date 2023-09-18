package org.tests.model.onetoone;

import jakarta.persistence.*;

@Entity
public class OtoBMaster {

  @Id
  Long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "master", fetch = FetchType.LAZY, optional = false)
  OtoBChild child;

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

  public OtoBChild getChild() {
    return child;
  }

  public void setChild(OtoBChild child) {
    this.child = child;
  }

}
