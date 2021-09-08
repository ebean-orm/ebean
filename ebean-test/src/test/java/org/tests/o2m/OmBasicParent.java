package org.tests.o2m;

import io.ebean.annotation.Where;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class OmBasicParent {

  @Id
  private long id;

  private final String name;

  @Version
  private long version;

  /**
   * Not really sensible Java code here but Kotlin can generate
   * a collection type that looks like this
   */
  @OneToMany(cascade = ALL, mappedBy = "parent")
  private List<? extends OmBasicChild> children;

  @OneToMany(cascade = ALL, mappedBy = "parent")
  @Where(clause = "'${dbTableName}' = ${ta}.name")
  private List<? extends OmBasicChild> childrenWithWhere;

  public OmBasicParent(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public List<? extends OmBasicChild> getChildren() {
    return children;
  }

  public void setChildren(List<? extends OmBasicChild> children) {
    this.children = children;
  }

  public List<? extends OmBasicChild> getChildrenWithWhere() {
    return childrenWithWhere;
  }

  public void setChildrenWithWhere(List<? extends OmBasicChild> childrenWithWhere) {
    this.childrenWithWhere = childrenWithWhere;
  }

}
