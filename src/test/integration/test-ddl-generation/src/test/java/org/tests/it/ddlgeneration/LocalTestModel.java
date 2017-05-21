package org.tests.it.ddlgeneration;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "local_model")
public class LocalTestModel {
  @Id
  Integer id;
  
  public Integer getId() {
    return id;
  }
  
  public void setId(Integer id) {
    this.id = id;
  }
}
