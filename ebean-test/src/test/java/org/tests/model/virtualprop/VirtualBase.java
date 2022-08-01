package org.tests.model.virtualprop;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Roland Praml, FOCONIS AG
 */
@Entity
public class VirtualBase {

  // TOOD: add to enhancer
  public static int _ebean_virtual_prop_count = 0;

  public int _ebean_getVirtualPropertyCount() {
    return _ebean_virtual_prop_count;
  }
  // TODO: Enhancer end

  @Id
  private int id;

  private String data;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }
}
