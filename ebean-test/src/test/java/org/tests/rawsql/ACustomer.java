package org.tests.rawsql;

import io.ebean.annotation.Sql;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
@Sql
public class ACustomer {

  private long custId;

  @Column(name = "customerName")
  private String custName;


  public long getCustId() {
    return custId;
  }

  public void setCustId(long custId) {
    this.custId = custId;
  }

  public String getCustName() {
    return custName;
  }

  public void setCustName(String custName) {
    this.custName = custName;
  }
}
