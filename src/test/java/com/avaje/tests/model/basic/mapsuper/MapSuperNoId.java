package com.avaje.tests.model.basic.mapsuper;

import com.avaje.ebean.annotation.CreatedTimestamp;

import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import java.sql.Timestamp;

@MappedSuperclass
public class MapSuperNoId {

  @CreatedTimestamp
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
