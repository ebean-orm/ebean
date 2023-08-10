package org.domain;

import io.ebean.Model;
import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheBeanTuning;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Cache(enableQueryCache = true)
@CacheBeanTuning(maxSecsToLive = 1)
@Entity
public class UParent extends Model {

  @Id
  private UUID id;

  private String name;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private final List<UChild> children = new ArrayList<>();

  public UParent(String name) {
    this.name = name;
  }

  public UUID id() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String name() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<UChild> children() {
    return children;
  }
}
