package org.tests.model.virtualprop;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "kind")
public class VirtualBaseInherit extends AbstractVirtualBase {

  private String data;

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

}
