package org.example.domain;

import io.ebean.annotation.Sql;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;

/**
 * An example of an Aggregate object populated via RawSql rather than a table.
 * <p>
 * Note the &#064;Sql indicates to Ebean that this bean is not based on a table but
 * instead uses RawSql. As it is a normal &#064;Entity a query bean (QOrderAggregate)
 * is generated for it, so RawSql (including RawSqlBuilder.withPlaceholders()) can be
 * used together with the generated, type-safe query bean API.
 * </p>
 */
@Entity
@Sql
public class OrderAggregate {

  @OneToOne
  Order order;

  Double totalAmount;

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

  @Override
  public String toString() {
    return order.getId() + " totalAmount:" + totalAmount;
  }
}
