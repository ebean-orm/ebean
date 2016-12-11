package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheQueryTuning;
import io.ebean.annotation.CreatedTimestamp;
import io.ebean.annotation.DocStore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Product entity bean.
 */
@DocStore
@Cache
@CacheQueryTuning(maxSecsToLive = 15)
@Entity
@Table(name = "o_product")
public class Product implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  Integer id;

  @Size(max = 20)
  String sku;

  String name;

  @CreatedTimestamp
  Timestamp cretime;

  @Version
  Timestamp updtime;

  /**
   * Return id.
   */
  public Integer getId() {
    return id;
  }

  /**
   * Set id.
   */
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   * Return sku.
   */
  public String getSku() {
    return sku;
  }

  /**
   * Set sku.
   */
  public void setSku(String sku) {
    this.sku = sku;
  }

  /**
   * Return name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Return cretime.
   */
  public Timestamp getCretime() {
    return cretime;
  }

  /**
   * Set cretime.
   */
  public void setCretime(Timestamp cretime) {
    this.cretime = cretime;
  }

  /**
   * Return updtime.
   */
  public Timestamp getUpdtime() {
    return updtime;
  }

  /**
   * Set updtime.
   */
  public void setUpdtime(Timestamp updtime) {
    this.updtime = updtime;
  }

}
