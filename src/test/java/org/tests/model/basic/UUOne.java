package org.tests.model.basic;

import io.ebean.annotation.Cache;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.UUID;

@Cache
@Entity
public class UUOne {

  @Id
  UUID id;

  String name;


  @OneToMany(cascade = CascadeType.ALL, mappedBy = "master")
  List<UUTwo> comments;

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

  public List<UUTwo> getComments() {
    return comments;
  }

  public void setComments(List<UUTwo> comments) {
    this.comments = comments;
  }

}
