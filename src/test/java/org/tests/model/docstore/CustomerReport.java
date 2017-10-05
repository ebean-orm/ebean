package org.tests.model.docstore;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.tests.model.basic.Customer;

import io.ebean.annotation.DocStore;

/**
 * Entity that will stored as JSON in database
 * 
 * @author Roland Praml, FOCONIS AG
 *
 */
@DocStore
@DiscriminatorValue("CR")
public class CustomerReport extends Report {

  @OneToMany
  private List<Customer> friends;

  @ManyToOne
  private Customer customer;

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setFriends(List<Customer> friends) {
    this.friends = friends;
  }

  public List<Customer> getFriends() {
    return friends;
  }

}
