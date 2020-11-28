package org.tests.model.basic;

import io.ebean.annotation.Cache;
import io.ebean.annotation.ChangeLog;
import io.ebean.annotation.DocEmbedded;
import io.ebean.annotation.DocStore;
import io.ebean.annotation.Formula;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.Where;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity bean.
 */
@Cache
@DocStore
@ChangeLog
@Entity
@Table(name = "o_order")
public class Order implements Serializable {

  private static final long serialVersionUID = 1L;

  public enum Status {
    NEW,
    APPROVED,
    SHIPPED,
    COMPLETE
  }

  @Id
  Integer id;

  /**
   * Derived total amount from the order details. Needs to be explicitly included in query as Transient.
   * Removing the Transient would mean by default it would be included in a order query.
   * <p>
   * NOTE: The join clause for totalAmount and totalItems is the same. If your query includes both
   * totalAmount and totalItems only the one join is added to the query.
   * </p>
   */
  @Transient
  @Formula(
    select = "z_b${ta}.total_amount",
    join = "join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_b${ta} on z_b${ta}.order_id = ${ta}.id")
  Double totalAmount;

  /**
   * Derived total item count from the order details. Needs to be explicitly included in query as Transient.
   */
  @Transient
  @Formula(
    select = "z_b${ta}.total_items",
    join = "join (select order_id, count(*) as total_items, sum(order_qty*unit_price) as total_amount from o_order_detail group by order_id) z_b${ta} on z_b${ta}.order_id = ${ta}.id")
  Integer totalItems;

  @Enumerated(value = EnumType.ORDINAL)
  Status status = Status.NEW;

  Date orderDate = new Date(System.currentTimeMillis());

  Date shipDate;

  @NotNull
  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumn(name = "kcustomer_id")
  @DocEmbedded(doc = "id,name")
  Customer customer;

  @Column(name = "name", table = "o_customer")
  String customerName;

  @WhenCreated
  Timestamp cretime;

  @Version
  Timestamp updtime;

  @Where(clause = "${ta}.id > 0")
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
  @OrderBy("id asc, orderQty asc, cretime desc")
  @DocEmbedded
  List<OrderDetail> details;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
  List<OrderShipment> shipments;

  @Override
  public String toString() {
    return id + " totalAmount:" + totalAmount + " totalItems:" + totalItems;
  }

  /**
   * Return id.
   */
  public Integer getId() {
    return id;
  }

  /**
   * Set id.
   */
  public void setId(Integer id) {
    this.id = id;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public Double getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(Double totalAmount) {
    this.totalAmount = totalAmount;
  }

  public Integer getTotalItems() {
    return totalItems;
  }

  public void setTotalItems(Integer totalItems) {
    this.totalItems = totalItems;
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
   * Return cretime.
   */
  public Timestamp getCretime() {
    return cretime;
  }

  /**
   * Set cretime.
   */
  public void setCretime(Timestamp cretime) {
    this.cretime = cretime;
  }

  /**
   * Return updtime.
   */
  public Timestamp getUpdtime() {
    return updtime;
  }

  /**
   * Set updtime.
   */
  public void setUpdtime(Timestamp updtime) {
    this.updtime = updtime;
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

  public List<OrderShipment> getShipments() {
    return shipments;
  }

  public void setShipments(List<OrderShipment> shipments) {
    this.shipments = shipments;
  }

  public void addShipment(OrderShipment shipment) {

    if (shipments == null) {
      shipments = new ArrayList<>();
    }
    shipments.add(shipment);
  }
}
