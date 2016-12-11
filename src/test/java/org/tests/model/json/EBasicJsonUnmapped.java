package org.tests.model.json;

import io.ebean.annotation.UnmappedJson;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.Map;

@Entity
public class EBasicJsonUnmapped {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @UnmappedJson
  Map<String, Object> unmapped;

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

  public Map<String, Object> getUnmapped() {
    return unmapped;
  }

  public void setUnmapped(Map<String, Object> unmapped) {
    this.unmapped = unmapped;
  }
}
