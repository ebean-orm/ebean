package com.avaje.tests.model.basic;

import javax.persistence.*;

@Entity
@Inheritance
@DiscriminatorColumn(name = "my_type", length = 3, discriminatorType = DiscriminatorType.INTEGER)
@DiscriminatorValue("1")
public class TIntRoot {

  @Id
  Integer id;

  String name;

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

}
