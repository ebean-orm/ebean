package org.tests.cascade;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class OmOrderedDetail {

  @Id
  Long id;

  String name;

  @ManyToOne
  OmOrderedMaster master;

  @Version
  Long version;

  public OmOrderedDetail(String name) {
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

  public OmOrderedMaster getMaster() {
    return master;
  }

  public void setMaster(OmOrderedMaster master) {
    this.master = master;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
