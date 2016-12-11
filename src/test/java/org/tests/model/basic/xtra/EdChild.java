package org.tests.model.basic.xtra;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "td_child")
public class EdChild {
  @Id
  @Column(name = "child_id")
  private int id;

  @Column(name = "child_name")
  private String name;

  @ManyToOne
  @JoinColumn(name = "parent_id", nullable = false)
  EdParent parent;

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

  public EdParent getParent() {
    return parent;
  }

  public void setParent(EdParent parent) {
    this.parent = parent;
  }
}
