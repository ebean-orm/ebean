package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.Map;

@Entity
public class ETransMany {

  @Id
  Integer id;

  String name;

  @Transient
  Map<String, String> transMap;

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

  public Map<String, String> getTransMap() {
    return transMap;
  }

  public void setTransMap(Map<String, String> transMap) {
    this.transMap = transMap;
  }

}
