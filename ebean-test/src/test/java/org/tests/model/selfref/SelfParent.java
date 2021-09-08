package org.tests.model.selfref;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;
import java.util.List;

@Entity
@Table(name = "self_parent")
public class SelfParent {

  @Id
  Long id;

  @Version
  Long version;

  String name;

  @ManyToOne()
  SelfParent parent;

  @OneToMany(mappedBy = "parent")
  List<SelfParent> children;

  public SelfParent(String name, SelfParent parent) {
    this.name = name;
    this.parent = parent;
  }

  public SelfParent() {

  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SelfParent getParent() {
    return parent;
  }

  public void setParent(SelfParent parent) {
    this.parent = parent;
  }

  public List<SelfParent> getChildren() {
    return children;
  }

  public void setChildren(List<SelfParent> children) {
    this.children = children;
  }
}
