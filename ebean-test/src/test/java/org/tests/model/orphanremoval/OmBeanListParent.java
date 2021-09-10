package org.tests.model.orphanremoval;

import io.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

@Entity
public class OmBeanListParent extends Model {

  @Id
  private long id;

  @Version
  private long version;

  @OneToMany(cascade = ALL, mappedBy = "parent", orphanRemoval = true)
  private List<OmBeanListChild> children;

  public long getId() {
    return id;
  }

  public List<OmBeanListChild> getChildren() {
    return children;
  }

  public void setChildren(List<OmBeanListChild> children) {
    // So a BeanList is used and overwriting children will replace the table entries
    this.children.clear();
    this.children.addAll(children);
  }
}


