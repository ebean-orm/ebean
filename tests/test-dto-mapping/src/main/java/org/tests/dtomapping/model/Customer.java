package org.tests.dtomapping.model;

import io.ebean.Model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Customer extends Model {

  @Id
  private Long id;

  private String name;

  @ManyToOne
  private Address billingAddress;

  @OneToMany(mappedBy = "customer")
  private List<Contact> contacts = new ArrayList<>();

  public Customer(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Address getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(Address billingAddress) {
    this.billingAddress = billingAddress;
  }

  public List<Contact> getContacts() {
    return contacts;
  }
}
