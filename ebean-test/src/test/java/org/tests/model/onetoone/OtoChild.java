package org.tests.model.onetoone;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class OtoChild {

  @Id
  Integer id;

  String name;

  @OneToOne
  OtoMaster master;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OtoMaster getMaster() {
    return master;
  }

  public void setMaster(OtoMaster master) {
    this.master = master;
  }

}
