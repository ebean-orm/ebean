package org.tests.model.orphanremoval;

import io.ebean.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;

@Entity
public class OmBeanListChild extends Model {

  @Id
  private Long id;

  private final String name;

  @ManyToOne
  private OmBeanListParent parent;

  @Version
  private long version;

  public OmBeanListChild(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }
}


