package org.tests.compositekeys.db;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;

@Entity
public class SubType {
  @EmbeddedId
  private SubTypeKey key;

  private String description;

  @Version
  private Long version;

  public SubTypeKey getKey() {
    return key;
  }

  public void setKey(SubTypeKey key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
