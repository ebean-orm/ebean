package org.domain;

import io.ebean.Model;
import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Cache
@Entity
public class UChild extends Model {

  @Id
  long id;

  String name;

  @ManyToOne
  final UParent parent;

  public UChild(UParent parent, String name) {
    this.parent = parent;
    this.name = name;
  }

  public long id() {
    return id;
  }

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
