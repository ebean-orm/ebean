package org.tests.model.basic;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
@Sql
public class MyAdHoc {

  @ManyToOne
  Order order;

  int detailCount;

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public int getDetailCount() {
    return detailCount;
  }

  public void setDetailCount(int detailCount) {
    this.detailCount = detailCount;
  }

}
