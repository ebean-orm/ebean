package org.tests.lazyforeignkeys;

import io.ebean.annotation.Formula;
import io.ebean.annotation.Platform;
import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "main_entity")
public class MainEntity {

  @Id
  private String id;

  private String attr1;

  private String attr2;

  @SoftDelete
  @Formula(select = "${ta}.id is null")
  @Formula(select = "CASE WHEN ${ta}.id is null THEN 1 ELSE 0 END", platforms = {Platform.SQLSERVER17, Platform.ORACLE})
  // evaluates to true in a left join if bean has been deleted.
  boolean deleted;

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

  public boolean isDeleted() {
    return deleted;
  }
}
