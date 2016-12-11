package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "e_basic_ndc")
public class EBasicNoDefaultConstructor {

  @Id
  Integer id;

  String name;

  public EBasicNoDefaultConstructor() {
  }

  public EBasicNoDefaultConstructor(Integer id, String name) {
    this.id = id;
    this.name = name;
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

}
