package org.tests.model.onetoone;

import io.ebean.annotation.Identity;
import io.ebean.annotation.IdentityGenerated;

import javax.persistence.*;

@Identity(generated = IdentityGenerated.BY_DEFAULT)
@Entity
public class OtoBChild {

  @Id
  @Column(name = "master_id")
  Long id;

  String child;

  @OneToOne
  @PrimaryKeyJoinColumn
  OtoBMaster master;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getChild() {
    return child;
  }

  public void setChild(String child) {
    this.child = child;
  }

  public OtoBMaster getMaster() {
    return master;
  }

  public void setMaster(OtoBMaster master) {
    this.master = master;
  }

}
