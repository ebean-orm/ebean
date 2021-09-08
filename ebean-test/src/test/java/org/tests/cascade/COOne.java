package org.tests.cascade;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class COOne {

  @Id
  private long id;

  private final String name;

  @OneToMany(cascade = ALL, orphanRemoval = true)
  private List<COOneMany> children;

  public COOne(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public List<COOneMany> getChildren() {
    return children;
  }

  public void setChildren(List<COOneMany> children) {
    this.children = children;
  }
}
