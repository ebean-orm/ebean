package org.tests.model.join;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.ebean.annotation.DbForeignKey;

@Inheritance
@Entity
public abstract class HAccess {

  @Id
  UUID id;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "accessor_id")
  HCustomer accessor;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "principal_id")
  HCustomer principal;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  @JoinColumn(name = "access_account_number")
  HAccount account;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public HCustomer getAccessor() {
    return accessor;
  }

  public void setAccessor(HCustomer accessor) {
    this.accessor = accessor;
  }

  public HCustomer getPrincipal() {
    return principal;
  }

  public void setPrincipal(HCustomer principal) {
    this.principal = principal;
  }

  public HAccount getAccount() {
    return account;
  }

  public void setAccount(HAccount account) {
    this.account = account;
    setPrincipal(account.getOwner());
  }

}
