package org.tests.model.json;

import io.ebean.annotation.DbJson;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import java.util.Map;

@Entity
public class EBasicJsonMapDetail {

  @Id
  Long id;

  @ManyToOne
  EBasicJsonMap owner;

  @Version
  Long version;

  String name;

  @DbJson
  Map<String, Object> content;

  public EBasicJsonMapDetail(String name) {
    this.name = name;
  }

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

  public EBasicJsonMap getOwner() {
    return owner;
  }

  public void setOwner(EBasicJsonMap owner) {
    this.owner = owner;
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
