package org.tests.lazyforeignkeys;

import javax.persistence.Entity;
import javax.persistence.Id;

import io.ebean.annotation.View;

@View(name = "vw_main_entity")
@Entity
public class ViewMainEntity {

  @Id
  private String id;

  private String attr1;
  
  private String attr2;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAttr1() {
    return attr1;
  }

  public void setAttr1(String attr1) {
    this.attr1 = attr1;
  }

  public String getAttr2() {
    return attr2;
  }

  public void setAttr2(String attr2) {
    this.attr2 = attr2;
  }
}
