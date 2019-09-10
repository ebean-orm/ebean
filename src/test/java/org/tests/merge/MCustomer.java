package org.tests.merge;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class MCustomer extends MBase {

  private String name;

  private String notes;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "customer")
  private List<MContact> contacts;

  @ManyToOne(cascade = CascadeType.ALL)
  private MAddress shippingAddress;

  @ManyToOne(cascade = CascadeType.ALL)
  private MAddress billingAddress;

  public MCustomer(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public List<MContact> getContacts() {
    return contacts;
  }

  public void setContacts(List<MContact> contacts) {
    this.contacts = contacts;
  }

  public MAddress getShippingAddress() {
    return shippingAddress;
  }

  public void setShippingAddress(MAddress shippingAddress) {
    this.shippingAddress = shippingAddress;
  }

  public MAddress getBillingAddress() {
    return billingAddress;
  }

  public void setBillingAddress(MAddress billingAddress) {
    this.billingAddress = billingAddress;
  }
}
