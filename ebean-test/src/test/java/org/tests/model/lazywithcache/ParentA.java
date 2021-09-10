package org.tests.model.lazywithcache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Parent class with ChildWithCache.
 *
 */
@Entity
public class ParentA {
  
  @Id
  Long id;
  
  @ManyToOne(optional = true)
  ChildWithCache child;
  
  String name;
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public ChildWithCache getChild() {
    return child;
  }

  public void setChild(ChildWithCache child) {
    this.child = child;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
