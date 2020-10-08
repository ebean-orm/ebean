package org.tests.model.zero;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "parent")
public class WithZeroParent {
  @Id
  @Column(name = "id")
  private int id;

  String name;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private List<WithZero> children;

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<WithZero> getChildren() {
    return children;
  }

  public void setChildren(final List<WithZero> children) {
    this.children = children;
  }

}
