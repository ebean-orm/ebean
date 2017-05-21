package org.tests.it.ddlgeneration;


import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(schema = "public", name = "global_model")
public class GlobalTestModel {
  @Id
  Integer id;
  
  public Integer getId() {
    return id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
}
