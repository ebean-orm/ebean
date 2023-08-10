package org.tests.model.virtualprop;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("A")
public class VirtualBaseA extends VirtualBaseInherit {

  private Integer num;

  public Integer getNum() {
    return num;
  }

  public void setNum(Integer num) {
    this.num = num;
  }
}
