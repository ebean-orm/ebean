package org.tests.model.ivo;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ESomeConvertType {

  @Id
  Long id;

  String name;

  Money money;

  public ESomeConvertType(String name, Money money) {
    this.name = name;
    this.money = money;
  }

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

  public Money getMoney() {
    return money;
  }

  public void setMoney(Money money) {
    this.money = money;
  }
}
