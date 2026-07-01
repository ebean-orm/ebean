package org.tests.model.json;

import io.ebean.annotation.DbJson;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;

@MappedSuperclass
public class EJsonGenericBase<T> {

  @Id
  Long id;

  String name;

  @DbJson
  T jsonData;

  @Version
  Long version;

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public T getJsonData() {
    return jsonData;
  }

  public void setJsonData(T jsonData) {
    this.jsonData = jsonData;
  }

  public Long getVersion() {
    return version;
  }
}
