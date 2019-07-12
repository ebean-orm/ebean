package org.tests.lazyforeignkeys;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "main_entity_relation")
public class MainEntityRelation {

  @Id
  private UUID id;

  private String id1;
  
  private String id2;
  
  private String attr1;

  public String getId1() {
    return id1;
  }

  public void setId1(String id1) {
    this.id1 = id1;
  }

  public String getId2() {
    return id2;
  }

  public void setId2(String id2) {
    this.id2 = id2;
  }
  
  public String getAttr1() {
    return attr1;
  }

  public void setAttr1(String attr1) {
    this.attr1 = attr1;
  }
}
