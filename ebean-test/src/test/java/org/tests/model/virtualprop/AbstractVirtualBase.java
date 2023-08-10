package org.tests.model.virtualprop;

import io.ebean.bean.extend.ExtendableBean;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractVirtualBase implements ExtendableBean {

  @Id
  private int id;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

}
