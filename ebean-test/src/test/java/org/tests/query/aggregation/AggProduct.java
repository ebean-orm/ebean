package org.tests.query.aggregation;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AggProduct {

  @Id
  private Integer id;
  private String name;
  private int price;

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

  public int getPrice() {
    return price;
  }

  public void setPrice(int price) {
    this.price = price;
  }
}
