package org.tests.cascade;

import io.ebean.annotation.SoftDelete;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class COOneMany {

  @Id
  private long id;

  @SoftDelete
  private boolean deleted;

  private String name;

  public COOneMany(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public long getId() {
    return id;
  }

  public boolean isDeleted() {
    return deleted;
  }
}
