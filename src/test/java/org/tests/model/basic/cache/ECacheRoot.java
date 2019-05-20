package org.tests.model.basic.cache;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

import io.ebean.annotation.Cache;

@Entity
@Cache(enableQueryCache = true, enableBeanCache = false)
public class ECacheRoot {

  @Id
  @GeneratedValue
  protected UUID id;

  @Size(max = 100)
  private String name;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
