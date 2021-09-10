package org.tests.model.json;

import io.ebean.annotation.DbJsonB;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.Map;

@Entity
public class EBasicJsonMapJsonB {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @DbJsonB
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
