package org.tests.model.basic;

import io.ebean.annotation.DbEnumValue;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@NamedQuery(name = "name", query = "select(name) order by name")
@NamedQuery(name = "withStatus", query = "select(name,status) order by name")
@Entity
@Table(name = "o_customer")
public class Customer extends BasicDomain {
  public enum Status {
    NEW("N"),
    ACTIVE("A"),
    INACTIVE("I");

    String dbValue;
    Status(String dbValue) {
      this.dbValue = dbValue;
    }
    @DbEnumValue
    public String getValue() {
      return dbValue;
    }
  }

  Status status;
  String name;
  String smallnote;

  @ManyToOne(cascade = CascadeType.ALL)
  Address billingAddress;
  @ManyToOne(cascade = CascadeType.ALL)
  Address shippingAddress;

  @OneToMany(mappedBy = "customer")
  List<Order> orders;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
  List<Contact> contacts;

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSmallnote() {
    return smallnote;
  }

  public void setSmallnote(String smallnote) {
    this.smallnote = smallnote;
  }

  public Address getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(Address billingAddress) {
    this.billingAddress = billingAddress;
  }

  public Address getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(Address shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public List<Order> getOrders() {
    return orders;
  }

  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  public List<Contact> getContacts() {
    return contacts;
  }

  public void setContacts(List<Contact> contacts) {
    this.contacts = contacts;
  }
}
