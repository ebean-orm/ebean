package org.domain;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Cache
@Entity
public class FUChild extends Model {

  @Id
  long id;

  String name;

  @ManyToOne
  final FUParent parent;

  public FUChild(FUParent parent, String name) {
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
