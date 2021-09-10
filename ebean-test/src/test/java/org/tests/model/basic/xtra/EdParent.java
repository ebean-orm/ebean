package org.tests.model.basic.xtra;


import javax.persistence.*;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "parent_type")
@DiscriminatorValue("BASIC")
@Table(name = "td_parent")
public class EdParent {
  @Id
  @Column(name = "parent_id")
  private int id;

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

