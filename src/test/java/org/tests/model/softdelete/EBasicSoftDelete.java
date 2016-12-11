package org.tests.model.softdelete;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
public class EBasicSoftDelete extends BaseSoftDelete {

  String name;

  String description;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  List<EBasicSDChild> children;

  @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
  List<EBasicNoSDChild> nosdChildren;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<EBasicSDChild> getChildren() {
    return children;
  }

  public void setChildren(List<EBasicSDChild> children) {
    this.children = children;
  }

  public void addChild(String childName, long amount) {
    getChildren().add(new EBasicSDChild(this, childName, amount));
  }

  public List<EBasicNoSDChild> getNosdChildren() {
    return nosdChildren;
  }

  public void setNosdChildren(List<EBasicNoSDChild> nosdChildren) {
    this.nosdChildren = nosdChildren;
  }

  public void addNoSoftDeleteChild(String childName, long amount) {
    getNosdChildren().add(new EBasicNoSDChild(this, childName, amount));
  }
}
