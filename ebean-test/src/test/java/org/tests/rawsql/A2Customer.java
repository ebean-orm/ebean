package org.tests.rawsql;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;

@Entity
@Sql
public class A2Customer {

  private long custId;

  private String customerName;

  public long getCustId() {
    return custId;
  }

  public void setCustId(long custId) {
    this.custId = custId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }
}
