package org.tests.model.orphanremoval;

import io.ebean.Model;

import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Entity
public class OmBeanListParent extends Model {

  @Id
  private long id;

  @Version
  private long version;

  private String name;

  @WhenCreated
  private Instant whenCreated;
  @WhenModified
  private Instant whenModified;

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

  public void setChildren2(List<OmBeanListChild> children) {
    this.children = children;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public Instant getWhenModified() {
    return whenModified;
  }

  public void setWhenModified(Instant whenModified) {
    this.whenModified = whenModified;
  }

  public Instant getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Instant whenCreated) {
    this.whenCreated = whenCreated;
  }
}


