package org.tests.model.docstore;

import io.ebean.annotation.DocStore;
import org.tests.model.basic.Customer;

import javax.persistence.DiscriminatorValue;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

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
