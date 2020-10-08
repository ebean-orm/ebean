package org.example.domain;

import io.ebean.annotation.DbArray;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Contact entity bean.
 */
@Entity
@Table(name="be_contact")
public class Contact extends BaseModel {

  @DbArray
  List<String> phoneNumbers = new ArrayList();

  @Column(length=50)
  String firstName;
  
  @Column(length=50)
  String lastName;
  
  @Column(length=200)
  String email;

  @Column(length=20)
  String phone;
  
  @ManyToOne(optional=false)
  Customer customer;

  @OneToMany(mappedBy = "contact")
  List<ContactNote> notes;

  /**
   * Default constructor.
   */
  public Contact() {
  }
  
  /**
   * Construct with a firstName and lastName.
   */
  public Contact(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
  
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public List<ContactNote> getNotes() {
    return notes;
  }

  public void setNotes(List<ContactNote> notes) {
    this.notes = notes;
  }

  public List<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }
}
