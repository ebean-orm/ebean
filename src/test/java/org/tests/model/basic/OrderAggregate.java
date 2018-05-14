package org.tests.model.basic;

import io.ebean.annotation.Sql;

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

  Double maxAmount;

  Double totalAmount;

  Long totalItems;

  @Override
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

  public Long getTotalItems() {
    return totalItems;
  }

  public void setTotalItems(Long totalItems) {
    this.totalItems = totalItems;
  }

  public Double getMaxAmount() {
    return maxAmount;
  }

  public void setMaxAmount(Double maxAmount) {
    this.maxAmount = maxAmount;
  }
}
