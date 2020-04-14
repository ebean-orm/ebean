package org.tests.model.version;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
public class VersionParent {

  @Id
  Integer id;

  @Version
  Integer version;

  String name;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "position")
  List<VersionChild> children = new ArrayList<>();

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(final Integer version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<VersionChild> getChildren() {
    return children;
  }

  public void setChildren(final List<VersionChild> children) {
    this.children = children;
  }
}
