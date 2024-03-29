package org.tests.model.json;

import io.ebean.annotation.DbJson;
import io.ebean.annotation.DbJsonType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import java.util.Map;

@Entity
public class EBasicJsonMapClob {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @DbJson(storage = DbJsonType.CLOB)
  Map<String, Object> content;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, Object> getContent() {
    return content;
  }

  public void setContent(Map<String, Object> content) {
    this.content = content;
  }
}
