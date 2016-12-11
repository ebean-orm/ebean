package org.tests.model.view;

import io.ebean.annotation.Cache;
import io.ebean.annotation.View;
import org.tests.model.basic.Order;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Cache(enableQueryCache = true)
@Entity
@View(name = "order_agg_vw", dependentTables = {"o_order", "o_order_detail"})
public class EOrderAgg {

  @Id
  @Column(name = "order_id")
  Long id;

  @OneToOne
  @JoinColumn(name = "order_id")
  Order order;

  Double orderTotal;

  Double shipTotal;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Double getOrderTotal() {
    return orderTotal;
  }

  public void setOrderTotal(Double orderTotal) {
    this.orderTotal = orderTotal;
  }

  public Double getShipTotal() {
    return shipTotal;
  }

  public void setShipTotal(Double shipTotal) {
    this.shipTotal = shipTotal;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }
}
