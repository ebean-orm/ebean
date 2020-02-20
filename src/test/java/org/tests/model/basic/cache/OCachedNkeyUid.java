package org.tests.model.basic.cache;

import io.ebean.annotation.Cache;

import javax.persistence.Entity;
import java.util.UUID;

@Cache(naturalKey = "cid")
@Entity
public class OCachedNkeyUid extends OCacheBase {

  private UUID cid;

  private String other;

  public OCachedNkeyUid(UUID cid, String other) {
    this.cid = cid;
    this.other = other;
  }

  public UUID getCid() {
    return cid;
  }

  public String getOther() {
    return other;
  }
}
