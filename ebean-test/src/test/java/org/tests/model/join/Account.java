package org.tests.model.join;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;

import org.tests.model.basic.Customer;

@Entity
@Inheritance
public abstract class Account {

  @Id
  String accountNumber;
  
  @ManyToOne
  Customer owner;

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public Customer getOwner() {
    return owner;
  }

  public void setOwner(Customer owner) {
    this.owner = owner;
  }
  
}