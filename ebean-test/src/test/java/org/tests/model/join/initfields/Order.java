package org.tests.model.join.initfields;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "join_initfields_order")
public class Order {
  @Id
  int id;

  @OneToMany
  public List<OrderItem> orderItems = new ArrayList<>();

  @OneToMany
  public List<OrderDetail> orderDetails = new ArrayList<>();

  @OneToMany
  public List<OrderInvoice> orderInvoices = List.of(); // Change this to new ArrayList<>() to make the test pass.
}
