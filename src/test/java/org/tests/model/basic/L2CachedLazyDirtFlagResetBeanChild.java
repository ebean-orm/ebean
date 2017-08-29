package org.tests.model.basic;

import javax.persistence.*;

@Entity
@Table(name = "l2_cldf_reset_bean_child")
public class L2CachedLazyDirtFlagResetBeanChild {

  @Id
  Long id;

  @ManyToOne
  @Column(nullable = false)
  L2CachedLazyDirtFlagResetBean parent;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
