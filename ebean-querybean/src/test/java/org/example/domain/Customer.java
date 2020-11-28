package org.example.domain;

import io.ebean.annotation.Cache;
import io.ebean.annotation.EnumValue;
import io.ebean.types.Inet;
import org.example.domain.finder.CustomerFinder;
import org.example.domain.otherpackage.PhoneNumber;
import org.example.domain.otherpackage.ValidEmail;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Customer entity bean.
 */
@Cache
@Entity
@Table(name = "be_customer")
public class Customer extends BaseModel {

  /**
   * Convenience Finder for 'active record' style.
   */
  public static final CustomerFinder find = new CustomerFinder();

  public enum Status {
    @EnumValue("G")
    GOOD,

    @EnumValue("B")
    BAD,

    @EnumValue("M")
    MIDDLING
  }

  Status status;

  boolean inactive;

  PhoneNumber phoneNumber;

  ValidEmail email;

  @Column(length = 100)
  String name;

  Date registered;

  Inet currentInet;

  @Column(length = 1000)
  String comments;

  @ManyToOne(cascade = CascadeType.ALL)
  Address billingAddress;

  @ManyToOne(cascade = CascadeType.ALL)
  Address shippingAddress;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.PERSIST)
  List<Contact> contacts;

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public boolean isInactive() {
    return inactive;
  }

  public void setInactive(boolean inactive) {
    this.inactive = inactive;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getRegistered() {
    return registered;
  }

  public void setRegistered(Date registered) {
    this.registered = registered;
  }

  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
  }

  public Inet getCurrentInet() {
    return currentInet;
  }

  public void setCurrentInet(Inet currentInet) {
    this.currentInet = currentInet;
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

  public List<Contact> getContacts() {
    return contacts;
  }

  public void setContacts(List<Contact> contacts) {
    this.contacts = contacts;
  }

  public PhoneNumber getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(final PhoneNumber phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public ValidEmail getEmail() {
    return email;
  }

  public void setEmail(final ValidEmail email) {
    this.email = email;
  }

  /**
   * Helper method to add a contact to the customer.
   */
  public void addContact(Contact contact) {
    if (contacts == null) {
      contacts = new ArrayList<>();
    }
    // setting the customer is automatically done when Ebean does
    // a cascade save from customer to contacts.
    contact.setCustomer(this);
    contacts.add(contact);
  }

}
