package org.tests.model.join;

import javax.persistence.*;

@Entity
@Inheritance
public abstract class HAccount {

  @Id
  String accountNumber;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  HCustomer owner;

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    this.accountNumber = accountNumber;
  }

  public HCustomer getOwner() {
    return owner;
  }

  public void setOwner(HCustomer owner) {
    this.owner = owner;
  }

}
