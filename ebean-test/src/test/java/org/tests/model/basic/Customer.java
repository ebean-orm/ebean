package org.tests.model.basic;

import io.ebean.annotation.*;
import org.tests.model.basic.finder.CustomerFinder;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Customer entity bean.
 */
@NamedQueries(
  value = {
    @NamedQuery(name = "name", query = "select(name) order by name"),
    @NamedQuery(name = "withStatus", query = "select(name,status) order by name")
  }
)
@NamedQuery(name = "withContacts", query = "fetch contacts (firstName, lastName) where id = :id")
@Cache(enableQueryCache = true)
@DocStore
@ChangeLog(inserts = ChangeLogInsertMode.EXCLUDE, updatesThatInclude = {"name", "status"})
@Entity
@Table(name = "o_customer")
@DbComment("Holds external customers")
public class Customer extends BasicDomain {

  private static final long serialVersionUID = 1L;

  public static final CustomerFinder find = new CustomerFinder();

  /**
   * EnumValue is an Ebean specific mapping for enums.
   */
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

  @Transient
  Boolean selected;


  @JsonIgnore
  //@Expose(deserialize = false, serialize = false)
  @Transient
  ReentrantLock lock = new ReentrantLock();

  @DbComment("status of the customer")
  Status status;

  @NotNull
  @Size(max = 40)
  String name;

  @DbComment("Short notes regarding the customer")
  @Size(max = 100)
  String smallnote;

  @DbComment("Join date of the customer")
  @NotNull(groups = {ValidationGroupSomething.class})
  Date anniversary;

  @DocEmbedded(doc = "*,country(*)")
  @ManyToOne(cascade = CascadeType.ALL)
  Address billingAddress;

  @DocEmbedded(doc = "*,country(*)")
  @ManyToOne(cascade = CascadeType.ALL)
  Address shippingAddress;

  @OneToMany(mappedBy = "customer")
  @Where(clause = "${ta}.order_date is not null")
  List<Order> orders;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
  List<Contact> contacts = new ArrayList<>();

  @Override
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
      contacts = new ArrayList<>();
    }
    contacts.add(contact);
  }
}
