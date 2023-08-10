package org.tests.model.virtualprop;

import io.ebean.bean.extend.ExtendableBean;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

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
