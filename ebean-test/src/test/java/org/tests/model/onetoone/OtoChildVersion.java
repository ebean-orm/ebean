package org.tests.model.onetoone;

import io.ebean.annotation.Where;

import javax.persistence.*;
import java.util.List;

@Entity
public class OtoChildVersion {

  @Id
  Integer id;

  String name;

  @OneToOne
  OtoMasterVersion master;

  @Version
  int version;

  @OneToMany(cascade = CascadeType.ALL)
  @JoinColumn(name = "ref_id")
  @Where(clause = "${mta}.type=1")
  List<OtoNotification> notifications;

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

  public OtoMasterVersion getMaster() {
    return master;
  }

  public void setMaster(OtoMasterVersion master) {
    this.master = master;
  }

  public int getVersion() {
    return version;
  }

  public void setVersion(int version) {
    this.version = version;
  }

}
