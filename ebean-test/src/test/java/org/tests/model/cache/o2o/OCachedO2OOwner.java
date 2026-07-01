package org.tests.model.cache.o2o;

import jakarta.persistence.*;

/**
 * Owning side of a OneToOne targeting a cached entity (OCachedO2ODetail).
 * When this bean is inserted, updated, or deleted the target's bean cache must be evicted.
 */
@Entity
@Table(name = "o_cached_o2o_owner")
public class OCachedO2OOwner {

  @Id
  Long id;

  String name;

  @OneToOne
  OCachedO2ODetail detail;

  public OCachedO2OOwner(String name, OCachedO2ODetail detail) {
    this.name = name;
    this.detail = detail;
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

  public OCachedO2ODetail getDetail() {
    return detail;
  }

  public void setDetail(OCachedO2ODetail detail) {
    this.detail = detail;
  }
}
