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

  /**
   * Computed/derived getter (no backing field) reading {@code billingAddress.line1} - deliberately
   * a different {@code Address} property to {@code city} (used narrowly elsewhere, see
   * {@code FetchCollisionDto}), to exercise the priority between a bare, full {@code requires()}
   * fetch of {@code billingAddress} and a sibling property's narrowed {@code fetch("billingAddress",
   * "city")}: {@code FetchGroup}'s builder replaces (not merges) same-path fetch calls, so if the
   * narrowed selection silently won, {@code line1} would never be loaded and calling this getter
   * outside a persistence context would throw {@code LazyInitialisationException} instead of
   * returning a value.
   */
  public String getBillingSummary() {
    return billingAddress == null ? null : billingAddress.getLine1() + ", " + billingAddress.getCity();
  }
}
