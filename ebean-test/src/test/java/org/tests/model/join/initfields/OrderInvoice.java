package org.tests.model.join.initfields;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="join_initfields_order_invoice")
public class OrderInvoice {
  @Id
  int id;

  @ManyToOne
  public Order order;
}
