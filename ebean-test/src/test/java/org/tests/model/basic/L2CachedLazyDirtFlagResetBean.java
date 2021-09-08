package org.tests.model.basic;

import io.ebean.annotation.Cache;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cached bean for testing caching implementation.
 */
@Cache
@Entity
@Table(name = "l2_cldf_reset_bean")
public class L2CachedLazyDirtFlagResetBean {

  public Long getId() {
    return id;
  }

  @Id
  Long id;

  String name;

  public List<L2CachedLazyDirtFlagResetBeanChild> getChildren() {
    return children;
  }

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
  List<L2CachedLazyDirtFlagResetBeanChild> children;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Long> someRichObjectMethod() {
    List<Long> result = new ArrayList<Long>();
    if(this.children != null) {
      for (L2CachedLazyDirtFlagResetBeanChild relation : this.children) {
        result.add(relation.getId());
      }
    }
    return result;
  }


}
