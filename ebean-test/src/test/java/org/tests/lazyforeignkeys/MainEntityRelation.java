package org.tests.lazyforeignkeys;

import io.ebean.annotation.DbForeignKey;

import javax.persistence.*;

import org.tests.model.basic.Cat;

import java.util.UUID;


@Entity
@Table(name = "main_entity_relation")
public class MainEntityRelation {

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
  
  @ManyToOne
  @JoinColumn(name = "cat_id")
  @DbForeignKey(noConstraint = true)
  private Cat cat;

  private String attr1;

  public MainEntity getEntity1() {
    return entity1;
  }

  public void setEntity1(MainEntity entity1) {
    this.entity1 = entity1;
  }

  public MainEntity getEntity2() {
    return entity2;
  }

  public void setEntity2(MainEntity entity2) {
    this.entity2 = entity2;
  }

  public Cat getCat() {
    return cat;
  }

  public void setCat(Cat cat) {
    this.cat = cat;
  }

  public String getAttr1() {
    return attr1;
  }

  public void setAttr1(String attr1) {
    this.attr1 = attr1;
  }
}
