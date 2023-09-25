package org.example.domain;

import io.ebean.annotation.DbArray;

import jakarta.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contact entity bean.
 */
@Entity
@Table(name = "be_contact")
public class Contact extends BaseModel {

  @DbArray
  List<@Size(max=20) String> phoneNumbers = new ArrayList<>();

  @ElementCollection
  @Size(max=10)
  List<@Size(max=20) String> paths;

  @Column(length = 50)
  String firstName;

  @Column(length = 50)
  String lastName;

  @Column(length = 200)
  String email;

  @Column(length = 20)
  String phone;

  ZonedDateTime zoneDateTime;

  @ManyToOne(optional = false)
  Customer customer;

  @OneToMany(mappedBy = "contact")
  List<@NotNull ContactNote> notes;

  @OneToMany(cascade = CascadeType.PERSIST)
  @MapKey(name = "key")
  Map<String, ContactOther> others;

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

  public ZonedDateTime getZoneDateTime() {
    return zoneDateTime;
  }

  public void setZoneDateTime(ZonedDateTime zoneDateTime) {
    this.zoneDateTime = zoneDateTime;
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
