package org.tests.m2m.softdelete;

import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

@Entity
public class MsManyA {

  @Id
  Long aid;

  String name;

  @SoftDelete
  boolean deleted;

  @ManyToMany
  List<MsManyB> manybs;

  public MsManyA(String name) {
    this.name = name;
  }

  public Long getAid() {
    return aid;
  }

  public void setAid(Long aid) {
    this.aid = aid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MsManyB> getManybs() {
    return manybs;
  }

  public void setManybs(List<MsManyB> manybs) {
    this.manybs = manybs;
  }
}
