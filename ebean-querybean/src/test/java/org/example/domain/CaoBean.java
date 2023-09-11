package org.example.domain;

import javax.persistence.*;

@Entity
public class CaoBean {

  @Id
  private CaoKey key;

  private String description;

  @Version
  private long version;

  public CaoKey getKey() {
    return key;
  }

  public void setKey(CaoKey key) {
    this.key = key;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

}
