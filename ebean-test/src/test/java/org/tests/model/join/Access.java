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
  Customer accessor;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "principal_id")
  Customer principal;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "account_account_number")
  Account account;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Customer getAccessor() {
    return accessor;
  }
  
  public void setAccessor(Customer accessor) {
    this.accessor = accessor;
  }
  
  public Customer getPrincipal() {
    return principal;
  }

  public void setPrincipal(Customer principal) {
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