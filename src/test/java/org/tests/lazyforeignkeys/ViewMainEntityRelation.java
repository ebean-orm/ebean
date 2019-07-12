package org.tests.lazyforeignkeys;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import io.ebean.annotation.DbForeignKey;
import io.ebean.annotation.View;

@View(name = "vw_main_entity_relation")
@Entity
public class ViewMainEntityRelation {

  @Id
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "id1")
  @DbForeignKey(noConstraint = true)
  private MainEntity entity1;
  
  @ManyToOne
  @JoinColumn(name = "id2")
  @DbForeignKey(noConstraint = true)
  private MainEntity entity2;
  
  private String attr1;

  public String getAttr1() {
    return attr1;
  }

  public void setAttr1(String attr1) {
    this.attr1 = attr1;
  }

  public MainEntity getEntity1() {
    return entity1;
  }

  public MainEntity getEntity2() {
    return entity2;
  }
}
