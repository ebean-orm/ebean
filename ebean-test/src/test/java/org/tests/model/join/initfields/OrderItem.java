package org.tests.model.join.initfields;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="join_initfields_order_item")
public class OrderItem {
  @Id
  int id;

  @ManyToOne
  Order order;

  public OrderItem(Order order) {
    this.order = order;
  }
}
