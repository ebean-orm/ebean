package org.example.domain;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity bean.
 */
@Entity
@Table(name = "o_order")
public class Order extends BaseModel {

  public static final Finder<Long,Order> find = new Finder<>(Order.class);

  public enum Status {
    NEW, APPROVED, SHIPPED, COMPLETE
  }

  Status status;

  Date orderDate;

  Date shipDate;

  @NotNull
  @ManyToOne
  Customer customer;

  @ManyToOne
  Address shippingAddress;
  
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
  @OrderBy("id asc")
  List<OrderDetail> details;

  public String toString() {
    return id + " status:" + status + " customer:" + customer;
  }
  
  /**
   * Return order date.
   */
  public Date getOrderDate() {
    return orderDate;
  }

  /**
   * Set order date.
   */
  public void setOrderDate(Date orderDate) {
    this.orderDate = orderDate;
  }

  /**
   * Return ship date.
   */
  public Date getShipDate() {
    return shipDate;
  }

  /**
   * Set ship date.
   */
  public void setShipDate(Date shipDate) {
    this.shipDate = shipDate;
  }

  /**
   * Return status.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Set status.
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Return customer.
   */
  public Customer getCustomer() {
    return customer;
  }

  /**
   * Set customer.
   */
  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  /**
   * Set the customer with their current shipping address.
   */
  public void setCustomerWithShipping(Customer customer) {
    this.customer = customer;
    this.shippingAddress = customer.getShippingAddress();
  }

  /**
   * Return details.
   */
  public List<OrderDetail> getDetails() {
    return details;
  }

  /**
   * Set details.
   */
  public void setDetails(List<OrderDetail> details) {
    this.details = details;
  }

  public void addDetail(OrderDetail detail) {

    if (details == null) {
      details = new ArrayList<>();
    }
    details.add(detail);
  }

}
