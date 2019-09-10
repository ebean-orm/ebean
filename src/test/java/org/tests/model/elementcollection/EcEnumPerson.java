package org.tests.model.elementcollection;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
public class EcEnumPerson {

  enum Tags {
    RED,
    BLUE,
    GREEN
  }

  @Id
  long id;

  String name;

  @ElementCollection
  Set<Tags> tags = new LinkedHashSet<>();

  @Version
  long version;

  public EcEnumPerson(String name) {
    this.name = name;
  }

  @Override
  public String toString() {
    return "person id:" + id + " name:" + name + " tags:" + tags;
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

  public void setName(String name) {
    this.name = name;
  }

  public Set<Tags> getTags() {
    return tags;
  }

  public void setTags(Set<Tags> tags) {
    this.tags = tags;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
