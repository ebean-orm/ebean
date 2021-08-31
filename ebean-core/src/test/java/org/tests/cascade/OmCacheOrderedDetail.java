package org.tests.cascade;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
@Cache
public class OmCacheOrderedDetail {

  @Id
  Long id;

  String name;

  @ManyToOne
  OmCacheOrderedMaster master;

  @Version
  Long version;

  public OmCacheOrderedDetail(String name) {
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

  public OmCacheOrderedMaster getMaster() {
    return master;
  }

  public void setMaster(OmCacheOrderedMaster master) {
    this.master = master;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
