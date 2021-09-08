package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MProtectedConstructBean {

  @Id
  Long id;

  String name;

//  private MProtectedConstructBean() {
//
//  }

  public MProtectedConstructBean(String name) {
    this.name = name;
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

}
