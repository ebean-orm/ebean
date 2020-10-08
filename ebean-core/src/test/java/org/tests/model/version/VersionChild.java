package org.tests.model.version;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
public class VersionChild {

  @Id
  Integer id;

  String name;

  @Version
  Integer version;

  @OneToMany(mappedBy = "child", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn(name = "position")
  List<VersionToy> toys = new ArrayList<>();

  @ManyToOne
  VersionParent parent;

  public Integer getId() {
    return id;
  }

  public void setId(final Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(final Integer version) {
    this.version = version;
  }

  public List<VersionToy> getToys() {
    return toys;
  }

  public void setToys(final List<VersionToy> toys) {
    this.toys = toys;
  }

  public VersionParent getParent() {
    return parent;
  }

  public void setParent(final VersionParent parent) {
    this.parent = parent;
  }
}
