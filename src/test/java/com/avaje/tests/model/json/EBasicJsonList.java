package com.avaje.tests.model.json;

import com.avaje.ebean.annotation.DbJson;
import com.avaje.ebean.annotation.DbJsonType;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
public class EBasicJsonList {

  @Id
  Long id;

  String name;

  @DbJson(length = 50)
  Set<Long> flags = new LinkedHashSet<Long>();

  @DbJson(length = 100, storage = DbJsonType.VARCHAR)
  List<String> tags = new ArrayList<String>();

  @Version
  Long version;

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

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public Set<Long> getFlags() {
    return flags;
  }

  public void setFlags(Set<Long> flags) {
    this.flags = flags;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
