package org.tests.model.json;

import io.ebean.annotation.DbJsonB;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Entity
public class EBasicJsonNodeJsonB {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  //@DbJson(storage = DbJsonType.JSONB)
  @DbJsonB
  JsonNode content;

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

  public JsonNode getContent() {
    return content;
  }

  public void setContent(JsonNode content) {
    this.content = content;
  }
}
