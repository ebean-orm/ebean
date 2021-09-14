package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "muser_type")
public class MUserType {

  @Id
  Integer id;

  String name;

  public MUserType() {
    super();
  }

  public MUserType(String name) {
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
