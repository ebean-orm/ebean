package org.tests.model.deleteorder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * Self referencing tree, as reported on #1852.
 */
@Entity
public class DcoTree {

  @Id
  @GeneratedValue
  Long id;

  String name;

  @ManyToOne
  DcoTree parent;

  @ManyToOne
  DcoTreeContainer container;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "parent")
  List<DcoTree> children = new ArrayList<>();

  public DcoTree(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public List<DcoTree> getChildren() {
    return children;
  }

  public DcoTree addChild(String name) {
    DcoTree child = new DcoTree(name);
    child.parent = this;
    children.add(child);
    return child;
  }
}
