package com.avaje.tests.model.zero;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "parent")
public class WithZeroParent {
  @Id
  @Column(name = "id")
  private int id;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private List<WithZero> children;

  public List<WithZero> getChildren() {
    return children;
  }

  public void setChildren(final List<WithZero> children) {
    this.children = children;
  }

  public int getId() {
    return id;
  }
}