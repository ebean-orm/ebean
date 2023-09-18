package org.tests.model.basic;

import io.ebean.annotation.Index;
import io.ebean.annotation.*;

import jakarta.persistence.*;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;

@DocStore
@Index(columnNames = {"last_name", "first_name"})
@ChangeLog
@Entity
@Cache(naturalKey = "email", enableQueryCache = true)
public class Contact {

  @Id @GeneratedValue
  int id;

  @Size(max=127)
  String firstName;

  @Size(max=127)
  String lastName;

  String phone;
  String mobile;
  String email;

  boolean isMember;

  @DocEmbedded(doc = "id,name")
  @ManyToOne(optional = false)
  Customer customer;

  @ManyToOne(optional = true)
  ContactGroup group;

  @OneToMany(cascade = CascadeType.ALL)
  List<ContactNote> notes;

  @WhenCreated
  Timestamp cretime;

  @Version
  Timestamp updtime;


  public Contact(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public Contact() {

  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public Timestamp getUpdtime() {
    return updtime;
  }

  public void setUpdtime(Timestamp updtime) {
    this.updtime = updtime;
  }

  public Timestamp getCretime() {
    return cretime;
  }

  public void setCretime(Timestamp cretime) {
    this.cretime = cretime;
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

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getMobile() {
    return mobile;
  }

  public void setMobile(String mobile) {
    this.mobile = mobile;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isMember() {
    return isMember;
  }

  public void setMember(boolean member) {
    isMember = member;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public ContactGroup getGroup() {
    return group;
  }

  public void setGroup(ContactGroup group) {
    this.group = group;
  }

  public List<ContactNote> getNotes() {
    return notes;
  }

  public void setNotes(List<ContactNote> notes) {
    this.notes = notes;
  }

}
