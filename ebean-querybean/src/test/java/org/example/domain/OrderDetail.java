package org.example.domain;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Order Detail entity bean.
 */
@Entity
@Table(name = "o_order_detail")
public class OrderDetail extends BaseModel {

  @ManyToOne
  Order order;

  Integer orderQty;

  Integer shipQty;

  Double unitPrice;

  @ManyToOne
  Product product;

  public OrderDetail() {
  }

  public OrderDetail(Product product, Integer orderQty, Double unitPrice) {
    this.product = product;
    this.orderQty = orderQty;
    this.unitPrice = unitPrice;
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
