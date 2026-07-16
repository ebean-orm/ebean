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

  /**
   * Computed/derived getter (no backing field) - not a real Ebean property. Exercises
   * {@code @DtoPath#requires()}: a {@code @DtoPath} traversing this segment must explicitly name
   * {@code "contacts"} as a required fetch, since this method's own data dependency (the
   * {@code contacts} collection) can't be inferred from the path string alone.
   */
  public Contact getPrimaryContact() {
    return contacts.isEmpty() ? null : contacts.get(0);
  }

  /**
   * Computed/derived getter (no backing field) deriving purely from {@code id} - which is always
   * fetched as a matter of course, so unlike {@link #getPrimaryContact()} this one genuinely needs
   * nothing extra fetched. Exercises {@code @DtoPath(requires = {})} (explicit empty array,
   * confirming "nothing extra needed") as distinct from omitting {@code requires} entirely (a
   * compile error).
   */
  public String getIdBadge() {
    return "CUST-" + id;
  }
}
