package org.tests.model.cache.o2o;

import jakarta.persistence.*;

/**
 * Detail entity WITHOUT {@code @Cache} — the owning {@link OCachedO2OOwnerNoCachedDetail}
 * must not trigger any bean cache eviction because there is no bean cache to evict.
 */
@Entity
@Table(name = "o_cached_o2o_detail_nc")
public class OCachedO2ODetailNoCached {

  @Id
  Long id;

  String name;

  @OneToOne(mappedBy = "detail")
  OCachedO2OOwnerNoCachedDetail owner;

  public OCachedO2ODetailNoCached(String name) {
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

  public OCachedO2OOwnerNoCachedDetail getOwner() {
    return owner;
  }

  public void setOwner(OCachedO2OOwnerNoCachedDetail owner) {
    this.owner = owner;
  }
}
