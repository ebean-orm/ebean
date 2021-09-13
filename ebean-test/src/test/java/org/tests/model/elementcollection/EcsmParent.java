package org.tests.model.elementcollection;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class EcsmParent {

  @Id
  private long id;

  @Version
  private long version;

  private String name;

  @OneToMany(cascade = ALL)
  private List<EcsmChild> children;

  public EcsmParent(String name) {
    this.name = name;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<EcsmChild> getChildren() {
    return children;
  }

  public void setChildren(List<EcsmChild> children) {
    this.children = children;
  }

}
