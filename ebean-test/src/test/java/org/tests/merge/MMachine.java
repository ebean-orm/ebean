package org.tests.merge;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.List;

@Entity
public class MMachine {

  @Id
  private long id;

  private String name;

  @ManyToMany
  private List<MGroup> groups = new ArrayList<>();

  @Version
  private long version;

  public MMachine(String name) {
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

  public void setName(String name) {
    this.name = name;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }

  public List<MGroup> getGroups() {
    return groups;
  }

  public void setGroups(List<MGroup> groups) {
    this.groups = groups;
  }
}
