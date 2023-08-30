package org.tests.model.basic.xtra;


import jakarta.persistence.*;
import java.util.List;

@MappedSuperclass
public abstract class EdParent {

  @Id
  @Column(name = "parent_id")
  private int id;

  String parentType = "EXTENDED";

  @Column(name = "parent_name")
  private String name;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "parent", cascade = CascadeType.ALL)
  List<EdChild> children;

  public List<EdChild> getChildren() {
    return children;
  }

  public void setChildren(List<EdChild> children) {
    this.children = children;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}

