package org.tests.ddl;

import io.ebean.annotation.ConstraintMode;
import io.ebean.annotation.DbForeignKey;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class DfkSetNull {

  @Id
  long id;

  String name;

  @ManyToOne
  @DbForeignKey(onDelete = ConstraintMode.SET_NULL)
  DfkOne one;

  public DfkSetNull(String name, DfkOne one) {
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
