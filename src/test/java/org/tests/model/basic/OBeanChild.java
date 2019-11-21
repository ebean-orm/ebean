package org.tests.model.basic;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Not cached bean (copy of {@link OCachedBeanChild}} without {@link Cache} annotation
 * for testing caching implementation for relations {@link Cache} and not {@link Cache}.
 */
@Entity
@Table(name = "o_bean_child")
public class OBeanChild {
  @Id
  Long id;

  @ManyToOne
  OCachedBean cachedBean;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OCachedBean getCachedBean() {
    return cachedBean;
  }

  public void setCachedBean(OCachedBean cachedBean) {
    this.cachedBean = cachedBean;
  }
}
