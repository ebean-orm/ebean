package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Cached bean for compound natural key.
 */
@Cache(naturalKey = {"store","code","sku"})
@Entity
@Table(name = "o_cached_natkey3")
public class OCachedNatKeyBean3 {

  @Id
  Long id;

  String store;

  int code;

  String sku;

  String description;

  OCachedNatKeyBean3(String store, int code, String sku) {
    this.store = store;
    this.code = code;
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
