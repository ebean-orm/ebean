package com.avaje.tests.model.basic;

import com.avaje.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * An example of an Aggregate object.
 * <p>
 * Note the &#064;Sql indicates to Ebean that this bean is not based on a table but
 * instead uses RawSql.
 * </p>
 */
@Entity
@Sql
public class OrderAggregate {

  @OneToOne
  Order order;

  Double totalAmount;

  Double totalItems;

  public String toString() {
    return order.getId() + " totalAmount:" + totalAmount + " totalItems:" + totalItems;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Double totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Double getTotalItems() {
    return totalItems;
  }

  public void setTotalItems(Double totalItems) {
    this.totalItems = totalItems;
  }
}
