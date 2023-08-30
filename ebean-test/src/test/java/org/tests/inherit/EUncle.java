package org.tests.inherit;

import jakarta.persistence.*;

@Entity
@Table(name = "rawinherit_uncle")
public class EUncle {

  @Id
  private Integer id;

  private String name;

  @ManyToOne(optional = false)
  private ChildA parent;

  @Version
  private Long version;

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

  public ChildA getParent() {
    return parent;
  }

  public void setParent(ChildA parent) {
    this.parent = parent;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }
}
