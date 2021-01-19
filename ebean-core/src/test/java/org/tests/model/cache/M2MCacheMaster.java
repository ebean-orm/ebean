package org.tests.model.cache;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import io.ebean.annotation.Cache;

@Entity
@Cache(enableQueryCache = true, enableBeanCache = true)
public class M2MCacheMaster {

  @Id
  private Integer id;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "m2mcache_set1")
  private Set<M2MCacheChild> set1 = new LinkedHashSet<>();

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "m2mcache_set2")
  private Set<M2MCacheChild> set2 = new LinkedHashSet<>();

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Set<M2MCacheChild> getSet1() {
    return set1;
  }

  public void setSet1(Set<M2MCacheChild> set1) {
    this.set1 = set1;
  }

  public Set<M2MCacheChild> getSet2() {
    return set2;
  }

  public void setSet2(Set<M2MCacheChild> set2) {
    this.set2 = set2;
  }

}
