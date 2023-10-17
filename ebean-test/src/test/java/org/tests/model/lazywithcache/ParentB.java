package org.tests.model.lazywithcache;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Parent class with ChildWithCache.
 *
 */
@Entity
public class ParentB {
  
  @Id
  Long id;
  
  @ManyToOne(optional = true)
  ChildWithCache child;
    
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
  
}
