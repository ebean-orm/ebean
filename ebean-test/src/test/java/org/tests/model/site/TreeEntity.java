package org.tests.model.site;

import io.ebean.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class TreeEntity extends Model {

  @Id
  private int id;

  private String text;

  @ManyToOne
  private TreeEntity parent;

  @OneToMany(cascade = ALL)
  private List<TreeEntity> children;

  public TreeEntity(String text) {
    this.text = text;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getText() {
    return text;
  }

  public TreeEntity getParent() {
    return parent;
  }

  public void setParent(TreeEntity parent) {
    this.parent = parent;
  }

  public List<TreeEntity> getChildren() {
    return children;
  }

  public void setChildren(List<TreeEntity> children) {
    this.children = children;
  }
}
