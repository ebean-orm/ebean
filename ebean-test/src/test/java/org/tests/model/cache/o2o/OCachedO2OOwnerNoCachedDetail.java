package org.tests.model.cache.o2o;

import jakarta.persistence.*;

/**
 * Owning side of a OneToOne whose target ({@link OCachedO2ODetailNoCached}) has no {@code @Cache}.
 * Used to verify that when the target is not cached, no eviction (and no error) occurs.
 */
@Entity
@Table(name = "o_cached_o2o_owner_nc")
public class OCachedO2OOwnerNoCachedDetail {

  @Id
  Long id;

  String name;

  @OneToOne
  OCachedO2ODetailNoCached detail;

  public OCachedO2OOwnerNoCachedDetail(String name, OCachedO2ODetailNoCached detail) {
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

  public OCachedO2ODetailNoCached getDetail() {
    return detail;
  }

  public void setDetail(OCachedO2ODetailNoCached detail) {
    this.detail = detail;
  }
}
