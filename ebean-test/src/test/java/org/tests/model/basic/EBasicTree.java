package org.tests.model.basic;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "e_basic_tree")
public class EBasicTree {

  @Id
  private int id;

  @ManyToOne
  private EBasicTree parent;

  @OneToMany
  @OrderBy("ref.name")
  private List<EBasicTree> children;

  @ManyToOne
  private EBasic ref;

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public EBasicTree getParent() {
    return parent;
  }

  public void setParent(EBasicTree parent) {
    this.parent = parent;
  }

  public List<EBasicTree> getChildren() {
    return children;
  }

  public void setChildren(List<EBasicTree> children) {
    this.children = children;
  }

  public EBasic getRef() {
    return ref;
  }

  public void setRef(EBasic ref) {
    this.ref = ref;
  }
}
