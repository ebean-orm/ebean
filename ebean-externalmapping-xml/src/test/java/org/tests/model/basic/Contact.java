package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Contact extends BasicDomain {

  @ManyToOne(optional = false)
  Customer customer;

  String firstName;
  String lastName;
  String phone;
  String mobile;
  String email;
  boolean isMember;

  public Contact(String firstName, String lastName) {
    this.firstName = firstName;
    this.lastName = lastName;
  }
}
