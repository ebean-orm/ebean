package org.tests.model.basic.mapsuper;

import io.ebean.annotation.WhenCreated;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.sql.Timestamp;

@MappedSuperclass
public class MapSuperNoId {

  @WhenCreated
  Timestamp whenCreated;

  @Version
  Timestamp whenUpdated;

  public Timestamp getWhenCreated() {
    return whenCreated;
  }

  public void setWhenCreated(Timestamp whenCreated) {
    this.whenCreated = whenCreated;
  }

  public Timestamp getWhenUpdated() {
    return whenUpdated;
  }

  public void setWhenUpdated(Timestamp whenUpdated) {
    this.whenUpdated = whenUpdated;
  }

}
