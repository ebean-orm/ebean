package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import java.util.UUID;

@Cache(naturalKey = "uid")
@Entity
public class OCachedNkeyUid extends OCacheBase {

  private UUID uid;

  private String other;

  public OCachedNkeyUid(UUID uid, String other) {
    this.uid = uid;
    this.other = other;
  }

  public UUID getUid() {
    return uid;
  }

  public String getOther() {
    return other;
  }
}
