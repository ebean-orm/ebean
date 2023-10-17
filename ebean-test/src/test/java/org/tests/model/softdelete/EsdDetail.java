package org.tests.model.softdelete;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class EsdDetail extends BaseSoftDelete {

  String name;

  @ManyToOne(optional = false)
  EsdMaster master;

  public EsdDetail(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public EsdMaster getMaster() {
    return master;
  }

  public void setMaster(EsdMaster master) {
    this.master = master;
  }
}
