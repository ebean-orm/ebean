package org.tests.model.lazywithcache;

import io.ebean.annotation.Cache;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;

/**
 * Class with @Cache and lazy load property.
 *
 * @author Noemi Szemenyei, FOCONIS AG
 *
 */
@Entity
@Cache(enableQueryCache = true)
public class ChildWithCache {

  @Id
  Long id;

  String name;

  @Basic(fetch = FetchType.LAZY)
  String address;

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

  public void setAddress(String address) {
    this.address = address;
  }

  public String getAddress() {
    return address;
  }

}
