package org.tests.model.json;

import io.ebean.annotation.DbHstore;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.LinkedHashMap;
import java.util.Map;

@Entity
public class EBasicHstore {

  @Id
  Long id;

  String name;

  // fallback to varchar(800) for non-Postgres
  @DbHstore(length = 800)
  Map<String, String> map;

  @Version
  Long version;

  public EBasicHstore(String name) {
    this.name = name;
    this.map = new LinkedHashMap<>();
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, String> getMap() {
    return map;
  }

  public void setMap(Map<String, String> map) {
    this.map = map;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
