package org.tests.inheritance.cache;

import io.ebean.Model;
import io.ebean.annotation.Cache;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@Cache(enableQueryCache=true)
@MappedSuperclass
public abstract class CIBaseModel extends Model {

  @Id
  protected long id;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }
}
