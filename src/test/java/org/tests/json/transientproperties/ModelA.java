package org.tests.json.transientproperties;

import io.ebean.annotation.Sql;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;

@Sql
@Entity
public class ModelA {

  @Id
  int id;

  String a;

  // transient mapping to an entity bean
  @Transient
  List<ModelB> list;

  public String getA() {
    return a;
  }

  public void setA(String a) {
    this.a = a;
  }

  public List<ModelB> getList() {
    return list;
  }

  public void setList(List<ModelB> list) {
    this.list = list;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }
}
