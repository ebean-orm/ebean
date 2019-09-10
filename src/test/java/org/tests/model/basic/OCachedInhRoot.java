package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;

import io.ebean.annotation.Cache;

/**
 * Cached entity for inheritance.
 *
 * @author Roland Praml, FOCONIS AG
 */
@Entity
@Table(name = "o_cached_inherit")
@Inheritance
@Cache
public abstract class OCachedInhRoot {

  @Id
  Long id;

  String name;

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



}
