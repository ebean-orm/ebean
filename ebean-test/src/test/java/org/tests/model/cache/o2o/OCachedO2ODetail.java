package org.tests.model.cache.o2o;

import io.ebean.annotation.Cache;

import jakarta.persistence.*;

/**
 * Cached entity that is the inverse (exported) side of a OneToOne.
 * Used to verify that when the owning side's FK changes, this entity's bean cache entry is evicted.
 */
@Cache
@Entity
@Table(name = "o_cached_o2o_detail")
public class OCachedO2ODetail {

  @Id
  Long id;

  String name;

  @OneToOne(mappedBy = "detail")
  OCachedO2OOwner owner;

  public OCachedO2ODetail(String name) {
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

  public OCachedO2OOwner getOwner() {
    return owner;
  }

  public void setOwner(OCachedO2OOwner owner) {
    this.owner = owner;
  }
}
