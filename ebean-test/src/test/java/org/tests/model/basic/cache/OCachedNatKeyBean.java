package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Cached bean for testing caching implementation.
 */
@Cache(naturalKey = {"store","sku"})
@Entity
@Table(name = "o_cached_natkey")
public class OCachedNatKeyBean {

  @Id
  Long id;

  String store;

  String sku;

  String description;

  OCachedNatKeyBean(String store, String sku) {
    this.store = store;
    this.sku = sku;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getStore() {
    return store;
  }

  public void setStore(String store) {
    this.store = store;
  }

  public String getSku() {
    return sku;
  }

  public void setSku(String sku) {
    this.sku = sku;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
