package org.tests.model.cache;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.annotation.Cache;

@Entity
@Cache(enableQueryCache = true, enableBeanCache = true)
public class M2MCacheChild {

  @Id
  private Integer id;

  private String name;

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

}