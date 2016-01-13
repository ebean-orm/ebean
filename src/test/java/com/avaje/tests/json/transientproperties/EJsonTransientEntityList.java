package com.avaje.tests.json.transientproperties;

import com.avaje.ebean.annotation.Sql;
import com.avaje.tests.model.basic.Order;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

@Sql
@Entity
public class EJsonTransientEntityList {

  @Id
  private Long id;

  private String name;

  @Transient
  private Boolean basic;

  @Transient
  private List<Order> orders;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Boolean getBasic() {
    return basic;
  }

  public void setBasic(Boolean basic) {
    this.basic = basic;
  }

  public List<Order> getOrders() {
    return orders;
  }

  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }
}
