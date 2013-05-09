package com.avaje.tests.model.basic;

import com.avaje.ebean.annotation.CacheStrategy;
import com.avaje.ebean.annotation.CacheTuning;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Cached bean for testing caching implementation.
 */
@CacheStrategy
@Entity
@Table(name = "o_cached_bean")
public class CachedBean {

  @Id
  Long id;

  @ManyToMany
  List<Country> countries = new ArrayList<Country>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public List<Country> getCountries() {
    return countries;
  }

  public void setCountries(List<Country> countries) {
    this.countries = countries;
  }
}
