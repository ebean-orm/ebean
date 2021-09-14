package org.tests.model.composite;


import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

/**
 * @author rnentjes
 */
@Entity
public class RCustomer {

  @EmbeddedId
  private RCustomerKey key;

  private String description;

  public RCustomer() {
  }

  public RCustomer(RCustomerKey key, String description) {
    this.key = key;
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public RCustomerKey getKey() {
    return key;
  }

  public void setKey(RCustomerKey key) {
    this.key = key;
  }

}

