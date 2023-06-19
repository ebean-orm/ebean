package org.tests.model.json;

import io.ebean.annotation.DbJson;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class EBasicJsonMap {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @DbJson(length = 5000)
  Map<String, Object> content;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  List<EBasicJsonMapDetail> details = new ArrayList<>();

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

  public List<EBasicJsonMapDetail> getDetails() {
    return details;
  }

  public void setDetails(List<EBasicJsonMapDetail> details) {
    this.details = details;
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
