package org.tests.basic.type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.List;
import java.util.Map;

@Entity
public class BSimpleWithGen {

  @Id
  Integer id;

  String name;

  @Transient
  Map<String, List<String>> someMap;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, List<String>> getSomeMap() {
    return someMap;
  }

  public void setSomeMap(Map<String, List<String>> someMap) {
    this.someMap = someMap;
  }

}
