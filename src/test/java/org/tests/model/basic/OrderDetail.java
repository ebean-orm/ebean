package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.DocEmbedded;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Order Detail entity bean.
 */
@Cache
@Entity
@Table(name = "o_order_detail")
public class OrderDetail implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  Integer id;

  @ManyToOne(optional = false)
  Order order;

  Integer orderQty;

  Integer shipQty;

  Double unitPrice;

  @ManyToOne
  @DocEmbedded(doc = "id,name,sku")
  Product product;

  Timestamp cretime;

  @Version
  Timestamp updtime;

  public OrderDetail() {
  }

  public OrderDetail(Product product, Integer orderQty, Double unitPrice) {
    this.product = product;
    this.orderQty = orderQty;
    this.unitPrice = unitPrice;
  }

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
   * Return order qty.
   */
  public Integer getOrderQty() {
    return orderQty;
  }

  /**
   * Set order qty.
   */
  public void setOrderQty(Integer orderQty) {
    this.orderQty = orderQty;
  }

  /**
   * Return ship qty.
   */
  public Integer getShipQty() {
    return shipQty;
  }

  /**
   * Set ship qty.
   */
  public void setShipQty(Integer shipQty) {
    this.shipQty = shipQty;
  }

  public Double getUnitPrice() {
    return unitPrice;
  }

  public void setUnitPrice(Double unitPrice) {
    this.unitPrice = unitPrice;
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

  /**
   * Return order.
   */
  public Order getOrder() {
    return order;
  }

  /**
   * Set order.
   */
  public void setOrder(Order order) {
    this.order = order;
  }

  /**
   * Return product.
   */
  public Product getProduct() {
    return product;
  }

  /**
   * Set product.
   */
  public void setProduct(Product product) {
    this.product = product;
  }

}
