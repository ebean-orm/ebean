package org.tests.merge;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class MContact extends MBase {

  String email;
  String firstName;
  String lastName;

  @ManyToOne
  MCustomer customer;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "contact")
  List<MContactMessage> messages;

  public MContact(String email, String firstName, String lastName) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
  }

  public MCustomer getCustomer() {
    return customer;
  }

  public void setCustomer(MCustomer customer) {
    this.customer = customer;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
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

  public List<MContactMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<MContactMessage> messages) {
    this.messages = messages;
  }
}
