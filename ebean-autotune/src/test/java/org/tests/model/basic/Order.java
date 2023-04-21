package org.tests.model.basic;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "o_order")
public class Order extends BaseModel {

  public enum Status {
    NEW,
    APPROVED,
    SHIPPED,
    COMPLETE
  }


  @Enumerated(EnumType.STRING)
  Status status = Status.NEW;

  LocalDate orderDate;

  @ManyToOne(cascade = CascadeType.PERSIST)
  final Customer customer;

  public Order(Customer customer) {
    this.customer = customer;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  public Customer getCustomer() {
    return customer;
  }

}
