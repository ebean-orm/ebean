package org.domain;


import io.ebean.annotation.Cache;
import io.ebean.annotation.CacheBeanTuning;
import io.ebean.annotation.Index;

import javax.persistence.Entity;
import java.time.LocalDate;

@Cache(naturalKey = "name")
@Entity
public class RCust extends EBase {

  @Index(unique = true)
  String name;

  public RCust(String name) {
    this.name = name;
  }

}
