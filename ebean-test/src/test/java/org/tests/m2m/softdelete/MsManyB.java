package org.tests.m2m.softdelete;

import io.ebean.annotation.SoftDelete;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import java.util.List;

@Entity
public class MsManyB {

  @Id
  Long bid;

  String name;

  @SoftDelete
  boolean deleted;

  @ManyToMany
  List<MsManyA> manyas;

  public MsManyB(String name) {
    this.name = name;
  }
}
