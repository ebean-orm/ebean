package org.tests.model.join;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.tests.model.basic.Customer;

import io.ebean.annotation.DbForeignKey;

@Inheritance
@Entity
public abstract class Access {

  @Id
  UUID id;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "accessor_id")
  ACustomer accessor;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "principal_id")
  ACustomer principal;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "access_account_number")
  Account account;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ACustomer getAccessor() {
    return accessor;
  }

  public void setAccessor(ACustomer accessor) {
    this.accessor = accessor;
  }

  public ACustomer getPrincipal() {
    return principal;
  }

  public void setPrincipal(ACustomer principal) {
    this.principal = principal;
  }

  public Account getAccount() {
    return account;
  }

  public void setAccount(Account account) {
    this.account = account;
    setPrincipal(account.getOwner());
  }

}
