package org.tests.model.virtualprop;

import javax.persistence.*;

@Entity
public class VirtualBase extends AbstractVirtualBase {

  private String data;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
