package org.tests.model.basic;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * An example of an Aggregate object.
 * <p>
 * Note the &#064;Sql indicates to Ebean that this bean is not based on a table but
 * instead uses RawSql.
 * </p>
 */
@Entity
@Sql
public class CustomerAggregate {

  @OneToOne
  Customer customer;

  int totalContacts;


  @Override
  public String toString() {
    return customer.getId() + " totalContacts:" + totalContacts;
  }


  public Customer getCustomer() {
    return customer;
  }


  public void setCustomer(Customer customer) {
    this.customer = customer;
  }


  public int getTotalContacts() {
    return totalContacts;
  }


  public void setTotalContacts(int totalContacts) {
    this.totalContacts = totalContacts;
  }
}
