package org.tests.model.basic;

import io.ebean.annotation.Cache;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Cached bean for testing caching implementation.
 */
@Cache
@Entity
@Table(name = "o_cached_bean")
public class OCachedBean {

  @Id
  Long id;

  String name;

  @ManyToMany(cascade = CascadeType.ALL)
  List<Country> countries = new ArrayList<>();

  @OneToMany(mappedBy = "cachedBean", cascade = CascadeType.ALL)
  List<OCachedBeanChild> children = new ArrayList<>();

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

  public List<Country> getCountries() {
    return countries;
  }

  public void setCountries(List<Country> countries) {
    this.countries = countries;
  }

  public List<OCachedBeanChild> getChildren() {
    return children;
  }

  public void setChildren(List<OCachedBeanChild> children) {
    this.children = children;
  }
}
