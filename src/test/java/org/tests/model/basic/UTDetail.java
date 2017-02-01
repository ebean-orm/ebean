package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "ut_detail")
public class UTDetail {

  @Id
  Integer id;

  String name;

  Integer qty;

  Double amount;

  @Version
  Integer version;

  public UTDetail() {
  }

  public UTDetail(String name, Integer qty, Double amount) {
    this.name = name;
    this.qty = qty;
    this.amount = amount;
  }

  @Override
  public String toString() {
    return id + " name:" + name + " qty:" + qty + " amt:" + amount;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getQty() {
    return qty;
  }

  public void setQty(Integer qty) {
    this.qty = qty;
  }

  public Double getAmount() {
    return amount;
  }

  public void setAmount(Double amount) {
    this.amount = amount;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

}
