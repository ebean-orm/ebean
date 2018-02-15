package org.tests.model.composite;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;


/**
 * @author rnentjes
 */
@Entity
@Table(name = "r_orders")
public class ROrder {

  @EmbeddedId
  protected ROrderPK orderPK;

  @ManyToOne(cascade = CascadeType.PERSIST)
  @JoinColumns({
    @JoinColumn(name = "company", referencedColumnName = "company", insertable = false, updatable = false),
    @JoinColumn(name = "customerName", referencedColumnName = "name", insertable = false, updatable = false)
  })
  private RCustomer customer;

  @Column(name = "customerName")
  @Size(max=127)
  private String customerName;

  private String item;

  public ROrder() {
    this(new ROrderPK());
  }

  public ROrder(ROrderPK orderPK) {
    this(orderPK, null);
  }

  public ROrder(ROrderPK orderPK, String item) {
    this.orderPK = orderPK;
    this.item = item;
  }

  public ROrder(ROrderPK orderPK, String item, RCustomer customer) {
    this.orderPK = orderPK;
    this.item = item;
    setCustomer(customer);
  }

  public ROrderPK getOrderPK() {
    return orderPK;
  }

  public void setOrderPK(ROrderPK orderPK) {
    this.orderPK = orderPK;
  }

  public String getItem() {
    return item;
  }

  public void setItem(String item) {
    this.item = item;
  }

  public RCustomer getCustomer() {
    return customer;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomer(RCustomer customer) {
    this.customer = customer;

//        if (customer == null) {
//            customerName = null;
//        } else {
//            customerName = customer.getKey().getName();
//        }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ROrder other = (ROrder) obj;
    if (this.orderPK != other.orderPK && (this.orderPK == null || !this.orderPK.equals(other.orderPK))) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 29 * hash + (this.orderPK != null ? this.orderPK.hashCode() : 0);
    return hash;
  }

}
