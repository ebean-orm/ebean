package org.tests.model.basic;

import io.ebean.Model;
import io.ebean.annotation.WhenModified;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.sql.Timestamp;
import java.time.LocalDate;

@Entity
@Table(name = "orders")
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
  Customer customer;

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

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }
}
