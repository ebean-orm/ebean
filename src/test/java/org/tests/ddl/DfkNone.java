package org.tests.ddl;

import io.ebean.annotation.DbForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DfkNone {

  @Id
  long id;

  String name;

  @ManyToOne
  @DbForeignKey(noConstraint = true)
  DfkOne one;

  public DfkNone(String name, DfkOne one) {
    this.name = name;
    this.one = one;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DfkOne getOne() {
    return one;
  }

  public void setOne(DfkOne one) {
    this.one = one;
  }
}
