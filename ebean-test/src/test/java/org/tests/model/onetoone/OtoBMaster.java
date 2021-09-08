package org.tests.model.onetoone;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OtoBMaster {

  @Id
  Long id;

  String name;

  @OneToOne(cascade = CascadeType.ALL, mappedBy = "master", fetch = FetchType.LAZY)
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
