package org.tests.model.basic;

import io.ebean.annotation.Cache;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.util.List;
import java.util.UUID;

@Cache
@Entity
public class UUOne {

  @Id
  UUID id;

  String name;

  String description;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "master")
  List<UUTwo> comments;

  @Version
  long version;

  public UUOne() {
  }

  public UUOne(String name, UUID id) {
    this.name = name;
    this.id = id;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

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

  public List<UUTwo> getComments() {
    return comments;
  }

  public void setComments(List<UUTwo> comments) {
    this.comments = comments;
  }

  public long getVersion() {
    return version;
  }

  public void setVersion(long version) {
    this.version = version;
  }
}
