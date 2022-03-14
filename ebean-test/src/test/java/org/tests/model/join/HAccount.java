package org.tests.model.join;

import javax.persistence.*;

import org.tests.model.basic.Customer;

@Entity
@Inheritance
public abstract class Account {

  @Id
  String accountNumber;

  @ManyToOne
  @JoinColumn(name = "owner_id")//, referencedColumnName = "id")
  ACustomer owner;

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public ACustomer getOwner() {
    return owner;
  }

  public void setOwner(ACustomer owner) {
    this.owner = owner;
  }

}
