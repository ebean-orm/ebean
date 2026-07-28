package org.domain;


import io.ebean.annotation.Cache;
import io.ebean.annotation.Index;
import jakarta.persistence.Entity;

@Cache(naturalKey = "name")
@Entity
public class FRCust extends EBase {

  @Index(unique = true)
  String name;

  public FRCust(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
