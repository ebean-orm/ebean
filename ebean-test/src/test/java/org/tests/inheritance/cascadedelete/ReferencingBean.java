package org.tests.inheritance.cascadedelete;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class ReferencingBean {

  @Id
  @GeneratedValue
  public UUID id;

  @OneToMany(cascade = ALL)
  private List<RootBean> rootBeans;

  public ReferencingBean(List<RootBean> rootBeans) {
    this.rootBeans = rootBeans;
  }

  public List<RootBean> getRootBeans() {
    return rootBeans;
  }

  public void setRootBeans(List<RootBean> rootBeans) {
    this.rootBeans = rootBeans;
  }
}
