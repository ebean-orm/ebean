package org.test;

import io.ebean.annotation.WhenModified;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.persistence.Version;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
public class BSimpleWithGen {

  @Id
  private Integer id;

  private String name;

  private String other;

  @Transient
  private Map<String, List<String>> someMap;

  @WhenModified
  private Instant whenModified;

  @Version
  private long version;

  public BSimpleWithGen(String name) {
    this.name = name;
  }

  public BSimpleWithGen(String name, String other) {
    this.name = name;
    this.other = other;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Map<String, List<String>> getSomeMap() {
    return someMap;
  }

  public void setSomeMap(Map<String, List<String>> someMap) {
    this.someMap = someMap;
  }

  public Instant getWhenModified() {
    return whenModified;
  }

  public long getVersion() {
    return version;
  }
}
