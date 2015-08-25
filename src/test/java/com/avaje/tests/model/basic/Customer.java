package com.avaje.tests.model.basic;

import com.avaje.ebean.annotation.ChangeLog;
import com.avaje.ebean.annotation.ChangeLogInsertMode;
import com.avaje.ebean.annotation.EnumValue;
import com.avaje.ebean.annotation.JsonIgnore;
import com.avaje.ebean.annotation.Where;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Customer entity bean.
 */
@ChangeLog(inserts = ChangeLogInsertMode.EXCLUDE, updatesThatInclude = {"name","status"})
@Entity
@Table(name = "o_customer")
public class Customer extends BasicDomain {

  private static final long serialVersionUID = 1L;

  /**
   * EnumValue is an Ebean specific mapping for enums.
   */
  public enum Status {
    @EnumValue("N")
    NEW,

    @EnumValue("A")
    ACTIVE,

    @EnumValue("I")
    INACTIVE
  }

  @Transient
  Boolean selected;


  @JsonIgnore
  //@Expose(deserialize = false, serialize = false)
  @Transient
  ReentrantLock lock = new ReentrantLock();

  Status status;

  @NotNull
  @Size(max = 40)
  String name;

  @Size(max = 100)
  String smallnote;

  @NotNull(groups = { ValidationGroupSomething.class })
  Date anniversary;

  @ManyToOne(cascade = CascadeType.ALL)
  Address billingAddress;

  @ManyToOne(cascade = CascadeType.ALL)
  Address shippingAddress;

  @OneToMany(mappedBy = "customer")
  @Where(clause = "${ta}.order_date is not null")
  List<Order> orders;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
  List<Contact> contacts;

  public String toString() {
    return id + " " + status + " " + name;
  }

  /**
   * Return name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Return billing address.
   */
  public Address getBillingAddress() {
    return billingAddress;
  }

  /**
   * Set billing address.
   */
  public void setBillingAddress(Address billingAddress) {
    this.billingAddress = billingAddress;
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
   * Return shipping address.
   */
  public Address getShippingAddress() {
    return shippingAddress;
  }

  /**
   * Set shipping address.
   */
  public void setShippingAddress(Address shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  /**
   * Return orders.
   */
  public List<Order> getOrders() {
    return orders;
  }

  /**
   * Set orders.
   */
  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  public Boolean getSelected() {
    return selected;
  }

  public void setSelected(Boolean selected) {
    this.selected = selected;
  }

  public ReentrantLock getLock() {
    return lock;
  }

  public String getSmallnote() {
    return smallnote;
  }

  public void setSmallnote(String smallnote) {
    this.smallnote = smallnote;
  }

  public Date getAnniversary() {
    return anniversary;
  }

  public void setAnniversary(Date anniversary) {
    this.anniversary = anniversary;
  }

  public List<Contact> getContacts() {
    return contacts;
  }

  public void setContacts(List<Contact> contacts) {
    this.contacts = contacts;
  }

  public void addContact(Contact contact) {
    if (contacts == null) {
      contacts = new ArrayList<Contact>();
    }
    contacts.add(contact);
  }
}
