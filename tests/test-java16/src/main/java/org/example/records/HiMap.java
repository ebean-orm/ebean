package org.example.records;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class HiMap {

  @Id
  private long id;

  @ManyToOne
  private HiBasic parent;

  private final String key;
  private final String val;

  public HiMap(String key, String val) {
    this.key = key;
    this.val = val;
  }

  public long id() {
    return id;
  }

  public HiMap setId(long id) {
    this.id = id;
    return this;
  }

  public HiBasic parent() {
    return parent;
  }

  public HiMap setParent(HiBasic parent) {
    this.parent = parent;
    return this;
  }

  public String key() {
    return key;
  }

  public String val() {
    return val;
  }
}
